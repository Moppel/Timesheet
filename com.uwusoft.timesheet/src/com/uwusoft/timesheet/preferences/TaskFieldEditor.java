package com.uwusoft.timesheet.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ListDialog;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.dialog.TaskListDialog;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.util.ExtensionManager;

public class TaskFieldEditor extends StringButtonFieldEditor {
	private String[] systems;
	
	public TaskFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		StorageService storageService = new ExtensionManager<StorageService>(
				StorageService.SERVICE_ID).getService(preferenceStore.getString(StorageService.PROPERTY));
        List<String> systems = new ArrayList<String>();
        int count=0;
        for (String system : storageService.getTasks().keySet()) {
            if (storageService.getTasks().get(system).isEmpty()) continue;
            systems.add(system);
            count++;
        }
        this.systems = systems.toArray(new String[count]);
        setChangeButtonText("Select task");
	}

	@Override
	protected String changePressed() {
		ListDialog listDialog = new TaskListDialog(getShell(), systems, oldValue);
		listDialog.setTitle("Tasks");
		listDialog.setMessage("Select next task");
		listDialog.setContentProvider(ArrayContentProvider.getInstance());
		listDialog.setLabelProvider(new LabelProvider());
		listDialog.setWidthInChars(70);
		if (listDialog.open() == Dialog.OK) {
		    String selectedTask = Arrays.toString(listDialog.getResult());
		    selectedTask = selectedTask.substring(selectedTask.indexOf("[") + 1, selectedTask.indexOf("]"));
			if (StringUtils.isEmpty(selectedTask)) return null;
			return selectedTask;
		}
		return null;
	}
}
