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
	protected Map<String, Set<String>> projectMap;
	protected Map<String, Set<String>> systemMap;
	
	public InternalAllDayTaskListDialog(Shell shell, Task taskSelected) {
		super(shell, taskSelected);
		projectMap = new HashMap<String, Set<String>>();
		systemMap = new HashMap<String, Set<String>>();
		for (String property : AllDayTaskFactory.getAllDayTasks()) {
			Task task = TimesheetApp.createTask(property);
			if (LocalStorageService.getAllDayTaskService().taskAvailable(property.substring(property.indexOf(".") + 1))
					|| task.equals(taskSelected)) continue;
			if (!systemMap.containsKey(task.getProject().getSystem()))
				systemMap.put(task.getProject().getSystem(), new LinkedHashSet<String>());
			systemMap.get(task.getProject().getSystem()).add(task.getProject().getName());
			if (!projectMap.containsKey(task.getProject().getName()))
				projectMap.put(task.getProject().getName(), new LinkedHashSet<String>());
			projectMap.get(task.getProject().getName()).add(task.getName());
		}
		setTitle("Internal All Day Tasks");
	}

	@Override
	protected List<String> getInternalProjects() {
		return new ArrayList<String>(systemMap.get(systemSelected));
	}

	@Override
	protected List<String> getInternalTasks() {
		return new ArrayList<String>(projectMap.get(projectSelected));
	}

	@Override
	protected void setOriginalButton(Composite parent) {
	}
}
