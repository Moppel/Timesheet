package com.uwusoft.timesheet.commands;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
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

public class CheckinHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		String startTime = event.getParameter("Timesheet.commands.startTime");
		StorageService storageService = new ExtensionManager<StorageService>(StorageService.SERVICE_ID)
				.getService(preferenceStore.getString(StorageService.PROPERTY));
		Date startDate = new Date();
		try {
			if (startTime != null) startDate = StorageService.formatter.parse(startTime);
		} catch (ParseException e) {
			MessageBox.setError("Check in", e.getMessage());
		}			
		TaskListDialog listDialog = new TaskListDialog(Display.getDefault(),
				TimesheetApp.createTask(TimesheetApp.DEFAULT_TASK), startDate, "Check in");
		if (listDialog.open() == Dialog.OK) {
			if (Boolean.toString(Boolean.TRUE).equals(event.getParameter("Timesheet.commands.storeWeekTotal")))
				storageService.storeLastWeekTotal(preferenceStore.getString(TimesheetApp.WORKING_HOURS)); // store Week and Overtime
			storageService.createTaskEntry(new TaskEntry(listDialog.getTime(), new Task(StorageService.CHECK_IN)));
		    String selectedTask = Arrays.toString(listDialog.getResult());
		    selectedTask = selectedTask.substring(selectedTask.indexOf("[") + 1, selectedTask.indexOf("]"));
			if (StringUtils.isEmpty(selectedTask)) return null;
			TaskEntry task = new TaskEntry(null, new Task(selectedTask));
			task.getTask().setProject(new Project(listDialog.getProject(), listDialog.getSystem()));
			task.setComment(listDialog.getComment());
			storageService.createTaskEntry(task);
			storageService.openUrl(StorageService.OPEN_BROWSER_CHECKIN);
			ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI.getWorkbench().getService(ISourceProviderService.class);
			SessionSourceProvider commandStateService = (SessionSourceProvider) sourceProviderService.getSourceProvider(SessionSourceProvider.SESSION_STATE);
			commandStateService.setEnabled(true);
			commandStateService.setBreak(false);
		}
		return null;
	}
}
