package com.uwusoft.timesheet.dialog;

import java.util.Date;

import org.eclipse.swt.widgets.Display;

public class EndAllDayTaskDateDialog extends AllDayTaskDateDialog {

	public EndAllDayTaskDateDialog(Display display, String title, String task, Date date) {
		super(display, title, task, date);
	}

	@Override
	protected void setEnabledAndFocus() {
		taskCombo.setEnabled(false);
		dateTimeStart.setEnabled(false);
		dateTimeEnd.setFocus();
	}
}
