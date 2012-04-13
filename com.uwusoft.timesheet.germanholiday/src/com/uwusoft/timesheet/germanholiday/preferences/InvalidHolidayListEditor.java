package com.uwusoft.timesheet.germanholiday.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.extensionpoint.HolidayService;
import com.uwusoft.timesheet.preferences.AbstractListEditor;
import com.uwusoft.timesheet.util.ExtensionManager;

public class InvalidHolidayListEditor extends AbstractListEditor {
	private Map<String, String> holidays, holidayNames;
	private HolidayService holidayService;

	public InvalidHolidayListEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
		holidayService = new ExtensionManager<HolidayService>(
				HolidayService.SERVICE_ID).getService(Activator.getDefault().getPreferenceStore().getString(HolidayService.PROPERTY));
		holidays = new HashMap<String, String>();
		holidayNames = new HashMap<String, String>();
		for (String holiday : holidayService.getInvalidHolidays()) {
			String localizedName = holidayService.getName(holiday); 
			holidays.put(holiday, localizedName);
			holidayNames.put(localizedName, holiday);
		}
	}

	@Override
	protected String getItem(String item) {
		return holidayNames.get(item);
	}

	@Override
	protected List<String> getListForDialog() {
		return new ArrayList<String>(holidays.values());
	}

	@Override
	protected String getStringForToken(String token) {
		return holidays.get(token);
	}

}
