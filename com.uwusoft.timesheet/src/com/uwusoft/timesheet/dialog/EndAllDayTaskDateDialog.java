package com.uwusoft.timesheet.dialog;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.uwusoft.timesheet.validation.PeriodValidator;

public class EndAllDayTaskDateDialog extends AllDayTaskDateDialog {
	private String key;

	public EndAllDayTaskDateDialog(Display display, String title, String task, Date startDate, Date endDate, String key) {
		super(display, title, task, startDate, endDate);
		this.key = key;
	}

	@Override
	protected void createTaskPart(Composite composite) {
	}

	@Override
	protected void createStartDatePart(Composite composite) {
		dateTimeStart = new DateTime(composite, SWT.DATE);
		dateTimeStart.setEnabled(false);
	}

	@Override
	protected void setKey(PeriodValidator periodValidator) {
		periodValidator.setKey(key);
	}

	@Override
	public Date getDate() {
		return super.getTo();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
        newShell.setSize(260, 280);
	}
}
