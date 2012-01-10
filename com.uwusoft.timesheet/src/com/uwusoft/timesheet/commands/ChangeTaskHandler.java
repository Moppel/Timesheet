package com.uwusoft.timesheet.commands;

import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.dialog.TaskListDialog;
import com.uwusoft.timesheet.dialog.TimeDialog;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.util.ExtensionManager;

public class ChangeTaskHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		ILog logger = Activator.getDefault().getLog();
		StorageService storageService = new ExtensionManager<StorageService>(
				StorageService.SERVICE_ID).getService(preferenceStore.getString(StorageService.PROPERTY));
		TaskListDialog listDialog = new TaskListDialog(HandlerUtil.getActiveShell(event), preferenceStore.getString(TimesheetApp.LAST_TASK));
		listDialog.setTitle("Tasks");
		listDialog.setMessage("Select next task");
		listDialog.setContentProvider(ArrayContentProvider.getInstance());
		listDialog.setLabelProvider(new LabelProvider());
		listDialog.setWidthInChars(70);
		if (listDialog.open() == Dialog.OK) {
		    String selectedTask = Arrays.toString(listDialog.getResult());
		    selectedTask = selectedTask.substring(selectedTask.indexOf("[") + 1, selectedTask.indexOf("]"));
			if (StringUtils.isEmpty(selectedTask)) return null;
			TimeDialog timeDialog = new TimeDialog(Display.getDefault(), listDialog.getSystem() == null ? "Time" : "System " + listDialog.getSystem(),
									selectedTask + (listDialog.getProject() == null ? "" : " (" + listDialog.getProject() + ")"), new Date());
			if (timeDialog.open() == Dialog.OK) {
				storageService.createTaskEntry(TimesheetApp.createTask(timeDialog.getTime(), TimesheetApp.LAST_TASK));
				preferenceStore.setValue(TimesheetApp.LAST_TASK,
						TimesheetApp.buildProperty(selectedTask, listDialog.getProject(), listDialog.getSystem()));
	            logger.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "change task last task: " + preferenceStore.getString(TimesheetApp.LAST_TASK)));
				preferenceStore.setValue(TimesheetApp.SYSTEM_SHUTDOWN, StorageService.formatter.format(timeDialog.getTime()));
			}
		}						
		return null;
	}
}
