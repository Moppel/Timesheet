package com.uwusoft.timesheet.dialog;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class SubmissionDialog extends Dialog {
	private int weekNum;
	private Date startDate, endDate;

	public SubmissionDialog(Display display, int weekNum) {
		super(new Shell(display, SWT.NO_TRIM | SWT.ON_TOP));
		this.weekNum = weekNum;
    	Calendar cal = Calendar.getInstance();
    	cal.set(Calendar.WEEK_OF_YEAR, weekNum);        
    	cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
    	startDate = cal.getTime();
    	cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
    	endDate = cal.getTime();
	}

	@Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        RowLayout layout = new RowLayout();
        layout.center = true;
        layout.wrap = false;
        composite.setLayout(layout);
        
        Button leftButton = new Button(composite, SWT.PUSH);
        leftButton.setText("<< ");
        
        Label startDateLabel = new Label(composite, SWT.NONE);
        startDateLabel.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(startDate));        
        
        Button currentWeekButton = new Button(composite, SWT.PUSH);
        currentWeekButton.setText(" - ");

        Label endDateLabel = new Label(composite, SWT.NONE);
        endDateLabel.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(endDate));        

        Button rightButton = new Button(composite, SWT.PUSH);
        rightButton.setText(" >>");
        
        composite.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
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
