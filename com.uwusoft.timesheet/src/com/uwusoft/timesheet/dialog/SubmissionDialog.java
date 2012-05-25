package com.uwusoft.timesheet.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.uwusoft.timesheet.util.WeekComposite;

public class SubmissionDialog extends Dialog {
	private WeekComposite weekComposite;

	public SubmissionDialog(Display display, int currentWeekNum, int lastWeekNum) {
		super(new Shell(display, SWT.NO_TRIM | SWT.ON_TOP));
    	weekComposite = new WeekComposite(null, currentWeekNum, lastWeekNum);
	}

	@Override
    protected Control createDialogArea(final Composite parent) {
        final Composite composite = (Composite) super.createDialogArea(parent);
    	weekComposite.createComposite(new Composite(composite, SWT.NONE));
        return composite;
	}

	@Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Submission of week");
    }

    public int getWeekNum() {
		return weekComposite.getWeekNum();
	}
}
