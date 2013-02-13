package com.uwusoft.timesheet.nimsalldaytaskservice;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.extensionpoint.AllDayTaskService;
import com.uwusoft.timesheet.jira3.Jira3IssueService;
import com.uwusoft.timesheet.model.AllDayTaskEntry;
import com.uwusoft.timesheet.model.Project;
import com.uwusoft.timesheet.model.Task;
import com.uwusoft.timesheet.submission.model.SubmissionProject;
import com.uwusoft.timesheet.submission.model.SubmissionTask;

public class NimsAllDayTaskService extends Jira3IssueService implements	AllDayTaskService {	
	public static final String PREFIX = "alldaytask.";
	public static final String PROJECT = "project";
	public static final String FILTER = "filter";
	
	private Map<String, String> subTasks;
	private Long projectId, filterId;
	private String projectName;

	public NimsAllDayTaskService() throws CoreException {
		super();
		String s = Activator.getDefault().getPreferenceStore().getString(PREFIX + PROJECT);
		if (!StringUtils.isEmpty(s)) projectId = new Long(s);
		s = Activator.getDefault().getPreferenceStore().getString(PREFIX + FILTER);
		if (!StringUtils.isEmpty(s)) filterId = new Long(s);
		
		subTasks = new HashMap<String, String>();
		for (Object subTaskMap : getSubTaskIssueTypesForProject("" + projectId)) {
			@SuppressWarnings("unchecked")
			Map<String, String> subTask = (Map<String, String>) subTaskMap;
			subTasks.put(subTask.get("id"), subTask.get("name"));
		}
		for (Object projectMap : getProjects()) {
		    @SuppressWarnings("rawtypes")
			Map project =  (Map) projectMap;
		    if (new Long((String) project.get("id")).equals(projectId)) {
		    	projectName = (String) project.get("name");
		    	break;
		    }
		}
	}

	@Override
	public Collection<SubmissionProject> getAssignedProjects() {
		List<SubmissionProject> assignedProjects = new ArrayList<SubmissionProject>();
		SubmissionProject project = new SubmissionProject(projectId, projectName);
		for (Object subTaskMap : getSubTaskIssueTypesForProject("" + projectId)) {
			@SuppressWarnings("unchecked")
			Map<String, String> subTask = (Map<String, String>) subTaskMap;
			project.addTask(new SubmissionTask(new Long(subTask.get("id")), subTask.get("name"), project));
		}
		assignedProjects.add(project);
		return assignedProjects;
	}

	@Override
	public Collection<AllDayTaskEntry> getAllDayTaskEntries() {
		List<AllDayTaskEntry> allDayTaskEntries = new ArrayList<AllDayTaskEntry>();
		Project project = new Project(projectName, getSystem());
		for (Object issueMap : getIssuesFromFilter("" + filterId)) {
			@SuppressWarnings("rawtypes")
			Map issue = (Map) issueMap;
			Task task = new Task(subTasks.get(issue.get("type")), project);

			Object[] customFieldValues = (Object[]) issue.get("customFieldValues");
			Date from = null, to = null;
			for (Object customFieldMap : customFieldValues) {
				@SuppressWarnings("rawtypes")
				Map customField = (Map) customFieldMap;
				SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yy", Locale.US);
				try {
					if ("customfield_10230".equals(customField.get("customfieldId")))
						from = formatter.parse((String) customField.get("values"));
					if ("customfield_10231".equals(customField.get("customfieldId")))
						to = formatter.parse((String) customField.get("values"));
					/*if ("customfield_10234".equals(customField.get("customfieldId")))
						System.out.println("requested days: " + customField.get("values"));*/
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			allDayTaskEntries.add(new AllDayTaskEntry(from, to, (String) issue.get("key"), task));
		}
		return allDayTaskEntries;
	}
	
	@Override
	public String getSystem() {
		return "NIMS";
	}
}
