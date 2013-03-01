package com.uwusoft.timesheet.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.commands.AllDayTaskFactory;
import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
import com.uwusoft.timesheet.model.Task;

public class InternalAllDayTaskListDialog extends TaskListDialog {
	private Map<String, Set<String>> projects;
	private Map<String, Set<String>> systems;
	
	public InternalAllDayTaskListDialog(Shell shell, Task taskSelected) {
		super(shell, taskSelected);
		projects = new HashMap<String, Set<String>>();
		systems = new HashMap<String, Set<String>>();
		for (String property : AllDayTaskFactory.getAllDayTasks()) {
			Task task = TimesheetApp.createTask(property);
			if (LocalStorageService.getAllDayTaskService().taskAvailable(property.substring(property.indexOf(".") + 1)))
				continue;
			if (!systems.containsKey(task.getProject().getSystem()))
				systems.put(task.getProject().getSystem(), new LinkedHashSet<String>());
			systems.get(task.getProject().getSystem()).add(task.getProject().getName());
			if (!projects.containsKey(task.getProject().getName()))
				projects.put(task.getProject().getName(), new LinkedHashSet<String>());
			projects.get(task.getProject().getName()).add(task.getName());
		}
		setTitle("Internal All Day Tasks");
	}

	@Override
	protected List<String> getInternalProjects() {
		return new ArrayList<String>(systems.get(systemSelected));
	}

	@Override
	protected List<String> getInternalTasks() {
		return new ArrayList<String>(projects.get(projectSelected));
	}

	@Override
	protected void setOriginalButton(Composite parent) {
	}
}
