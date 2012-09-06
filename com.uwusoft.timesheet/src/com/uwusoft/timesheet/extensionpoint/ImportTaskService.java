package com.uwusoft.timesheet.extensionpoint;

import java.util.Collection;
import java.util.List;


import com.uwusoft.timesheet.submission.model.SubmissionProject;

public interface ImportTaskService {
    public static final String CHECK_IN = "Check in";
    public static final String BREAK = "Break";
	
	public static final String PROPERTY_WEEK = "week";

    /**
     * @return a list of tasks assigned to name of special time management system
     */
    List<String> findTasksBySystemAndProject(String system, String project);

    boolean importTasks(String submissionSystem, Collection<SubmissionProject> projects);
}
