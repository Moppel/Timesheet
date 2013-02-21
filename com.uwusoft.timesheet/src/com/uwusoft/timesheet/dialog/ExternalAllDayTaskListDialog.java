package com.uwusoft.timesheet.dialog;

import org.eclipse.swt.widgets.Shell;

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
}
