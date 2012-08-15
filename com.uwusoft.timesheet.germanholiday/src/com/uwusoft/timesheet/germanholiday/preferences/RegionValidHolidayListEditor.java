package com.uwusoft.timesheet.germanholiday.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;

import com.uwusoft.timesheet.germanholiday.Messages;
import com.uwusoft.timesheet.preferences.AbstractListEditor;

public class RegionValidHolidayListEditor extends AbstractListEditor {
	private Map<String, String> holidays, holidayNames;

	public RegionValidHolidayListEditor(String name, String labelText, List<String> regionValidHolidaysList, Composite parent) {
		super(name, labelText, parent);
		holidays = new HashMap<String, String>();
		holidayNames = new HashMap<String, String>();
		for (String holiday : regionValidHolidaysList) {
			String localizedName = Messages.getString(holiday); 
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
