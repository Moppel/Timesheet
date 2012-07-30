package com.uwusoft.timesheet.commands;

import java.text.ParseException;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.ISourceProviderService;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.dialog.TaskListDialog;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.model.Project;
import com.uwusoft.timesheet.model.Task;
import com.uwusoft.timesheet.model.TaskEntry;
import com.uwusoft.timesheet.util.ExtensionManager;
import com.uwusoft.timesheet.util.MessageBox;

public class ChangeTaskHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		ILog logger = Activator.getDefault().getLog();
		String changeTime = event.getParameter("Timesheet.commands.changeTime");
		Date changeDate = new Date();
		if (changeTime != null)
			try {
				changeDate = StorageService.formatter.parse(changeTime);
			} catch (ParseException e) {
				MessageBox.setError("Change task", e.getMessage());
			}
		StorageService storageService = new ExtensionManager<StorageService>(
				StorageService.SERVICE_ID).getService(preferenceStore.getString(StorageService.PROPERTY));
		Task lastTask;
		TaskEntry lastTaskEntry = storageService.getLastTask();
		if (StorageService.BREAK.equals(lastTaskEntry.getTask().getName()))
			lastTask = TimesheetApp.createTask(TimesheetApp.DEFAULT_TASK);
		else
			lastTask = lastTaskEntry.getTask();
		TaskListDialog listDialog = new TaskListDialog(Display.getDefault(), lastTask, changeDate, "Change task");
		listDialog.setMessage("Select next task");
		if (listDialog.open() == Dialog.OK) {
		    String selectedTask = listDialog.getTask();
			if (StringUtils.isEmpty(selectedTask)) return null;
			storageService.updateTaskEntry(lastTaskEntry.getId(), listDialog.getTime(), true);
            logger.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "change last task: " + lastTask));
			TaskEntry task = new TaskEntry(null, new Task(selectedTask));
			
			ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI.getWorkbench().getService(ISourceProviderService.class);
			SessionSourceProvider commandStateService = (SessionSourceProvider) sourceProviderService.getSourceProvider(SessionSourceProvider.SESSION_STATE);
			if (listDialog.getProject() != null) {
				task.getTask().setProject(new Project(listDialog.getProject(), listDialog.getSystem()));
				commandStateService.setBreak(true); // currently the only task without project is the break
			}
			else
				commandStateService.setBreak(false);
			task.setComment(listDialog.getComment());
			storageService.createTaskEntry(task);				
			storageService.openUrl(StorageService.OPEN_BROWSER_CHANGE_TASK);
			commandStateService.setEnabled(true);
		}						
		return null;
	}
}
