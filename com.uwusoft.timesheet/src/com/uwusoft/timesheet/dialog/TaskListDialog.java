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

        systemCombo.select(0);
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
        return parent;
    }

	private void setTasksAndProjects() {
		systemSelected = systemCombo.getText();
		List<String> projects = storageService.getProjects(systemSelected);
        projectCombo.setItems(projects.toArray(new String[projects.size()]));
        projectCombo.select(0);
		setTasks();
	}

	private void setTasks() {
		projectSelected = projectCombo.getText();
		List<String> tasks = new ArrayList<String>(storageService.findTasksBySystemAndProject(systemSelected, projectSelected));
		tasks.remove(this.tasks[0]); // TODO
        getTableViewer().setInput(tasks);
	}
	
	public String getSystem() {
		return systemSelected;
	}
	
	public String getProject() {
		return projectSelected;
	}
}
