package com.uwusoft.timesheet.dialog;

import org.eclipse.swt.widgets.Shell;

import java.util.HashMap;
import java.util.Map;

import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.commands.AllDayTaskFactory;
import com.uwusoft.timesheet.model.Project;
import com.uwusoft.timesheet.model.Task;

public class InternalAllDayTaskListDialog extends TaskListDialog {
	private Map<String, Project> projects;
	
	public InternalAllDayTaskListDialog(Shell shell, Task taskSelected) {
		super(shell, taskSelected, false);
		projects = new HashMap<String, Project>();
		for (String property : AllDayTaskFactory.getAllDayTasks()) {
			Task task = TimesheetApp.createTask(property);
			if (!projects.containsKey(task.getProject().getName()))
				projects.put(task.getProject().getName(), task.getProject());
			else
				projects.get(task.getProject().getName()).addTask(task);
		}
	}


}
