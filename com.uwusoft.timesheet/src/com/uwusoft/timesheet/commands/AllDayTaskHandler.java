package com.uwusoft.timesheet.commands;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.dialog.AllDayTaskDateDialog;
import com.uwusoft.timesheet.extensionpoint.AllDayTaskService;
import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
import com.uwusoft.timesheet.model.AllDayTaskEntry;
import com.uwusoft.timesheet.model.AllDayTasks;
import com.uwusoft.timesheet.model.Project;
import com.uwusoft.timesheet.model.Task;
import com.uwusoft.timesheet.util.MessageBox;

public class AllDayTaskHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		AllDayTasks allDayTasks = AllDayTasks.getInstance();
		Date startDate = allDayTasks.getNextBegin(new Date());
		String task = event.getParameter("Timesheet.commands.task");
		AllDayTaskDateDialog dateDialog;
		try {
			dateDialog = new AllDayTaskDateDialog(Display.getDefault(), event.getCommand().getName(), task, startDate);
			if (dateDialog.open() == Dialog.OK) {
				Date from = DateUtils.truncate(dateDialog.getFrom(), Calendar.DATE);
				Date to = DateUtils.truncate(dateDialog.getTo(), Calendar.DATE);
				String newTask = dateDialog.getTask().substring(dateDialog.getTask().indexOf(".") + 1, dateDialog.getTask().length()).replaceAll("_", " ");
				AllDayTaskEntry entry;
				String system = TimesheetApp.getDescriptiveName(Activator.getDefault().getPreferenceStore().getString(AllDayTaskService.PROPERTY),
						AllDayTaskService.SERVICE_NAME);
				if (LocalStorageService.getAllDayTaskService().taskAvailable(newTask))
					entry = new AllDayTaskEntry(from, to, new Task(newTask, new Project(LocalStorageService.getAllDayTaskService().getProjectName(), system)));
				else {
					entry = new AllDayTaskEntry(from, to, TimesheetApp.createTask(dateDialog.getTask()));
					entry.setSyncStatus(true);
				}
				LocalStorageService.getInstance().createAllDayTaskEntry(entry, true);
				LocalStorageService.getInstance().synchronizeAllDayTaskEntries();
			}
		} catch (NotDefinedException e) {
			MessageBox.setError("All day task", e.getLocalizedMessage());
		}
		return null;
	}
}
