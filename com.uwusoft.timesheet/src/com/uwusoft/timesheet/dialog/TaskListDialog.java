package com.uwusoft.timesheet.dialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.model.Task;
import com.uwusoft.timesheet.submission.model.SubmissionProject;
import com.uwusoft.timesheet.submission.model.SubmissionTask;
import com.uwusoft.timesheet.util.ExtensionManager;

/**
 * todo: add class doc
 *
 * @author Uta Wunderlich
 * @version $Revision: $, $Date: Nov 29, 2011
 * @since Nov 29, 2011
 */
public class TaskListDialog extends ListDialog {

    protected LocalStorageService storageService;
    private SubmissionService submissionService;
    protected Task taskSelected;
	protected String[] systems;
	private Map<String, SubmissionProject> assignedProjects;
	private Map<String, SubmissionTask> tasksMap;
    private Combo systemCombo, projectCombo;
    protected String projectSelected, systemSelected, task, selectedTask;
    private boolean original;
    //private Job setProposals;
	private StatusLineManager statusLineManager = new StatusLineManager();

	class TaskLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}

		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		public Image getImage(Object obj) {
			return AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/task_16.png").createImage();
		}
	}

    public TaskListDialog(Shell shell, Task taskSelected) {
        super(shell);
		storageService = LocalStorageService.getInstance();
		setSystems();
        this.taskSelected = taskSelected;
        task = taskSelected == null ? null : taskSelected.getName();
		setContentProvider(ArrayContentProvider.getInstance());
		setLabelProvider(new TaskLabelProvider());
		setTitle("Tasks");
		setMessage("Select task");
		setWidthInChars(70);
    }

	protected void setSystems() {
		Set<String> systemsList = TimesheetApp.getSubmissionSystems().keySet();
		if (systemsList.isEmpty()) {
			systemsList = new HashSet<String>();
			systemsList.add("Local");
		}
        systems = systemsList.toArray(new String[systemsList.size()]);
	}
    
    @Override
    protected Control createDialogArea(Composite composite) {
        Composite parent = (Composite) super.createDialogArea(composite);        
        
        setTimePanel(parent);
        
        Composite systemPanel = new Composite(parent, SWT.NONE);
        systemPanel.setLayout(new GridLayout(4, false));
        
        (new Label(systemPanel, SWT.NULL)).setText(getSystemText() +  " System: ");
        systemCombo = new Combo(systemPanel, SWT.READ_ONLY);
        systemCombo.setItems(systems);
        if (systems.length == 1) systemCombo.setEnabled(false);
        
        (new Label(systemPanel, SWT.NULL)).setText("Project: ");
        projectCombo = new Combo(systemPanel, SWT.READ_ONLY);
        
        setOriginalButton(parent);

        setComment(parent);

        if (taskSelected != null && taskSelected.getProject().getSystem() != null) {
        	for (int i = 0; i < systems.length; i++) {
        		if (taskSelected.getProject().getSystem().equals(systems[i])) {
                	systemCombo.select(i);
                	break;
        		}
        	}
        }
        else systemCombo.select(0);
        setTasksAndProjects();
        
        systemCombo.setBounds(50, 50, 180, 65);
        systemCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setTasksAndProjects();
            }
        });
        
        projectCombo.setBounds(50, 50, 180, 65);
        projectCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setTasks();
            }
        });
        statusLineManager.createControl(parent);
        return parent;
    }

	protected void setTimePanel(Composite parent) {
	}

	protected void setComment(Composite parent) {
	}

	protected void setOriginalButton(Composite parent) {
		Button originalButton = new Button(parent, SWT.CHECK);
        originalButton.setText("original");
        originalButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	original = ((Button) e.getSource()).getSelection();
                setTasksAndProjects();
            }
        });
	}

	protected String getSystemText() {
		return "Submission";
	}

	private void setTasksAndProjects() {
		systemSelected = systemCombo.getText();
		List<String> projectList;
		if (original) {
			submissionService = new ExtensionManager<SubmissionService>(SubmissionService.SERVICE_ID).getService(
					TimesheetApp.getSubmissionSystems().get(systemSelected));
			projectList = new ArrayList<String>(submissionService.getAssignedProjects().keySet());
		}
		else
			projectList = getInternalProjects();
		String[] projects = projectList.toArray(new String[projectList.size()]);
        projectCombo.setItems(projects);
        if (projects.length == 1) projectCombo.setEnabled(false);
        else projectCombo.setEnabled(true);
		if (taskSelected != null)
			projectSelected = taskSelected.getProject().getName();
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

	protected List<String> getInternalProjects() {
		return storageService.getProjects(systemSelected);
	}

	private void setTasks() {
		projectSelected = projectCombo.getText();
		if (original) {
			submissionService = new ExtensionManager<SubmissionService>(SubmissionService.SERVICE_ID).getService(
					TimesheetApp.getSubmissionSystems().get(systemSelected));
			assignedProjects = submissionService.getAssignedProjects();
			SubmissionProject submissionProject = assignedProjects.get(projectSelected);
			if (submissionProject == null) {
				projectSelected = assignedProjects.keySet().iterator().next();
				submissionProject = assignedProjects.get(projectSelected);
			}
			List<String> tasks = new ArrayList<String>(storageService.findTasksBySystemAndProject(systemSelected, projectSelected));
			tasksMap = new HashMap<String, SubmissionTask>();
			for (SubmissionTask task : submissionProject.getTasks()) {
				if (!tasks.contains(task.getName())) tasksMap.put(task.getName(), task);
			}
			getTableViewer().setInput(tasksMap.keySet());
		}
		else {
			List<String> tasks = getInternalTasks();
			getTableViewer().setInput(tasks);
			if (taskSelected != null && taskSelected.getProject().getName() != null && taskSelected.getProject().getName().equals(projectSelected) && taskSelected.getProject().getSystem().equals(systemSelected)) {
				for (int i = 0; i < tasks.size(); i++) {
					if (tasks.get(i).equals(this.taskSelected.getName())) {
						ISelection selection = new StructuredSelection(getTableViewer().getTable().getItem(i).getData());
						getTableViewer().setSelection(selection);
				        getTableViewer().getTable().setFocus();
				        break;
					}
				}
			}
		}
	}

	protected List<String> getInternalTasks() {
		return new ArrayList<String>(storageService.findTasksBySystemAndProject(systemSelected, projectSelected));
	}
	
	@Override
	protected void okPressed() {
		super.okPressed();
		//if (setProposals != null) setProposals.cancel();
	    if (selectedTask == null) {
	    	selectedTask = Arrays.toString(getResult());
	    	selectedTask = selectedTask.substring(selectedTask.indexOf("[") + 1, selectedTask.indexOf("]"));
	    }
		if (StringUtils.isEmpty(selectedTask)) return;
		if (original) {
			List<SubmissionProject> projects = new ArrayList<SubmissionProject>();
			SubmissionProject project = assignedProjects.get(projectSelected);
			project.setTasks(Collections.singletonList(tasksMap.get(selectedTask)));
			projects.add(project);
			storageService.importTasks(systemSelected, projects);
		}
	}

	/*@Override
	protected void cancelPressed() {
		super.cancelPressed();
		if (setProposals != null) setProposals.cancel();
	}*/

    public String getTask() {
		return selectedTask;
	}

	public String getSystem() {
		return systemSelected;
	}
	
	public String getProject() {
		return projectSelected;
	}
}