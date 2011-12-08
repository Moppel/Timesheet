package com.uwusoft.timesheet.preferences;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import com.uwusoft.timesheet.dialog.TaskListDialog;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;

public class TaskFieldEditor extends StringButtonFieldEditor {
	
	public TaskFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
        setChangeButtonText("Select task");
	}

	@Override
	protected String changePressed() {
		TaskListDialog listDialog = new TaskListDialog(getShell(), oldValue);
		listDialog.setTitle("Tasks");
		listDialog.setMessage("Select task");
		listDialog.setContentProvider(ArrayContentProvider.getInstance());
		listDialog.setLabelProvider(new LabelProvider());
		listDialog.setWidthInChars(70);
		if (listDialog.open() == Dialog.OK) {
		    String selectedTask = Arrays.toString(listDialog.getResult());
		    selectedTask = selectedTask.substring(selectedTask.indexOf("[") + 1, selectedTask.indexOf("]"));
			if (StringUtils.isEmpty(selectedTask)) return null;
			return selectedTask
					+ SubmissionService.separator + listDialog.getProject()
					+ SubmissionService.separator + listDialog.getSystem();
		}
		return null;
	}
}
