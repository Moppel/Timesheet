
package com.uwusoft.timesheet.validation;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;

import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
import com.uwusoft.timesheet.model.AllDayTaskEntry;

public class PeriodValidator extends MultiValidator {

	private final IObservableValue start;

	private final IObservableValue end;
	
	private List<AllDayTaskEntry> allDayTaskEntries;

	public PeriodValidator(final IObservableValue start, final IObservableValue end) {
		this.start = start;
		this.end = end;
		allDayTaskEntries = LocalStorageService.getInstance().getAllDayTaskEntries();
	}

	@Override
	protected IStatus validate() {
		Date startDate = (Date) DateUtils.truncate(this.start.getValue(), Calendar.DATE);
		Date endDate = (Date) DateUtils.truncate(this.end.getValue(), Calendar.DATE);
		IStatus status = ValidationStatus.ok();

		if ((this.start != null) && (this.end != null)) {
			if (startDate.after(endDate)) {
				status = ValidationStatus.error("The start date has to be before the end date.");
				return status;
			}
			for (AllDayTaskEntry entry : allDayTaskEntries) {
				if (startDate.compareTo(entry.getFrom()) >= 0 && startDate.compareTo(entry.getTo()) <= 0) {
					status = ValidationStatus.error("The start date is within "
							+ (entry.getExternalId()== null ? "another entry" : entry.getExternalId() + "(Issue Key)"));
					return status;
				}
				if (endDate.compareTo(entry.getFrom()) >= 0 && endDate.compareTo(entry.getTo()) <= 0) {
					status = ValidationStatus.error("The end date is within "
							+ (entry.getExternalId()== null ? "another entry" : entry.getExternalId() + "(Issue Key)"));
					return status;
				}
			}
		}
		return status;
	}

}
