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
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.services.ISourceProviderService;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.dialog.TaskListDialog;
import com.uwusoft.timesheet.dialog.TimeDialog;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.model.Project;
import com.uwusoft.timesheet.model.Task;
import com.uwusoft.timesheet.model.TaskEntry;
import com.uwusoft.timesheet.util.ExtensionManager;

public class ChangeTaskHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		ILog logger = Activator.getDefault().getLog();
		StorageService storageService = new ExtensionManager<StorageService>(
				StorageService.SERVICE_ID).getService(preferenceStore.getString(StorageService.PROPERTY));
		ISourceProviderService sourceProviderService = (ISourceProviderService) HandlerUtil.getActiveWorkbenchWindow(event).getService(ISourceProviderService.class);
		CommandState commandStateService = (CommandState) sourceProviderService.getSourceProvider(CommandState.MY_STATE);
		TaskEntry lastTask = storageService.getLastTask();
		TaskListDialog listDialog = new TaskListDialog(HandlerUtil.getActiveShell(event), lastTask.getTask());
		listDialog.setTitle("Tasks");
		listDialog.setMessage("Select next task");
		listDialog.setWidthInChars(70);
		if (listDialog.open() == Dialog.OK) {
		    String selectedTask = Arrays.toString(listDialog.getResult());
		    selectedTask = selectedTask.substring(selectedTask.indexOf("[") + 1, selectedTask.indexOf("]"));
			if (StringUtils.isEmpty(selectedTask)) return null;
			TimeDialog timeDialog = new TimeDialog(Display.getDefault(), "Change Task",
									selectedTask + (listDialog.getProject() == null ? ""
											: " (" + listDialog.getProject() + ")") + (listDialog.getSystem() == null ? "" : "\nSystem: " + listDialog.getSystem()),
									new Date());
			if (timeDialog.open() == Dialog.OK) {
				storageService.updateTaskEntry(timeDialog.getTime(), lastTask.getId(), true);
				TaskEntry task = new TaskEntry(null, new Task(selectedTask));
				task.getTask().setProject(new Project(listDialog.getProject(), listDialog.getSystem()));
				task.setComment(listDialog.getComment());
				storageService.createTaskEntry(task);				
	            logger.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "change task last task: " + lastTask));
				preferenceStore.setValue(TimesheetApp.SYSTEM_SHUTDOWN, StorageService.formatter.format(timeDialog.getTime()));
				storageService.openUrl(StorageService.OPEN_BROWSER_CHANGE_TASK);
			}
		}						
		commandStateService.setEnabled(true);
		return null;
	}
}
