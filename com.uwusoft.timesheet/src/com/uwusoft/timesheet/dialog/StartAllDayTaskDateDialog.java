package com.uwusoft.timesheet.dialog;

import java.util.Date;

import org.eclipse.swt.widgets.Display;

public class StartAllDayTaskDateDialog extends AllDayTaskDateDialog {

	public StartAllDayTaskDateDialog(Display display, String title,	String task, Date date) {
		super(display, title, task, date);
	}

	@Override
	protected void setEnabledAndFocus() {
		taskCombo.setEnabled(false);
		dateTimeEnd.setEnabled(false);
		dateTimeStart.setFocus();
	}

	@Override
	public Date getDate() {
		return super.getFrom();
	}	
}
