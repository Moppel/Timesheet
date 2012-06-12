package com.uwusoft.timesheet.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class WeekComposite {
	private int currentWeekNum, lastWeekNum;
	private Date startDate, endDate;
	private Label startDateLabel, endDateLabel;
	private Button leftButton, rightButton;
	private PropertyChangeListener listener;
	private Composite composite;
	
	public WeekComposite(PropertyChangeListener listener, int currentWeekNum, int lastWeekNum) {
		this.listener = listener;
		this.currentWeekNum = currentWeekNum;
		this.lastWeekNum = lastWeekNum;
	}

	public Composite createComposite(final Composite composite) {
		this.composite = composite;
        RowLayout layout = new RowLayout();
        layout.center = true;
        layout.wrap = false;
        composite.setLayout(layout);
        
		leftButton = new Button(composite, SWT.PUSH);
        leftButton.setText("<<");
        leftButton.setEnabled(currentWeekNum > 1);
        
        startDateLabel = new Label(composite, SWT.NONE);
        
        Button currentWeekButton = new Button(composite, SWT.PUSH);
        currentWeekButton.setText(" - ");
        
        endDateLabel = new Label(composite, SWT.NONE);

        rightButton = new Button(composite, SWT.PUSH);
        rightButton.setText(">>");
    	rightButton.setEnabled(currentWeekNum < lastWeekNum);
        
        leftButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	currentWeekNum = currentWeekNum - 1;
            	calculateStartEndDate(true);
            }
        });
        
    	currentWeekButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Calendar cal = new GregorianCalendar();
            	cal.setTime(new Date());        
            	currentWeekNum = cal.get(Calendar.WEEK_OF_YEAR);
            	calculateStartEndDate(true);
            }
        });

        rightButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	currentWeekNum = currentWeekNum + 1;
            	calculateStartEndDate(true);
            }
        });
        
        calculateStartEndDate(false);
        return composite;		
	}

	private void calculateStartEndDate(boolean firePropertyChange) {
		Calendar cal = new GregorianCalendar();
    	cal.set(Calendar.WEEK_OF_YEAR, currentWeekNum + 1);
    	cal.setFirstDayOfWeek(Calendar.MONDAY);
    	cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
    	startDate = cal.getTime();
		startDateLabel.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(startDate));
    	cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
    	endDate = cal.getTime();
        endDateLabel.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(endDate));        
        leftButton.setEnabled(currentWeekNum > 1);
    	rightButton.setEnabled(currentWeekNum < lastWeekNum);
    	composite.pack();
    	if (listener != null && firePropertyChange) listener.propertyChange(new PropertyChangeEvent(this, "weekNum", null, currentWeekNum));
	}
    
	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public int getWeekNum() {
		return currentWeekNum;
	}

	public void setCurrentWeekNum(int currentWeekNum) {
		this.currentWeekNum = currentWeekNum;
		calculateStartEndDate(false);
	}
}
