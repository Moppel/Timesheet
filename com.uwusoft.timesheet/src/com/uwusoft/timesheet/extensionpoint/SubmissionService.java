package com.uwusoft.timesheet.extensionpoint;

import java.util.Date;
import java.util.List;

/**
 * todo: add class doc
 *
 * @author Uta Wunderlich
 * @version $Revision: $, $Date: Aug 17, 2011
 * @since Aug 17, 2011
 */
public interface SubmissionService {
	public static final String SERVICE_ID = "com.uwusoft.timesheet.submissionservice";
	public static final String SERVICE_NAME = "submission";
	public static final String PROPERTY = "submission.system";
	public static final String separator = ";";
	public static final String PROJECTS = "Projects";

    List<String> getAssignedTasks();
	
	void submit(Date date, String task, String project, Double total);
}
