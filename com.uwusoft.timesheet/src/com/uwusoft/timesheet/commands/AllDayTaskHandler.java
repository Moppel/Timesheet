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
import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
import com.uwusoft.timesheet.model.AllDayTasks;
import com.uwusoft.timesheet.model.Task;
import com.uwusoft.timesheet.util.MessageBox;

public class AllDayTaskHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		AllDayTasks allDayTasks = AllDayTasks.getInstance();
		Date startDate = allDayTasks.getNextBegin(new Date());
		String task = event.getParameter("Timesheet.commands.task");
		DateDialog dateDialog;
		try {
			dateDialog = new DateDialog(Display.getDefault(), event.getCommand().getName(),	task, startDate);
			if (dateDialog.open() == Dialog.OK) {
				Task allDayTask = TimesheetApp.createTask(dateDialog.getTask());
				//LocalStorageService.getInstance().createAllDayTaskEntry(entry);
				//TODO wholeDayTasks.addNextTask(dateDialog.getTime(), dateDialog.getTask());
			}
		} catch (NotDefinedException e) {
			MessageBox.setError("All day task", e.getLocalizedMessage());
		}
		return null;
	}
}
