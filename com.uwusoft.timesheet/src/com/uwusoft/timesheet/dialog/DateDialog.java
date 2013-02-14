package com.uwusoft.timesheet.dialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
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

import com.uwusoft.timesheet.Messages;
import com.uwusoft.timesheet.commands.AllDayTaskFactory;

public class DateDialog extends Dialog {

	private String title, task;
    private int fromDay, fromMonth, fromYear, toDay, toMonth, toYear;
    private Combo taskCombo;
    private Map<String, String> allDayTaskTranslations;

	public DateDialog(Display display, String task, Date date) {
		this(display, "Date", task, date);
	}
    
	public DateDialog(Display display, String title, String task, Date date) {
		super(new Shell(display, SWT.NO_TRIM | SWT.ON_TOP));
		this.title = title;
		this.task = task;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		fromDay = calendar.get(Calendar.DAY_OF_MONTH);
		fromMonth = calendar.get(Calendar.MONTH);
		fromYear = calendar.get(Calendar.YEAR);
		toDay = calendar.get(Calendar.DAY_OF_MONTH);
		toMonth = calendar.get(Calendar.MONTH);
		toYear = calendar.get(Calendar.YEAR);
		allDayTaskTranslations = new HashMap<String, String>();
	}

	@Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(2, false));
        
        (new Label(composite, SWT.NULL)).setText("Task: ");
        taskCombo = new Combo(composite, SWT.READ_ONLY);
        List<String> allDayTasks = new ArrayList<String>();
        for (String allDayTask : AllDayTaskFactory.getAllDayTasks()) {
        	allDayTasks.add(Messages.getString(allDayTask));
        	allDayTaskTranslations.put(Messages.getString(allDayTask), allDayTask);
        }
        taskCombo.setItems(allDayTasks.toArray(new String[allDayTasks.size()]));
		for (int i = 0; i < allDayTasks.size(); i++) {
			if (task.equals(allDayTaskTranslations.get(allDayTasks.get(i)))) {
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
		DateTime dateEntry = new DateTime(composite, SWT.CALENDAR);
        dateEntry.setDate(fromYear, fromMonth, fromDay);
        dateEntry.addSelectionListener(new SelectionListener() {			
			public void widgetSelected(SelectionEvent e) {
				fromDay = ((DateTime) e.getSource()).getDay();
				fromMonth = ((DateTime) e.getSource()).getMonth();
				fromYear = ((DateTime) e.getSource()).getYear();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
        (new Label(composite, SWT.NULL)).setText("To: ");
		dateEntry = new DateTime(composite, SWT.CALENDAR);
        dateEntry.setDate(toYear, toMonth, toDay);
        dateEntry.addSelectionListener(new SelectionListener() {			
			public void widgetSelected(SelectionEvent e) {
				toDay = ((DateTime) e.getSource()).getDay();
				toMonth = ((DateTime) e.getSource()).getMonth();
				toYear = ((DateTime) e.getSource()).getYear();
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
		return allDayTaskTranslations.get(task) == null ? task : allDayTaskTranslations.get(task);
	}
    
    public Date getFrom() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, fromDay);
		calendar.set(Calendar.MONTH, fromMonth);
		calendar.set(Calendar.YEAR, fromYear);
		return calendar.getTime();
    }
    
    public Date getTo() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, toDay);
		calendar.set(Calendar.MONTH, toMonth);
		calendar.set(Calendar.YEAR, toYear);
		return calendar.getTime();
    }
}
