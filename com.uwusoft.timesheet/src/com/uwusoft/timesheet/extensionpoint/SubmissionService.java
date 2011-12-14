package com.uwusoft.timesheet.extensionpoint;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

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

    /**
     * @return {@link Map} of assigned tasks (value) for projects (key)<br>If there aren't any projects in the system all tasks have to be
     * assigned to an empty key ({@link StringUtils#EMPTY})
     */
	Map<String, List<String>> getAssignedProjects();

	void submit(Date date, String task, String project, Double total);
}
