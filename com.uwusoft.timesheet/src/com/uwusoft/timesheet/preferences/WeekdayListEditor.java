package com.uwusoft.timesheet.preferences;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.widgets.Composite;

public class WeekdayListEditor extends AbstractListEditor {
	private Map<String, Integer> weekdays;
	private Map<Integer, String> weekdayNumbers;

	public WeekdayListEditor(String name, String labelText,	Composite parent) {
		super(name, labelText, parent);
		weekdays = new LinkedHashMap<String, Integer>();
		weekdayNumbers = new HashMap<Integer, String>();
		int i = 0;
		// TODO: add locale
        for (String weekDay : new DateFormatSymbols().getWeekdays()) {
        	weekdays.put(weekDay, i);
        	weekdayNumbers.put(i++, weekDay);
        }
        weekdays.remove(StringUtils.EMPTY);
	}
	
	@Override
	protected String getItem(String item) {
		return Integer.toString(weekdays.get(item));
	}

	@Override
	protected List<String> getListForDialog() {
		return new ArrayList<String>(weekdays.keySet());
	}

	@Override
	protected String getStringForToken(String token) {
		return weekdayNumbers.get(Integer.parseInt(token));
	}
}
