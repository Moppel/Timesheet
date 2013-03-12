package com.uwusoft.timesheet.dialog;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.uwusoft.timesheet.validation.PeriodValidator;

public class StartAllDayTaskDateDialog extends AllDayTaskDateDialog {
	private Long id;

	public StartAllDayTaskDateDialog(Display display, String title,	Date startDate, Date endDate, Long id) {
		super(display, title, startDate, endDate);
		this.id = id;
	}

	@Override
	protected void createEndDatePart(Composite composite) {
		dateTimeEnd = new DateTime(composite, SWT.DATE);
		dateTimeEnd.setEnabled(false);
	}

	@Override
	protected void setId(PeriodValidator periodValidator) {
		periodValidator.setId(id);
	}

	@Override
	public Date getDate() {
		return super.getFrom();
	}

	@Override
	protected void setFocus() {
		dateTimeStart.setFocus();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
        newShell.setSize(260, 280);
	}
}
