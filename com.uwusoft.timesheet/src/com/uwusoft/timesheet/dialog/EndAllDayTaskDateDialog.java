package com.uwusoft.timesheet.dialog;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.uwusoft.timesheet.validation.PeriodValidator;

public class EndAllDayTaskDateDialog extends AllDayTaskDateDialog {
	private Long id;

	public EndAllDayTaskDateDialog(Display display, String title, String task, Date startDate, Date endDate, Long id) {
		super(display, title, task, startDate, endDate);
		this.id = id;
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
	protected void setId(PeriodValidator periodValidator) {
		periodValidator.setId(id);
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
