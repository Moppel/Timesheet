package com.uwusoft.timesheet.extensionpoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EmptyHolidayService implements HolidayService {

	@Override
	public List<Date> getOfflimitDates(int year) {
		return new ArrayList<Date>();
	}

	@Override
	public String getName(Date date) {
		return "";
	}
}
