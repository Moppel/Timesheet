package com.uwusoft.timesheet.commands;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

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
import com.uwusoft.timesheet.dialog.TimeDialog;
import com.uwusoft.timesheet.extensionpoint.StorageService;
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
		try {
			Date startDate;
			if (startTime == null) startDate = new Date();
			else startDate = StorageService.formatter.parse(startTime);
			
			TimeDialog timeDialog = new TimeDialog(Display.getDefault(), "Check in at "
					+ DateFormat.getDateInstance(DateFormat.SHORT).format(startDate),
					TimesheetApp.getTaskName(TimesheetApp.DEFAULT_TASK), startDate);
			if (timeDialog.open() == Dialog.OK) {
				if (Boolean.toString(Boolean.TRUE).equals(event.getParameter("Timesheet.commands.storeWeekTotal")))
					storageService.storeLastWeekTotal(preferenceStore.getString(TimesheetApp.WORKING_HOURS)); // store Week and Overtime
				storageService.createTaskEntry(new TaskEntry(timeDialog.getTime(), new Task(StorageService.CHECK_IN)));
				Task defaultTask = TimesheetApp.createTask(TimesheetApp.DEFAULT_TASK);
				TaskEntry defaultTaskEntry = new TaskEntry(timeDialog.getTime(), defaultTask);
				defaultTaskEntry.setDateTime(null);
				storageService.createTaskEntry(defaultTaskEntry);
				storageService.openUrl(StorageService.OPEN_BROWSER_CHECKIN);
				ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI.getWorkbench().getService(ISourceProviderService.class);
				SessionSourceProvider commandStateService = (SessionSourceProvider) sourceProviderService.getSourceProvider(SessionSourceProvider.SESSION_STATE);
				commandStateService.setEnabled(true);
				commandStateService.setBreak(false);
			}
		} catch (ParseException e) {
			MessageBox.setError("Check in", e.getMessage());
		}
		return null;
	}
}
