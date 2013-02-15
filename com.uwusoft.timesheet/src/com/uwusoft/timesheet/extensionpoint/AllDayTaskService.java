package com.uwusoft.timesheet.extensionpoint;

import java.util.Collection;

import com.uwusoft.timesheet.model.AllDayTaskEntry;
import com.uwusoft.timesheet.submission.model.SubmissionProject;

public interface AllDayTaskService {
	public static final String SERVICE_ID = "com.uwusoft.timesheet.alldaytaskservice";
	public static final String SERVICE_NAME = "alldaytask";
	public static final String PROPERTY = "alldaytask.system";
	public static final String PREFIX = SERVICE_NAME + ".";

	String getProjectKey();
	
	Collection<SubmissionProject> getAssignedProjects();
	
	Collection<AllDayTaskEntry> getAllDayTaskEntries();
	
	String getSystem();
}
