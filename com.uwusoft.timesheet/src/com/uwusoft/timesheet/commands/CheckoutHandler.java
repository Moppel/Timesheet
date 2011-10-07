package com.uwusoft.timesheet.commands;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
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

public class CheckoutHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
        String shutdownTime = event.getParameter("Timesheet.commands.shutdownTime");
        if (shutdownTime == null) return null;
		try {
			Date shutdownDate = TimesheetApp.formatter.parse(shutdownTime);
			StorageService storageService = new ExtensionManager<StorageService>(StorageService.SERVICE_ID)
					.getService(preferenceStore.getString(StorageService.PROPERTY));
			TimeDialog timeDialog = new TimeDialog(Display.getDefault(), "Check out at " + DateFormat.getDateInstance(DateFormat.SHORT).format(shutdownDate),
					preferenceStore.getString(TimesheetApp.LAST_TASK), shutdownDate);
			if (timeDialog.open() == Dialog.OK) {
				storageService.createTaskEntry(timeDialog.getTime(), preferenceStore.getString(TimesheetApp.LAST_TASK));
				storageService.storeLastDailyTotal();
				preferenceStore.setValue(TimesheetApp.LAST_TASK, StringUtils.EMPTY);
				if (!StringUtils.isEmpty(preferenceStore.getString(TimesheetApp.DAILY_TASK)))
					storageService.createTaskEntry(timeDialog.getTime(), preferenceStore.getString(TimesheetApp.DAILY_TASK),
							preferenceStore.getString(TimesheetApp.DAILY_TASK_TOTAL));
				if (Boolean.toString(Boolean.TRUE).equals(event.getParameter("Timesheet.commands.storeWeekTotal")))
					storageService.storeLastWeekTotal(preferenceStore.getString(TimesheetApp.WORKING_HOURS)); // store Week and Overtime
			}
		} catch (ParseException e) {
			MessageBox.setError("Check out", e.getLocalizedMessage());
		}
		return null;
	}

}
