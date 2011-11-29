/* Copyright (c) 2005 by net-linx; All rights reserved */
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

    private String[] categories;
    private Combo categoryCombo;
    private String taskSelected;

    public TaskListDialog(Shell shell, String[] categories, String taskSelected) {
        super(shell);
        this.categories = categories;
    	this.taskSelected = taskSelected;
    }
    
    @Override
    protected Control createDialogArea(Composite composite) {
        Composite parent = (Composite) super.createDialogArea(composite);
        
        Composite categoryPanel = new Composite(parent, SWT.NONE);
        categoryPanel.setLayout(new GridLayout(2, false));
        
        (new Label(categoryPanel, SWT.NULL)).setText("Submission System: ");
        categoryCombo = new Combo(categoryPanel, SWT.READ_ONLY);
        categoryCombo.setItems(categories);
        
        categoryCombo.select(0);
        setTasks();
        
        categoryCombo.setBounds(50, 50, 180, 65);
        categoryCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setTasks();
            }
        });
        return parent;
    }

	private void setTasks() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		StorageService storageService = new ExtensionManager<StorageService>(
                StorageService.SERVICE_ID).getService(preferenceStore.getString(StorageService.PROPERTY));
		List<String> tasks = new ArrayList<String>(storageService.getTasks().get(categoryCombo.getText()));
		tasks.remove(taskSelected);
        getTableViewer().setInput(tasks);
	}
}
