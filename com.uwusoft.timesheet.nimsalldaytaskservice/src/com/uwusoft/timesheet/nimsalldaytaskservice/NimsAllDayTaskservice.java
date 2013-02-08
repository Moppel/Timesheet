package com.uwusoft.timesheet.nimsalldaytaskservice;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;

import com.uwusoft.timesheet.extensionpoint.AllDayTaskService;
import com.uwusoft.timesheet.jira3.Jira3IssueService;
import com.uwusoft.timesheet.model.AllDayTaskEntry;
import com.uwusoft.timesheet.model.Project;
import com.uwusoft.timesheet.model.Task;
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

	@Override
	public Collection<AllDayTaskEntry> getAllDayTaskEntries() {
		List<AllDayTaskEntry> allDayTaskEntries = new ArrayList<AllDayTaskEntry>();
		Project project = new Project("INT", getSystem());
		Task task = new Task("Vacation", project);
		Calendar from = Calendar.getInstance();
		from.set(Calendar.DAY_OF_MONTH, 28);
		from.set(Calendar.MONTH, 3);
		from.set(Calendar.YEAR, 2013);
		allDayTaskEntries.add(new AllDayTaskEntry(from.getTime(), from.getTime(), task));
		from.set(Calendar.DAY_OF_MONTH, 21);
		from.set(Calendar.MONTH, 5);
		Calendar to = Calendar.getInstance();
		to.set(Calendar.DAY_OF_MONTH, 31);
		to.set(Calendar.MONTH, from.get(Calendar.MONTH));
		to.set(Calendar.YEAR, from.get(Calendar.YEAR));
		allDayTaskEntries.add(new AllDayTaskEntry(from.getTime(), to.getTime(), task));
		return allDayTaskEntries;
	}
	
	@Override
	public String getSystem() {
		return "NIMS";
	}
}
