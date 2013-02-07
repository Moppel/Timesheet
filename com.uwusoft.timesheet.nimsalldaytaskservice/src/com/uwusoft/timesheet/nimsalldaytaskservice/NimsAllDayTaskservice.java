package com.uwusoft.timesheet.nimsalldaytaskservice;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;

import com.uwusoft.timesheet.extensionpoint.AllDayTaskService;
import com.uwusoft.timesheet.jira3.Jira3IssueService;
import com.uwusoft.timesheet.submission.model.SubmissionProject;

public class NimsAllDayTaskservice extends Jira3IssueService implements
		AllDayTaskService {

	public NimsAllDayTaskservice() throws CoreException {
		super();
	}

	@Override
	public Collection<SubmissionProject> getAssignedProjects() {
		// TODO Auto-generated method stub
		return null;
	}

}
