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

	public ExternalAllDayTaskListDialog(Shell shell, Task taskSelected) {
		super(shell, taskSelected);
		setTitle("External All Day Tasks");
	}

	@Override
	protected void setSystems() {
		String system = TimesheetApp.getDescriptiveName(Activator.getDefault().getPreferenceStore().getString(AllDayTaskService.PROPERTY),
				AllDayTaskService.SERVICE_NAME);
		systems = new String[] {system};
	}

	@Override
	protected List<String> getInternalTasks() {
		Set<String> allDayTasks = new LinkedHashSet<String>();
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
    	for (String task : LocalStorageService.getInstance().getAllDayTasks()) {
    		String property = AllDayTaskService.PREFIX + task.replaceAll("\\s", "_");
    		if (StringUtils.isEmpty(preferenceStore.getString(property))) continue;
    		allDayTasks.add(property.substring(property.indexOf(".") + 1));
    	}
    	return new ArrayList<String>(allDayTasks);
	}

	@Override
	protected void setOriginalButton(Composite parent) {
	}

	@Override
	protected String getSystemText() {
		return "Issue";
	}
}
