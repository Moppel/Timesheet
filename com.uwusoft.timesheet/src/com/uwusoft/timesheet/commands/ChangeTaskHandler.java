package com.uwusoft.timesheet.commands;

import java.sql.Timestamp;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.ISourceProviderService;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.dialog.TaskListDialogWithTime;
import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.model.Task;
import com.uwusoft.timesheet.model.TaskEntry;
import com.uwusoft.timesheet.util.MessageBox;

public class ChangeTaskHandler extends AbstractHandler {
	private TaskListDialogWithTime listDialog;
	private LocalStorageService storageService = LocalStorageService.getInstance();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String changeTime = event.getParameter("Timesheet.commands.changeTime");
		Date changeDate = new Date();
		if (changeTime != null)
			try {
				changeDate = StorageService.formatter.parse(changeTime);
			} catch (ParseException e) {
				MessageBox.setError("Change task", e.getMessage());
			}
		boolean okPressed = changeTask(changeDate);
		if (okPressed) {
			storageService.openUrl(StorageService.OPEN_BROWSER_CHANGE_TASK);
			storageService.synchronize();
		}
		return null;
	}

	private boolean changeTask(Date changeDate) {
		ILog logger = Activator.getDefault().getLog();
		Task lastTask;
		TaskEntry lastTaskEntry = storageService.getLastTask();
		if (StorageService.BREAK.equals(lastTaskEntry.getTask().getName())) {
			lastTaskEntry = storageService.getLastTaskEntry();
			lastTask = lastTaskEntry == null ? TimesheetApp.createTask(TimesheetApp.DEFAULT_TASK)
					: lastTaskEntry.getTask();
		}
		else
			lastTask = lastTaskEntry == null ? TimesheetApp.createTask(TimesheetApp.DEFAULT_TASK) : lastTaskEntry.getTask();
		listDialog = new TaskListDialogWithTime(Display.getDefault(), lastTask, changeDate, "Change task");
		listDialog.setMessage("Select next task");
		if (listDialog.open() == Dialog.OK) {
		    String selectedTask = listDialog.getTask();
			if (StringUtils.isEmpty(selectedTask)) return false;
			lastTaskEntry.setDateTime(new Timestamp(listDialog.getTime().getTime()));
			storageService.updateTaskEntry(lastTaskEntry);
            logger.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "change last task: " + lastTaskEntry.getTask()));
			TaskEntry task = new TaskEntry(null, storageService.findTaskByNameProjectAndSystem(selectedTask, listDialog.getProject(), listDialog.getSystem()));
			
			ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI.getWorkbench().getService(ISourceProviderService.class);
			SessionSourceProvider commandStateService = (SessionSourceProvider) sourceProviderService.getSourceProvider(SessionSourceProvider.SESSION_STATE);
			if (listDialog.getProject() != null)
				commandStateService.setBreak(false);
			else
				commandStateService.setBreak(true); // currently the only task without project is the break
			task.setComment(listDialog.getComment());
			storageService.createTaskEntry(task);				
			commandStateService.setEnabled(true);
			return true;
		}
		return false;
	}
}
