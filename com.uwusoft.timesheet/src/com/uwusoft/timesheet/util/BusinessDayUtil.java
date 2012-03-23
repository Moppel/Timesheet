package com.uwusoft.timesheet.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.eclipse.jface.preference.IPreferenceStore;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.model.Task;
import com.uwusoft.timesheet.model.TaskEntry;
import com.uwusoft.timesheet.model.WholeDayTasks;

public class BusinessDayUtil {

	private static transient Map<Integer, List<Date>> computedDates = new HashMap<Integer, List<Date>>();
	private static transient Task task = TimesheetApp.createTask(TimesheetApp.HOLIDAY_TASK);

	/*
	 * This method will calculate the next business day after the one input.
	 * This means that if the next day falls on a weekend or one of the
	 * following holidays then it will try the next day.
	 * 
	 * Holidays Accounted For: New Year's Day Christmas Day
	 */
	public static boolean isBusinessDay(Date dateToCheck) {
		// Setup the calendar to have the start date truncated
		Calendar baseCal = Calendar.getInstance();
		baseCal.setTime(DateUtils.truncate(dateToCheck, Calendar.DATE));

		List<Date> offlimitDates;

		// Grab the list of dates for the year. These SHOULD NOT be modified.
		synchronized (computedDates) {
			int year = baseCal.get(Calendar.YEAR);

			// If the map doesn't already have the dates computed, create them.
			if (!computedDates.containsKey(year))
				computedDates.put(year, getOfflimitDates(year));
			offlimitDates = computedDates.get(year);
		}

		// If it's on a holiday, increment and test again
		// If it's on a weekend, increment necessary amount and test again
		if (offlimitDates.contains(baseCal.getTime()) || isWeekend(dateToCheck))
			return false;
		else
			return true;
	}

	private static boolean isWeekend(Date dateToCheck) {
		// Setup the calendar to have the start date truncated
		Calendar baseCal = Calendar.getInstance();
		baseCal.setTime(DateUtils.truncate(dateToCheck, Calendar.DATE));
		// Determine if the date is on a weekend.
		int dayOfWeek = baseCal.get(Calendar.DAY_OF_WEEK);
		return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
	}

	/**
	 * 
	 * This method will calculate the next business day after the one input.
	 * This leverages the isBusinessDay heavily, so look at that documentation
	 * for further information.
	 * 
	 * @param startDate
	 *            the Date of which you need the next business day.
	 * @param createHoliday
	 *            TODO
	 * @return The next business day. I.E. it doesn't fall on a weekend, a
	 *         holiday or the official observance of that holiday if it fell on
	 *         a weekend.
	 * 
	 */
	public static Date getNextBusinessDay(Date startDate, boolean createHoliday) {
		// Increment the Date object by a Day and clear out hour/min/sec
		// information
		Date nextDay = DateUtils.truncate(addDays(startDate, 1), Calendar.DATE);
		// If tomorrow is a valid business day, return it
		if (isBusinessDay(nextDay)) {
			if (createHoliday) handleWeekChange(startDate, nextDay);
			return nextDay;
		}
		// Else we recursively call our function until we find one.
		else {
			if (createHoliday && !isWeekend(nextDay)) { // store holiday entry
				IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
				StorageService storageService = new ExtensionManager<StorageService>(
						StorageService.SERVICE_ID).getService(preferenceStore.getString(StorageService.PROPERTY));
				handleWeekChange(startDate, nextDay);
				TaskEntry taskEntry = new TaskEntry(nextDay, task, WholeDayTasks.getInstance().getTotal(), true);
				storageService.createTaskEntry(taskEntry);
			}
			return getNextBusinessDay(nextDay, createHoliday);
		}
	}

	/**
	 */
	public static void handleWeekChange(Date startDate, Date endDate) {
		Calendar calWeek = new GregorianCalendar();
		calWeek.setFirstDayOfWeek(Calendar.MONDAY);
		calWeek.setTime(startDate);
		int startWeek = calWeek.get(Calendar.WEEK_OF_YEAR);
		calWeek.setTime(endDate);
		int endWeek = calWeek.get(Calendar.WEEK_OF_YEAR);
		if (startWeek != endWeek) {
			IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
			StorageService storageService = new ExtensionManager<StorageService>(
					StorageService.SERVICE_ID).getService(preferenceStore.getString(StorageService.PROPERTY));
			storageService.storeLastWeekTotal(preferenceStore.getString(TimesheetApp.WORKING_HOURS));
		}
	}

	/*
	 * Based on a year, this will compute the actual dates of
	 * 
	 * Holidays Accounted For: New Year's Day Christmas Day
	 */
	private static List<Date> getOfflimitDates(int year) {
		List<Date> offlimitDates = new ArrayList<Date>();

		Calendar baseCalendar = GregorianCalendar.getInstance();
		baseCalendar.clear();

		// Add in the static dates for the year.
		// New years day
		baseCalendar.set(year, Calendar.JANUARY, 1);
		offlimitDates.add(baseCalendar.getTime());

		// Tag der deutschen Einheit
		baseCalendar.set(year, Calendar.OCTOBER, 3);
		offlimitDates.add(baseCalendar.getTime());

		// Reformationstag
		baseCalendar.set(year, Calendar.OCTOBER, 31);
		offlimitDates.add(baseCalendar.getTime());

		// Christmas
		baseCalendar.set(year, Calendar.DECEMBER, 25);
		offlimitDates.add(baseCalendar.getTime());

		baseCalendar.set(year, Calendar.DECEMBER, 26);
		offlimitDates.add(baseCalendar.getTime());

		// Now deal with floating holidays.
		// Ostersonntag
		Date osterSonntag = getOsterSonntag(year);
		offlimitDates.add(osterSonntag);
		// Karfreitag
		offlimitDates.add(addDays(osterSonntag, -2));
		// Ostermontag
		offlimitDates.add(addDays(osterSonntag, 1));

		// Christi Himmelfahrt
		offlimitDates.add(addDays(osterSonntag, 39));

		// Pfingstmontag
		offlimitDates.add(addDays(osterSonntag, 50));

		// TODO: Bu�- und Bettag
		// Der letzte Mittwoch vor dem 23. November (letzter Sonntag nach
		// Trinitatis)

		return offlimitDates;
	}

	/**
	 * This method will take in the various parameters and return a Date objet
	 * that represents that value.
	 * 
	 * Ex. To get Martin Luther Kings BDay, which is the 3rd Monday of January,
	 * the method call woudl be:
	 * 
	 * calculateFloatingHoliday(3, Calendar.MONDAY, year, Calendar.JANUARY);
	 * 
	 * Reference material can be found at:
	 * http://michaelthompson.org/technikos/holidays.php#MemorialDay
	 * 
	 * @param nth
	 *            0 for Last, 1 for 1st, 2 for 2nd, etc.
	 * @param dayOfWeek
	 *            Use Calendar.MODAY, Calendar.TUESDAY, etc.
	 * @param year
	 * @param month
	 *            Use Calendar.JANUARY, etc.
	 * @return
	 * 
	 *         private static Date calculateFloatingHoliday(int nth, int
	 *         dayOfWeek, int year, int month) { Calendar baseCal =
	 *         Calendar.getInstance(); baseCal.clear();
	 * 
	 *         //Determine what the very earliest day this could occur. //If the
	 *         value was 0 for the nth parameter, increment to the following
	 *         //month so that it can be subtracted alter. baseCal.set(year,
	 *         month + ((nth <= 0) ? 1 : 0), 1); Date baseDate =
	 *         baseCal.getTime();
	 * 
	 *         //Figure out which day of the week that this "earliest" could
	 *         occur on //and then determine what the offset is for our day that
	 *         we actually need. int baseDayOfWeek =
	 *         baseCal.get(Calendar.DAY_OF_WEEK); int fwd = dayOfWeek -
	 *         baseDayOfWeek;
	 * 
	 *         //Based on the offset and the nth parameter, we are able to
	 *         determine the offset of days and then //adjust our base date.
	 *         return addDays(baseDate, (fwd + (nth - (fwd >= 0 ? 1 : 0)) * 7));
	 *         }
	 */

	private static Date getOsterSonntag(int jahr) {
		int a, b, c, d, e, p, q, r, x, y, tag, monat;

		// Es geht um die Berechnung der Gr��en d und e
		// Dazu braucht man die 9 Hilfsgr��en a, b, c, p, n, q, r, x, y !!

		p = jahr / 100;

		q = p / 3;
		r = p / 4;

		x = (15 + p - q - r) % 30;
		y = (4 + p - r) % 7;

		a = jahr % 19;
		b = jahr % 4;
		c = jahr % 7;

		d = (19 * a + x) % 30;
		e = (2 * b + 4 * c + 6 * d + y) % 7;

		if (d == 29 && e == 6) {
			// => Ostern am 19.April
			tag = 19;
			monat = Calendar.APRIL;
		} else if (d == 28 && e == 6) {
			// => Ostern am 18.April
			tag = 18;
			monat = Calendar.APRIL;
		} else if (22 + d + e < 32) // ansonsten gilt
		{
			// => Ostern am (22+d+e).M�rz
			tag = 22 + d + e;
			monat = Calendar.MARCH;
		} else {
			// => Ostern am (d+e-9).April
			tag = d + e - 9;
			monat = Calendar.APRIL;
		}

		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(jahr, monat, tag);
		return cal.getTime();
	}

	/*
	 * If the given date falls on a weekend, the method will adjust to the
	 * closest weekday. I.E. If the date is on a Saturday, then the Friday will
	 * be returned, if it's a Sunday, then Monday is returned.
	 * 
	 * private static Date offsetForWeekend(Calendar baseCal) { Date returnDate
	 * = baseCal.getTime(); if (baseCal.get(Calendar.DAY_OF_WEEK) ==
	 * Calendar.SATURDAY) { /*if (log.isDebugEnabled())
	 * log.debug("Offsetting the Saturday by -1: " + returnDate);* return
	 * addDays(returnDate, -1); } else if (baseCal.get(Calendar.DAY_OF_WEEK) ==
	 * Calendar.SUNDAY) { /*if (log.isDebugEnabled())
	 * log.debug("Offsetting the Sunday by +1: " + returnDate);* return
	 * addDays(returnDate, 1); } else return returnDate; }
	 * 
	 * /** Private method simply adds
	 * 
	 * @param dateToAdd
	 * 
	 * @param numberOfDay
	 * 
	 * @return
	 */
	private static Date addDays(Date dateToAdd, int numberOfDay) {
		if (dateToAdd == null)
			throw new IllegalArgumentException("Date can't be null!");
		Calendar tempCal = Calendar.getInstance();
		tempCal.setTime(dateToAdd);
		tempCal.add(Calendar.DATE, numberOfDay);
		return tempCal.getTime();
	}
}
