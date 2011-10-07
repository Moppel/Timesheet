package com.uwusoft.timesheet.commands;

import java.text.ParseException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.dialog.TimeDialog;
import com.uwusoft.timesheet.extensionpoint.StorageService;
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
			TimeDialog timeDialog = new TimeDialog(Display.getDefault(), "Check in at "	+ startTime,
					StorageService.CHECK_IN, TimesheetApp.formatter.parse(startTime));
			if (timeDialog.open() == Dialog.OK) {
				storageService.createTaskEntry(timeDialog.getTime(), StorageService.CHECK_IN);
				preferenceStore.setValue(TimesheetApp.LAST_TASK, preferenceStore.getString(TimesheetApp.DEFAULT_TASK));
			}
		} catch (ParseException e) {
			MessageBox.setError("Check in", e.getLocalizedMessage());
		}
		return null;
	}
}
