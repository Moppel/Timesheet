package com.uwusoft.timesheet.dialog;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class TimeDialog extends Dialog {

	private String title, task;
    private int day, month, year, hours, minutes;
	
	public TimeDialog(Display display, String task, Date time) {
		this(display, "Time", task, time);
	}
    
    public TimeDialog(Display display, String title, String task, Date time) {
		super(new Shell(display, SWT.NO_TRIM | SWT.ON_TOP));
		this.title = title;
		this.task = task;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(time);
		day = calendar.get(Calendar.DAY_OF_MONTH);
		month = calendar.get(Calendar.MONTH);
		year = calendar.get(Calendar.YEAR);
		hours = calendar.get(Calendar.HOUR_OF_DAY);
		minutes = calendar.get(Calendar.MINUTE);		
	}

	@Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(1, false));
        
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.CENTER;

        Label label = new Label(composite, SWT.NONE);
        label.setText(task);
        label.setLayoutData(gridData);

        /*DateTime dateEntry = new DateTime(composite, SWT.DATE);
        dateEntry.addSelectionListener(new SelectionListener() {			
			public void widgetSelected(SelectionEvent e) {
				day = ((DateTime) e.getSource()).getDay();
				month = ((DateTime) e.getSource()).getMonth();
				year = ((DateTime) e.getSource()).getYear();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});*/

        DateTime timeEntry = new DateTime(composite, SWT.TIME | SWT.SHORT);
        timeEntry.setLayoutData(gridData);
        timeEntry.setHours(hours);
        timeEntry.setMinutes(minutes);
        timeEntry.addSelectionListener(new SelectionListener() {			
			public void widgetSelected(SelectionEvent e) {
				hours = ((DateTime) e.getSource()).getHours();
				minutes = ((DateTime) e.getSource()).getMinutes();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
        timeEntry.setFocus();
        return composite;
    }

    @Override
    protected Point getInitialSize() {
        return new Point(220, 120);
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
		calendar.set(Calendar.HOUR_OF_DAY, hours);
		calendar.set(Calendar.MINUTE, minutes);
		return calendar.getTime();
    }
}
