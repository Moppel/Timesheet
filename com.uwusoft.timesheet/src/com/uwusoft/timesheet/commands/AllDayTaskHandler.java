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
import com.uwusoft.timesheet.dialog.DateDialog;
import com.uwusoft.timesheet.model.AllDayTasks;
import com.uwusoft.timesheet.util.MessageBox;

public class AllDayTaskHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		
		AllDayTasks wholeDayTasks = AllDayTasks.getInstance();
		Date startDate = wholeDayTasks.getNextBegin();
		String task = event.getParameter("Timesheet.commands.task");
		DateDialog dateDialog;
		try {
			dateDialog = new DateDialog(Display.getDefault(), event.getCommand().getName(),
					preferenceStore.getString(task), startDate);
			if (dateDialog.open() == Dialog.OK) {
				wholeDayTasks.addNextTask(dateDialog.getTime(), dateDialog.getTask());
			}
		} catch (NotDefinedException e) {
			MessageBox.setError("All day task", e.getLocalizedMessage());
		}
		return null;
	}
}
