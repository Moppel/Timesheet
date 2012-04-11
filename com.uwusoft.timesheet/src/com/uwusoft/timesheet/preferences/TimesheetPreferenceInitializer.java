package com.uwusoft.timesheet.preferences;

import java.util.Calendar;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;

public class TimesheetPreferenceInitializer extends	AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(TimesheetApp.WORKING_HOURS, 40);
		store.setDefault(TimesheetApp.NON_WORKING_DAYS, Calendar.SATURDAY + SubmissionService.separator + Calendar.SUNDAY);
	}
}
