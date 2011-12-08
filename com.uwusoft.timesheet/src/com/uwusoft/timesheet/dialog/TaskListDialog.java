package com.uwusoft.timesheet.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListDialog;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
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
	private String[] systems, tasks;
    private Combo systemCombo, projectCombo;
    private String projectSelected, systemSelected;

    public TaskListDialog(Shell shell, String taskSelected) {
        super(shell);
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		storageService = new ExtensionManager<StorageService>(
                StorageService.SERVICE_ID).getService(preferenceStore.getString(StorageService.PROPERTY));
		List<String> systemsList = storageService.getSystems();
        systems = systemsList.toArray(new String[systemsList.size()]);
        tasks = taskSelected.split(SubmissionService.separator);
    }
    
    @Override
    protected Control createDialogArea(Composite composite) {
        Composite parent = (Composite) super.createDialogArea(composite);
        
        Composite systemPanel = new Composite(parent, SWT.NONE);
        systemPanel.setLayout(new GridLayout(4, false));
        
        (new Label(systemPanel, SWT.NULL)).setText("Submission System: ");
        systemCombo = new Combo(systemPanel, SWT.READ_ONLY);
        systemCombo.setItems(systems);
        
        (new Label(systemPanel, SWT.NULL)).setText("Project: ");
        projectCombo = new Combo(systemPanel, SWT.READ_ONLY);

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
		List<String> projectList = storageService.getProjects(systemSelected);
		String[] projects = projectList.toArray(new String[projectList.size()]);
        projectCombo.setItems(projects);
        if (first && tasks.length > 1) {
        	for (int i = 0; i < projects.length; i++) {
        		if (tasks[1].equals(projects[i])) {
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
		List<String> tasks = new ArrayList<String>(storageService.findTasksBySystemAndProject(systemSelected, projectSelected));
		if (this.tasks.length > 1 && this.tasks[1].equals(projectSelected)) {
			if (this.tasks.length > 2 && this.tasks[2].equals(systemSelected)) tasks.remove(this.tasks[0]);
		}
        getTableViewer().setInput(tasks);
	}
	
	public String getSystem() {
		return systemSelected;
	}
	
	public String getProject() {
		return projectSelected;
	}
}
