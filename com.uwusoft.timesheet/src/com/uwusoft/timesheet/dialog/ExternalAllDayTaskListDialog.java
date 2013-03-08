package com.uwusoft.timesheet.dialog;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.extensionpoint.AllDayTaskService;
import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
import com.uwusoft.timesheet.model.Task;

public class ExternalAllDayTaskListDialog extends TaskListDialog {
	private boolean showAll = false;

	public ExternalAllDayTaskListDialog(Shell shell, Task taskSelected) {
		super(shell, taskSelected);
		setTitle("External All Day Tasks");
	}

	public ExternalAllDayTaskListDialog(Shell shell, Task taskSelected, boolean showAll) {
		this(shell, taskSelected);
		this.showAll = showAll;
	}
	
	@Override
	protected void setSystems() {
		systems = new String[] {TimesheetApp.getDescriptiveName(Activator.getDefault().getPreferenceStore().getString(AllDayTaskService.PROPERTY),
				AllDayTaskService.SERVICE_NAME)};
	}

	@Override
	protected List<String> getInternalTasks() {
    	return new ArrayList<String>(getInternalTasks(showAll, taskSelected));
	}

	public static Set<String> getInternalTasks(boolean showAll, Task taskSelected) {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		Task vacationPlanningTask = TimesheetApp.createTask(AllDayTaskService.PREFIX + AllDayTaskService.VACATION_PLANNING_TASK);
		Set<String> allDayTasks = new LinkedHashSet<String>();
    	for (String task : LocalStorageService.getInstance().getAllDayTasks()) {
    		String property = AllDayTaskService.PREFIX + task.replaceAll("\\s", "_");
    		if (showAll || !taskSelected.getName().equals(task) && (vacationPlanningTask.getName().equals(task)
    				|| !StringUtils.isEmpty(preferenceStore.getString(property))))
    			allDayTasks.add(task);
    	}
    	return allDayTasks;
	}

	@Override
	protected void setOriginalButton(Composite parent) {
	}

	@Override
	protected String getSystemText() {
		return "Issue";
	}
}
