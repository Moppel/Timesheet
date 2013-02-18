package com.uwusoft.timesheet.extensionpoint;

import java.util.Collection;
import java.util.Date;

import com.uwusoft.timesheet.model.AllDayTaskEntry;
import com.uwusoft.timesheet.submission.model.SubmissionProject;

public interface AllDayTaskService {
	public static final String SERVICE_ID = "com.uwusoft.timesheet.alldaytaskservice";
	public static final String SERVICE_NAME = "alldaytask";
	public static final String PROPERTY = "alldaytask.system";
	public static final String PREFIX = SERVICE_NAME + ".";
	public static final String PROJECT = "project";

	String getProjectKey();
	
	String getProjectName();
	
	Collection<SubmissionProject> getAssignedProjects();
	
	Collection<AllDayTaskEntry> getAllDayTaskEntries();
	
	String createAllDayTaskEntry(String taskProperty, Date from, Date to);
	
	boolean updateAllDayTaskEntry(String taskProperty, Date from, Date to);
	
	String getSystem();
}
