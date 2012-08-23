package com.uwusoft.timesheet.dialog;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.model.AllDayTasks;

public class DateDialog extends Dialog {

	private String title, task, date;
    private int day, month, year;
    private Combo taskCombo;

	public DateDialog(Display display, String task, Date date) {
		this(display, "Date", task, date);
	}
    
	public DateDialog(Display display, String title, String task, Date date) {
		super(new Shell(display, SWT.NO_TRIM | SWT.ON_TOP));
		this.date = DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
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
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(2, false));
        
        (new Label(composite, SWT.NULL)).setText("Task: ");
        taskCombo = new Combo(composite, SWT.READ_ONLY);
        List<String> allDayTasks = new ArrayList<String>();
        for (String allDayTask : AllDayTasks.allDayTasks)
        	allDayTasks.add(preferenceStore.getString(allDayTask));
        taskCombo.setItems(allDayTasks.toArray(new String[allDayTasks.size()]));
		for (int i = 0; i < allDayTasks.size(); i++) {
			if (task.equals(allDayTasks.get(i))) {
				taskCombo.select(i);
				break;
			}
		}
		taskCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                task = taskCombo.getText();
            }
        });
		
		(new Label(composite, SWT.NULL)).setText("From: ");
        (new Label(composite, SWT.NULL)).setText("" + date);       
        (new Label(composite, SWT.NULL)).setText("To: ");
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

	public String getTask() {
		return task;
	}
    
    public Date getTime() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.YEAR, year);
		return calendar.getTime();
    }
}
