package com.uwusoft.timesheet.germanholidayservice;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.uwusoft.timesheet.extensionpoint.HolidayService;
import com.uwusoft.timesheet.util.BusinessDayUtil;

public class GermanHolidayService implements HolidayService {
	private static Map<Date, String> holidays = new HashMap<Date, String>();
	
	public GermanHolidayService() {
	}

	@Override
	public List<Date> getOfflimitDates(int year) {
		Calendar baseCalendar = GregorianCalendar.getInstance();
		baseCalendar.clear();

		// Add in the static dates for the year.
		// New years day
		baseCalendar.set(year, Calendar.JANUARY, 1);
		holidays.put(baseCalendar.getTime(), "newYearsDay");

		// Tag der Arbeit
		baseCalendar.set(year, Calendar.MAY, 1);
		holidays.put(baseCalendar.getTime(), "dayOfWork");

		// Tag der deutschen Einheit
		baseCalendar.set(year, Calendar.OCTOBER, 3);
		holidays.put(baseCalendar.getTime(), "germanUnificationDay");

		// Reformationstag
		baseCalendar.set(year, Calendar.OCTOBER, 31);
		holidays.put(baseCalendar.getTime(), "reformationDay");

		// Christmas
		baseCalendar.set(year, Calendar.DECEMBER, 25);
		holidays.put(baseCalendar.getTime(), "xmasDay");

		baseCalendar.set(year, Calendar.DECEMBER, 26);
		holidays.put(baseCalendar.getTime(), "boxingDay");

		// Now deal with floating holidays.
		// Ostersonntag
		Date osterSonntag = BusinessDayUtil.getOsterSonntag(year);
		holidays.put(osterSonntag, "easter");
		// Karfreitag
		holidays.put(BusinessDayUtil.addDays(osterSonntag, -2), "goodFriday");
		// Ostermontag
		holidays.put(BusinessDayUtil.addDays(osterSonntag, 1), "easterMonday");

		// Christi Himmelfahrt
		holidays.put(BusinessDayUtil.addDays(osterSonntag, 39), "ascensionDay");

		// Pfingstmontag
		holidays.put(BusinessDayUtil.addDays(osterSonntag, 50), "whitMonday");

		// TODO: Buﬂ- und Bettag
		// Der letzte Mittwoch vor dem 23. November (letzter Sonntag nach
		// Trinitatis)
		// Gets 3rd Wednesday in November
		holidays.put(BusinessDayUtil.calculateFloatingHoliday(3, Calendar.WEDNESDAY, year,
				Calendar.NOVEMBER), "dayOfRepentance");		
		return new ArrayList<Date>(holidays.keySet());
	}
}
