
package com.uwusoft.timesheet.validation;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;

import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
import com.uwusoft.timesheet.model.AllDayTaskEntry;

public class PeriodValidator extends MultiValidator {

	private final IObservableValue start;
	private final IObservableValue end;
	
	private String key;
	
	private List<AllDayTaskEntry> allDayTaskEntries;

	public PeriodValidator(final IObservableValue start, final IObservableValue end) {
		this.start = start;
		this.end = end;
		allDayTaskEntries = LocalStorageService.getInstance().getAllDayTaskEntries();
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Override
	protected IStatus validate() {
		Date startDate = (Date) DateUtils.truncate(start.getValue(), Calendar.DATE);
		Date endDate = (Date) DateUtils.truncate(end.getValue(), Calendar.DATE);
		IStatus status = ValidationStatus.ok();

		if (startDate.after(endDate)) {
			status = ValidationStatus.error("The start date has to be before the end date.");
			return status;
		}
		for (AllDayTaskEntry entry : allDayTaskEntries) {
			if (key != null && key.equals(entry.getExternalId())) continue;
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
		return status;
	}

}
