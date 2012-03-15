package com.uwusoft.timesheet.dialog;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class DateDialog extends Dialog {

	private String title, task, date;
    private int day, month, year;

	public DateDialog(Display display, String task, Date date) {
		this(display, "Date", task, date);
	}
    
	public DateDialog(Display display, String title, String task, Date date) {
		super(new Shell(display, SWT.NO_TRIM | SWT.ON_TOP));
		this.date = DateFormat.getDateInstance(DateFormat.SHORT).format(date);
		this.title = title;
		this.task = task;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		day = calendar.get(Calendar.DAY_OF_MONTH);
		month = calendar.get(Calendar.MONTH);
		year = calendar.get(Calendar.YEAR);
	}

	@Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(2, false));
        
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 2;

        Label label = new Label(composite, SWT.NONE);
        label.setText(task);
        label.setLayoutData(gridData);
        //label.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);

		label = new Label(composite, SWT.NONE);
        label.setText("From:");
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		
        label = new Label(composite, SWT.NONE);
        label.setText("" + date);
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        
        label = new Label(composite, SWT.NONE);
        label.setText("To:");
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		
		DateTime dateEntry = new DateTime(composite, SWT.DATE);
		dateEntry.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        dateEntry.setDay(day);
        dateEntry.setMonth(month);
        dateEntry.setYear(year);
        //dateEntry.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
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
        dateEntry.setLayoutData(gridData);
        return composite;
	}

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(title);
    }
    
    public Date getTime() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.YEAR, year);
		return calendar.getTime();
    }
}
