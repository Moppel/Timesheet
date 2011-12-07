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
	private String[] systems;
    private Combo systemCombo, projectCombo;
    private String taskSelected;

    public TaskListDialog(Shell shell, String taskSelected) {
        super(shell);
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		storageService = new ExtensionManager<StorageService>(
                StorageService.SERVICE_ID).getService(preferenceStore.getString(StorageService.PROPERTY));
		List<String> systemsList = storageService.getSystems();
        systems = systemsList.toArray(new String[systemsList.size()]);
    	this.taskSelected = taskSelected;
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
		List<String> projects = storageService.getProjects(systemCombo.getText());
        projectCombo.setItems(projects.toArray(new String[projects.size()]));
        projectCombo.select(0);
		setTasks();
	}

	private void setTasks() {
		List<String> tasks = new ArrayList<String>(storageService.findTasksBySystemAndProject(systemCombo.getText(), projectCombo.getText()));
		tasks.remove(taskSelected);
        getTableViewer().setInput(tasks);
	}
	
	public String getSystem() {
		return systemCombo.getText();
	}
	
	public String getProject() {
		return projectCombo.getText();
	}
}
