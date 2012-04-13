package com.uwusoft.timesheet.extensionpoint;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface HolidayService {
	public static final String SERVICE_ID = "com.uwusoft.timesheet.holidayservice";
	public static final String SERVICE_NAME = "holiday";
	public static final String PROPERTY = "holiday.system";
	
	/**
	 * Based on a year, this will compute the actual dates of Holidays
	 * @param year
	 * @return list of actual dates of Holidays
	 */
	List<Date> getOfflimitDates(int year);

	/**
	 * @param date
	 * @return <code>true</code> if date is valid holiday for the region
	 */
	boolean isValid(Date date);

	/**
	 * @param date
	 * @return localized name of holiday
	 */
	String getName(Date date);

	/**
	 * @return a collection of holidays that possibly are valid for a special region of the country
	 */
	Collection<String> getInvalidHolidays();

	/**
	 * @param name
	 * @return the localized name for the holiday
	 */
	String getName(String name);
}
