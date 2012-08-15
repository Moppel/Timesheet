package com.uwusoft.timesheet.germanholiday;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.dialog.PreferencesDialog;
import com.uwusoft.timesheet.extensionpoint.HolidayService;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.util.BusinessDayUtil;

public class GermanHolidayService implements HolidayService {
	public static final String PROPERTY = "holiday.german.valid";
	private static Map<Date, String> holidays = new HashMap<Date, String>();
	private static List<String> regionValidHolidays = new ArrayList<String>();
	public static final List<String> regionValidHolidaysList 
						= Arrays.asList(new String[] {"epiphany", "mariaAscension", "allSaintsDay", "dayOfRepentance", "corpusChristi"});
	
	public GermanHolidayService() {
        if (Activator.getDefault().getPreferenceStore().getString(PROPERTY) == "") {
        	PreferencesDialog preferencesDialog;
        	do
        		preferencesDialog = new PreferencesDialog(Display.getDefault(), "com.uwusoft.timesheet.germanholiday.preferences.GermanHolidayPreferencePage");
        	while (preferencesDialog.open() != Dialog.OK);
        }
	}

	@Override
	public List<Date> getOfflimitDates(int year) {
		holidays.clear();
		regionValidHolidays.clear();
		String regionValids = Activator.getDefault().getPreferenceStore().getString(PROPERTY);
		if (regionValids != null)
        	regionValidHolidays = Arrays.asList(regionValids.split(SubmissionService.separator));
		
		Calendar baseCalendar = GregorianCalendar.getInstance();
		baseCalendar.clear();

		// Add in the static dates for the year.
		storeHoliday(year, baseCalendar, Calendar.JANUARY, 1, "newYearsDay");
		storeRegionValidHoliday(year, baseCalendar, Calendar.JANUARY, 6, "epiphany");		
		storeHoliday(year, baseCalendar, Calendar.MAY, 1, "labourDay");
		storeRegionValidHoliday(year, baseCalendar, Calendar.AUGUST, 15, "mariaAscension");
		storeHoliday(year, baseCalendar, Calendar.OCTOBER, 3, "germanUnificationDay");
		storeHoliday(year, baseCalendar, Calendar.OCTOBER, 31, "reformationDay");
		storeRegionValidHoliday(year, baseCalendar, Calendar.NOVEMBER, 1, "allSaintsDay");

		// TODO: Buﬂ- und Bettag
		// Der letzte Mittwoch vor dem 23. November (letzter Sonntag nach Trinitatis)
		// Gets 3rd Wednesday in November
		String holiday = "dayOfRepentance";
		if (regionValidHolidays.contains(holiday))
			holidays.put(BusinessDayUtil.calculateFloatingHoliday(3, Calendar.WEDNESDAY, year, Calendar.NOVEMBER), holiday);		

		storeHoliday(year, baseCalendar, Calendar.DECEMBER, 25, "xmasDay");
		storeHoliday(year, baseCalendar, Calendar.DECEMBER, 26, "boxingDay");

		// Now deal with floating holidays.
		// Ostersonntag
		Date osterSonntag = BusinessDayUtil.getOsterSonntag(year);
		holidays.put(BusinessDayUtil.addDays(osterSonntag, -2), "goodFriday");
		holidays.put(BusinessDayUtil.addDays(osterSonntag, 1), "easterMonday");
		holidays.put(BusinessDayUtil.addDays(osterSonntag, 39), "ascensionDay");
		holidays.put(BusinessDayUtil.addDays(osterSonntag, 50), "whitMonday");
		
		holiday = "corpusChristi";
		if (regionValidHolidays.contains(holiday))
			holidays.put(BusinessDayUtil.addDays(osterSonntag, 60), holiday);

		return new ArrayList<Date>(holidays.keySet());
	}

	private void storeHoliday(int year, Calendar baseCalendar, int month, int date, String holiday) {
		baseCalendar.set(year, month, date);
		holidays.put(baseCalendar.getTime(), holiday);
	}
	
	private void storeRegionValidHoliday(int year, Calendar baseCalendar, int month, int date, String holiday) {
		if (regionValidHolidays.contains(holiday))
			storeHoliday(year, baseCalendar, month, date, holiday);
	}
	
	@Override
	public String getName(Date date) {
		return Messages.getString(holidays.get(date));
	}
}
