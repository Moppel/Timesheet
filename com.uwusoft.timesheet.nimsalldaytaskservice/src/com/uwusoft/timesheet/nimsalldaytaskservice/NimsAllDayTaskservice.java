package com.uwusoft.timesheet.nimsalldaytaskservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;

import com.uwusoft.timesheet.extensionpoint.AllDayTaskService;
import com.uwusoft.timesheet.jira3.Jira3IssueService;
import com.uwusoft.timesheet.submission.model.SubmissionProject;
import com.uwusoft.timesheet.submission.model.SubmissionTask;

public class NimsAllDayTaskservice extends Jira3IssueService implements
		AllDayTaskService {

	public NimsAllDayTaskservice() throws CoreException {
		super();
	}

	@Override
	public Collection<SubmissionProject> getAssignedProjects() {
		List<SubmissionProject> assignedProjects = new ArrayList<SubmissionProject>();
		SubmissionProject project = new SubmissionProject("INT");
		project.addTask(new SubmissionTask("Vacation", project));
		assignedProjects.add(project);
		return assignedProjects;
	}
	
	public String getSystem() {
		return "NIMS";
	}
}
