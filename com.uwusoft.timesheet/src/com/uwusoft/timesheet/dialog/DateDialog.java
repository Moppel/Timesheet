package com.uwusoft.timesheet.dialog;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class DateDialog extends Dialog {

	private String title;
	private int day, month, year;

	public DateDialog(Display display, String title, Date date) {
		super(new Shell(display, SWT.NO_TRIM | SWT.ON_TOP));
		this.title = title;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		day = calendar.get(Calendar.DAY_OF_MONTH);
		month = calendar.get(Calendar.MONTH);
		year = calendar.get(Calendar.YEAR);
	}

	@Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(1, false));
		DateTime dateEntry = new DateTime(composite, SWT.CALENDAR);
        dateEntry.setDate(year, month, day);
        dateEntry.addSelectionListener(new SelectionListener() {			
			public void widgetSelected(SelectionEvent e) {
				day = ((DateTime) e.getSource()).getDay();
				month = ((DateTime) e.getSource()).getMonth();
				year = ((DateTime) e.getSource()).getYear();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
        dateEntry.setFocus();
        return composite;
	}

	@Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(title);
    }

    public Date getDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.YEAR, year);
		return calendar.getTime();
    }
}
