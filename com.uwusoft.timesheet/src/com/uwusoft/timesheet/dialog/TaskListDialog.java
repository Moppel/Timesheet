package com.uwusoft.timesheet.dialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListDialog;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.extensionpoint.model.SubmissionTask;
import com.uwusoft.timesheet.util.ExtensionManager;

/**
 * todo: add class doc
 *
 * @author Uta Wunderlich
 * @version $Revision: $, $Date: Nov 29, 2011
 * @since Nov 29, 2011
 */
public class TaskListDialog extends ListDialog {

    private StorageService storageService;
    private SubmissionService submissionService;
    private Map<String,String> submissionSystems;
	private String[] systems, tasks;
	private Map<String, SubmissionTask> tasksMap;
    private Combo systemCombo, projectCombo;
    private String projectSelected, systemSelected;
    private boolean original;

    public TaskListDialog(Shell shell, String taskSelected) {
        super(shell);
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		storageService = new ExtensionManager<StorageService>(
                StorageService.SERVICE_ID).getService(preferenceStore.getString(StorageService.PROPERTY));
		List<String> systemsList = storageService.getSystems();
        systems = systemsList.toArray(new String[systemsList.size()]);
        tasks = taskSelected.split(SubmissionService.separator);
        submissionSystems = TimesheetApp.getSubmissionSystems();
		setContentProvider(ArrayContentProvider.getInstance());
		setLabelProvider(new LabelProvider());
    }
    
    @Override
    protected Control createDialogArea(Composite composite) {
        Composite parent = (Composite) super.createDialogArea(composite);
        
        Composite systemPanel = new Composite(parent, SWT.NONE);
        systemPanel.setLayout(new GridLayout(4, false));
        
        (new Label(systemPanel, SWT.NULL)).setText("Submission System: ");
        systemCombo = new Combo(systemPanel, SWT.READ_ONLY);
        systemCombo.setItems(systems);
        if (systems.length == 1) systemCombo.setEnabled(false);
        
        (new Label(systemPanel, SWT.NULL)).setText("Project: ");
        projectCombo = new Combo(systemPanel, SWT.READ_ONLY);
        
        Button originalButton = new Button(parent, SWT.CHECK);
        originalButton.setText("original");
        originalButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	original = ((Button) e.getSource()).getSelection();
                setTasksAndProjects(true);
            }
        });

        if (tasks.length > 2) {
        	for (int i = 0; i < systems.length; i++) {
        		if (tasks[2].equals(systems[i])) {
                	systemCombo.select(i);
                	break;
        		}
        	}
        }
        else systemCombo.select(0);
        setTasksAndProjects(true);
        
        systemCombo.setBounds(50, 50, 180, 65);
        systemCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setTasksAndProjects(false);
            }
        });
        
        projectCombo.setBounds(50, 50, 180, 65);
        projectCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setTasks();
            }
        });
        return parent;
    }

	private void setTasksAndProjects(boolean first) {
		systemSelected = systemCombo.getText();
		List<String> projectList;
		if (original) {
			submissionService = new ExtensionManager<SubmissionService>(SubmissionService.SERVICE_ID).getService(submissionSystems.get(systemSelected));
			projectList = new ArrayList<String>(submissionService.getAssignedProjects().keySet());
		}
		else {
			projectList = storageService.getProjects(systemSelected);
		}
		String[] projects = projectList.toArray(new String[projectList.size()]);
        projectCombo.setItems(projects);
        if (projects.length == 1) projectCombo.setEnabled(false);
		if (projectSelected == null && tasks.length > 1 && projectList.contains(tasks[1])) projectSelected = tasks[1];
		if (projectSelected != null) {
			for (int i = 0; i < projects.length; i++) {
				if (projectSelected.equals(projects[i])) {
					projectCombo.select(i);
					break;
				}
			}
		}
        else projectCombo.select(0);
		setTasks();
	}

	private void setTasks() {
		projectSelected = projectCombo.getText();
		if (original) {
			submissionService = new ExtensionManager<SubmissionService>(SubmissionService.SERVICE_ID).getService(submissionSystems.get(systemSelected));
			Map<String, Set<SubmissionTask>> assignedProjects = submissionService.getAssignedProjects();
			Set<SubmissionTask> submissionTasks = assignedProjects.get(projectSelected);
			if (submissionTasks == null) {
				projectSelected = assignedProjects.keySet().iterator().next();
				submissionTasks = assignedProjects.get(projectSelected);
			}
			List<String> tasks = new ArrayList<String>(storageService.findTasksBySystemAndProject(systemSelected, projectSelected));
			tasksMap = new HashMap<String, SubmissionTask>();
			for (SubmissionTask task : submissionTasks) {
				if (!tasks.contains(task.getName())) tasksMap.put(task.getName(), task);
			}
			getTableViewer().setInput(tasksMap.keySet());
		}
		else {
			List<String> tasks = new ArrayList<String>(storageService.findTasksBySystemAndProject(systemSelected, projectSelected));
			if (this.tasks.length > 1 && this.tasks[1].equals(projectSelected)) {
				if (this.tasks.length > 2 && this.tasks[2].equals(systemSelected)) tasks.remove(this.tasks[0]);
			}
	        getTableViewer().setInput(tasks);
		}
	}
	
	@Override
	protected void okPressed() {
		super.okPressed();
		if (original) {
		    String selectedTask = Arrays.toString(getResult());
		    selectedTask = selectedTask.substring(selectedTask.indexOf("[") + 1, selectedTask.indexOf("]"));
			if (StringUtils.isEmpty(selectedTask)) return;
			Map<String, Set<SubmissionTask>> projects = new HashMap<String, Set<SubmissionTask>>();
			projects.put(projectSelected, new HashSet<SubmissionTask>());
			projects.get(projectSelected).add(tasksMap.get(selectedTask));
			storageService.importTasks(systemSelected, projects);
		}
	}

	public String getSystem() {
		return systemSelected;
	}
	
	public String getProject() {
		return projectSelected;
	}
}
