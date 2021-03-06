package com.uwusoft.timesheet.dialog;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Text;

import com.uwusoft.timesheet.validation.DateTimeObservableValue;
import com.uwusoft.timesheet.validation.Period;
import com.uwusoft.timesheet.validation.PeriodValidator;

public class AllDayTaskDateDialog extends Dialog {

	private String title;
    private int fromDay, fromMonth, fromYear, toDay, toMonth, toYear;
	protected DateTime dateTimeStart, dateTimeEnd;
	private Text status;
	private final Period period;

	public AllDayTaskDateDialog(Display display, Date date) {
		this(display, "Date", date);
	}
    
	public AllDayTaskDateDialog(Display display, String title, Date date) {
		this(display, title, date, date);
	}

	public AllDayTaskDateDialog(Display display, String title, Date startDate, Date endDate) {
		super(new Shell(display, SWT.NO_TRIM | SWT.ON_TOP));
		this.title = title;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		fromDay = calendar.get(Calendar.DAY_OF_MONTH);
		fromMonth = calendar.get(Calendar.MONTH);
		fromYear = calendar.get(Calendar.YEAR);
		calendar.setTime(endDate);
		toDay = calendar.get(Calendar.DAY_OF_MONTH);
		toMonth = calendar.get(Calendar.MONTH);
		toYear = calendar.get(Calendar.YEAR);
		period = new Period(startDate, endDate);
	}
	
	@Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(getColumns(), false));
        
		(new Label(composite, SWT.NULL)).setText("From: ");
		createStartDatePart(composite);

		(new Label(composite, SWT.NULL)).setText("To: ");
        createEndDatePart(composite);
		
        setFocus();
        
        status = new Text(composite, SWT.NONE);
		status.setEnabled(false);
		status.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, getColumns(), 0));
		status.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (getButton(IDialogConstants.OK_ID) != null)
					getButton(IDialogConstants.OK_ID).setEnabled(Status.OK_STATUS.getMessage().equals(status.getText()));
			}
		});
		createDatabinding();
        return composite;
	}
	
	protected int getColumns() {
		return 2;
	}

	public Text getStatus() {
		return status;
	}

	public void setStatus(Text status) {
		this.status = status;
	}

	protected void createStartDatePart(Composite composite) {
		dateTimeStart = new DateTime(composite, SWT.CALENDAR);
		dateTimeStart.setDate(fromYear, fromMonth, fromDay);
		dateTimeStart.addSelectionListener(new SelectionListener() {			
			public void widgetSelected(SelectionEvent e) {
				fromDay = ((DateTime) e.getSource()).getDay();
				fromMonth = ((DateTime) e.getSource()).getMonth();
				fromYear = ((DateTime) e.getSource()).getYear();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	protected void createEndDatePart(Composite composite) {
		dateTimeEnd = new DateTime(composite, SWT.CALENDAR);
        dateTimeEnd.setDate(toYear, toMonth, toDay);
        dateTimeEnd.addSelectionListener(new SelectionListener() {			
			public void widgetSelected(SelectionEvent e) {
				toDay = ((DateTime) e.getSource()).getDay();
				toMonth = ((DateTime) e.getSource()).getMonth();
				toYear = ((DateTime) e.getSource()).getYear();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	protected void setFocus() {
		dateTimeEnd.setFocus();
	}

	private void createDatabinding() { // see http://eclipsesource.com/blogs/2009/02/27/databinding-crossvalidation-with-a-multivalidator/
		DateTimeObservableValue startObservable = new DateTimeObservableValue(dateTimeStart);
		DateTimeObservableValue endObservable = new DateTimeObservableValue(dateTimeEnd);

		DataBindingContext context = new DataBindingContext();

		// bind start and end
		UpdateValueStrategy modelToTarget = new UpdateValueStrategy(
				UpdateValueStrategy.POLICY_UPDATE);
		UpdateValueStrategy targetToModel = new UpdateValueStrategy(
				UpdateValueStrategy.POLICY_UPDATE);

		context.bindValue(startObservable, BeansObservables
				.observeValue(period, Period.PROP_START), targetToModel, modelToTarget);

		context.bindValue(endObservable, BeansObservables
				.observeValue(period, Period.PROP_END), targetToModel, modelToTarget);

		// bind status
		PeriodValidator periodValidator = new PeriodValidator(startObservable, endObservable);
		setId(periodValidator);

		modelToTarget = new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE);
		modelToTarget.setConverter(new Converter(IStatus.class, String.class) {

			@Override
			public Object convert(final Object arg) {
				if (arg instanceof IStatus) {
					IStatus status = (IStatus) arg;
					return status.getMessage();
				}
				return null;
			}

		});
		targetToModel = new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE);

		context.bindValue(SWTObservables.observeText(status), periodValidator
				.getValidationStatus(), targetToModel, modelToTarget);
	}

	protected void setId(PeriodValidator periodValidator) {
		periodValidator.setId(0L);
	}

	public Date getDate() {
		return getTo();
	}
    
	@Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(title);
        newShell.setSize(260, 425);
    }

    public Date getFrom() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, fromDay);
		calendar.set(Calendar.MONTH, fromMonth);
		calendar.set(Calendar.YEAR, fromYear);
		return DateUtils.truncate(calendar.getTime(), Calendar.DATE);
    }
    
    public Date getTo() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, toDay);
		calendar.set(Calendar.MONTH, toMonth);
		calendar.set(Calendar.YEAR, toYear);
		return DateUtils.truncate(calendar.getTime(), Calendar.DATE);
    }
}
