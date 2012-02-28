package com.uwusoft.timesheet.extensionpoint;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.uwusoft.timesheet.extensionpoint.model.SubmissionTask;

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
	public static final String USERNAME = "user.name";
	public static final String PASSWORD = "user.password";
	public static final String URL = "server.url";
	public static final String OPEN_BROWSER = "openbrowser";

    /**
     * @return {@link Map} of assigned tasks (value) for projects (key)<br>If there aren't any projects in the system all tasks have to be
     * assigned to an empty object
     */
	Map<String, Set<SubmissionTask>> getAssignedProjects();

	void submit(Date date, SubmissionTask task, Double total);
}
