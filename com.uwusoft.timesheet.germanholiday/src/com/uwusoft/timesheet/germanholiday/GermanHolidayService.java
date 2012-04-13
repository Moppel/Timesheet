package com.uwusoft.timesheet.germanholiday;

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
	private static Map<Date, Holiday> holidays = new HashMap<Date, Holiday>();
	
	public GermanHolidayService() {
	}

	@Override
	public List<Date> getOfflimitDates(int year) {
		Calendar baseCalendar = GregorianCalendar.getInstance();
		baseCalendar.clear();

		// Add in the static dates for the year.
		baseCalendar.set(year, Calendar.JANUARY, 1);
		holidays.put(baseCalendar.getTime(), new Holiday("newYearsDay", true));

		baseCalendar.set(year, Calendar.JANUARY, 6);
		holidays.put(baseCalendar.getTime(), new Holiday("epiphany", false));
		
		baseCalendar.set(year, Calendar.MAY, 1);
		holidays.put(baseCalendar.getTime(), new Holiday("dayOfWork", true));

		baseCalendar.set(year, Calendar.AUGUST, 15);
		holidays.put(baseCalendar.getTime(), new Holiday("mariaAscension", false));

		baseCalendar.set(year, Calendar.OCTOBER, 3);
		holidays.put(baseCalendar.getTime(), new Holiday("germanUnificationDay", true));

		baseCalendar.set(year, Calendar.OCTOBER, 31);
		holidays.put(baseCalendar.getTime(), new Holiday("reformationDay", true));

		baseCalendar.set(year, Calendar.NOVEMBER, 1);
		holidays.put(baseCalendar.getTime(), new Holiday("allSaintsDay", false));

		baseCalendar.set(year, Calendar.DECEMBER, 25);
		holidays.put(baseCalendar.getTime(), new Holiday("xmasDay", true));

		baseCalendar.set(year, Calendar.DECEMBER, 26);
		holidays.put(baseCalendar.getTime(), new Holiday("boxingDay", true));

		// Now deal with floating holidays.
		// Ostersonntag
		Date osterSonntag = BusinessDayUtil.getOsterSonntag(year);
		//holidays.put(osterSonntag, "easter");
		holidays.put(BusinessDayUtil.addDays(osterSonntag, -2), new Holiday("goodFriday", true));
		holidays.put(BusinessDayUtil.addDays(osterSonntag, 1), new Holiday("easterMonday", true));
		holidays.put(BusinessDayUtil.addDays(osterSonntag, 39), new Holiday("ascensionDay", true));
		holidays.put(BusinessDayUtil.addDays(osterSonntag, 50), new Holiday("whitMonday", true));
		holidays.put(BusinessDayUtil.addDays(osterSonntag, 60), new Holiday("corpusChristi", false));

		// TODO: Buﬂ- und Bettag
		// Der letzte Mittwoch vor dem 23. November (letzter Sonntag nach Trinitatis)
		// Gets 3rd Wednesday in November
		holidays.put(BusinessDayUtil.calculateFloatingHoliday(3, Calendar.WEDNESDAY, year, Calendar.NOVEMBER),
				new Holiday("dayOfRepentance", false));		
		return new ArrayList<Date>(holidays.keySet());
	}
	
	@Override
	public boolean isValid(Date date) {
		return holidays.get(date).isValid();
	}
	
	private class Holiday {
		private String name;
		private boolean valid;
		
		public Holiday(String name, boolean valid) {
			this.name = name;
			this.valid = valid;
		}

		public String getName() {
			return name;
		}

		public boolean isValid() {
			return valid;
		}		
	}
}
