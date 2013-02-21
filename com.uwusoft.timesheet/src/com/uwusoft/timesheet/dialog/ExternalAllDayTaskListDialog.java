package com.uwusoft.timesheet.dialog;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.extensionpoint.AllDayTaskService;
import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
import com.uwusoft.timesheet.model.Task;

public class ExternalAllDayTaskListDialog extends TaskListDialog {

	public ExternalAllDayTaskListDialog(Shell shell, Task taskSelected) {
		super(shell, taskSelected, false);
		setTitle("External All Day Tasks");
	}

	@Override
	protected void setSystems() {
		systems = new String[] {LocalStorageService.getInstance().getAllDayTaskService().getSystem()};
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
}
