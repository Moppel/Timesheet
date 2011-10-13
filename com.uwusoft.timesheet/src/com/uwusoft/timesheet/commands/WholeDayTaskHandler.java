package com.uwusoft.timesheet.commands;

import java.util.Date;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.dialog.DateDialog;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.extensionpoint.model.WholeDayTasks;
import com.uwusoft.timesheet.util.MessageBox;

public class WholeDayTaskHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		
		WholeDayTasks wholeDayTasks = WholeDayTasks.getInstance();
		Date startDate = wholeDayTasks.getNextBegin();
		String task = event.getParameter("Timesheet.commands.task");
		DateDialog dateDialog;
		try {
			dateDialog = new DateDialog(Display.getDefault(), event.getCommand().getName(),
					preferenceStore.getString(task), startDate);
			if (dateDialog.open() == Dialog.OK) {
				wholeDayTasks.addNextTask(dateDialog.getTime(), preferenceStore.getString(task));
				preferenceStore.setValue(TimesheetApp.SYSTEM_SHUTDOWN, StorageService.formatter.format(System.currentTimeMillis()));
			}
		} catch (NotDefinedException e) {
			MessageBox.setError("Whole day task", e.getLocalizedMessage());
		}
		return null;
	}
}
