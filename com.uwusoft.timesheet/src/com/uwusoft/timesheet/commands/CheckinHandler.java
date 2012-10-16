package com.uwusoft.timesheet.commands;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.ISourceProviderService;

import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.dialog.TaskListDialog;
import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.model.Project;
import com.uwusoft.timesheet.model.Task;
import com.uwusoft.timesheet.model.TaskEntry;
import com.uwusoft.timesheet.util.MessageBox;

public class CheckinHandler extends AbstractHandler {
	public static final String title = "Check in";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String startTime = event.getParameter("Timesheet.commands.startTime");
		LocalStorageService storageService = LocalStorageService.getInstance();
		Date startDate = new Date();
		try {
			if (startTime != null) startDate = StorageService.formatter.parse(startTime);
		} catch (ParseException e) {
			MessageBox.setError("Check in", e.getMessage());
		}			
		TaskListDialog listDialog = new TaskListDialog(Display.getDefault(),
				TimesheetApp.createTask(TimesheetApp.DEFAULT_TASK), startDate, title);
		if (listDialog.open() == Dialog.OK) {
			storageService.createTaskEntry(new TaskEntry(listDialog.getTime(), new Task(StorageService.CHECK_IN)));
		    String selectedTask = listDialog.getTask();
			if (StringUtils.isEmpty(selectedTask)) return null;
			TaskEntry task = new TaskEntry(null, new Task(selectedTask));
			task.getTask().setProject(new Project(listDialog.getProject(), listDialog.getSystem()));
			task.setComment(listDialog.getComment());
			storageService.createTaskEntry(task);
			storageService.openUrl(StorageService.OPEN_BROWSER_CHECKIN);
			storageService.synchronize();
			ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI.getWorkbench().getService(ISourceProviderService.class);
			SessionSourceProvider commandStateService = (SessionSourceProvider) sourceProviderService.getSourceProvider(SessionSourceProvider.SESSION_STATE);
			commandStateService.setEnabled(true);
			commandStateService.setBreak(false);
		}
		return null;
	}
}
