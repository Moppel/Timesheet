package com.uwusoft.timesheet.dialog;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class SubmissionDialog extends Dialog {
	private int weekNum;
	private Label startDateLabel, endDateLabel;
	private Button leftButton, rightButton;

	public SubmissionDialog(Display display, int weekNum) {
		super(new Shell(display, SWT.NO_TRIM | SWT.ON_TOP));
		this.weekNum = weekNum;
	}

	private void calculateStartEndDate(Composite composite) {
		Calendar cal = new GregorianCalendar();
    	cal.set(Calendar.WEEK_OF_YEAR, weekNum + 1);
    	cal.setFirstDayOfWeek(Calendar.MONDAY);
    	cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		startDateLabel.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(cal.getTime()));
		startDateLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
    	cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        endDateLabel.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(cal.getTime()));        
        endDateLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        leftButton.setEnabled(weekNum > 1);
        cal = new GregorianCalendar();
    	cal.setTime(new Date());
    	rightButton.setEnabled(weekNum < cal.get(Calendar.WEEK_OF_YEAR));
    	composite.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
	}

	@Override
    protected Control createDialogArea(final Composite parent) {
        final Composite composite = (Composite) super.createDialogArea(parent);
        RowLayout layout = new RowLayout();
        layout.center = true;
        layout.wrap = false;
        composite.setLayout(layout);
        
        leftButton = new Button(composite, SWT.PUSH);
        leftButton.setText("<<");
        leftButton.setEnabled(weekNum > 1);
        
        startDateLabel = new Label(composite, SWT.NONE);
        
        Button currentWeekButton = new Button(composite, SWT.PUSH);
        currentWeekButton.setText(" - ");
        
        endDateLabel = new Label(composite, SWT.NONE);

        rightButton = new Button(composite, SWT.PUSH);
        rightButton.setText(">>");
        Calendar cal = new GregorianCalendar();
    	cal.setTime(new Date());
    	rightButton.setEnabled(weekNum < cal.get(Calendar.WEEK_OF_YEAR));
        
        leftButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	weekNum = weekNum - 1;
            	calculateStartEndDate(composite);
            }
        });
        
    	currentWeekButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Calendar cal = new GregorianCalendar();
            	cal.setTime(new Date());        
            	weekNum = cal.get(Calendar.WEEK_OF_YEAR);
            	calculateStartEndDate(composite);
            }
        });

        rightButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	weekNum = weekNum + 1;
            	calculateStartEndDate(composite);
            }
        });
        
        calculateStartEndDate(composite);
        return composite;
	}

	@Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Submission of week");
    }

    public int getWeekNum() {
		return weekNum;
	}
}
