package com.uwusoft.timesheet.extensionpoint;

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
	public List<Date> getOfflimitDates(int year);
}
