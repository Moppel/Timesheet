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
import com.uwusoft.timesheet.model.TaskEntry;
import com.uwusoft.timesheet.model.WholeDayTasks;
import com.uwusoft.timesheet.util.ExtensionManager;
import com.uwusoft.timesheet.util.MessageBox;

public class CheckoutHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
        
		String shutdownTime = event.getParameter("Timesheet.commands.shutdownTime");
		Date shutdownDate;
		if (shutdownTime == null) shutdownDate = new Date();
		else
			try {
				shutdownDate = StorageService.formatter.parse(shutdownTime);
			} catch (ParseException e) {
				MessageBox.setError("Check out", e.getLocalizedMessage());
				return null;
			}
		StorageService storageService = new ExtensionManager<StorageService>(StorageService.SERVICE_ID)
				.getService(preferenceStore.getString(StorageService.PROPERTY));
		TaskEntry lastTask = storageService.getLastTask();
		TimeDialog timeDialog = new TimeDialog(Display.getDefault(), "Check out at " + DateFormat.getDateInstance(DateFormat.SHORT).format(shutdownDate),
				lastTask.getTask().display(), shutdownDate);
		if (timeDialog.open() == Dialog.OK) {
			storageService.updateTaskEntry(lastTask.getId(), timeDialog.getTime(), true);
			storageService.storeLastDailyTotal();
			WholeDayTasks.getInstance().createTaskEntries(timeDialog.getTime());
			preferenceStore.setValue(TimesheetApp.SYSTEM_SHUTDOWN, StorageService.formatter.format(timeDialog.getTime()));
			ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI.getWorkbench().getService(ISourceProviderService.class);
			SessionSourceProvider commandStateService = (SessionSourceProvider) sourceProviderService.getSourceProvider(SessionSourceProvider.SESSION_STATE);
			commandStateService.setEnabled(false);
			commandStateService.setBreak(false);
		}
		return null;
	}
}
