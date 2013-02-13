package com.uwusoft.timesheet.extensionpoint;

public interface IssueService {
	public static final String SERVICE_ID = "com.uwusoft.timesheet.issueservice";
	public static final String SERVICE_NAME = "issue";
	public static final String PROPERTY = "issue.system";

	Object[] getProjects();
	Object[] getSavedFilters();
}
