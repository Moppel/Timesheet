package com.uwusoft.timesheet.nimsalldaytaskservice;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.dialog.PreferencesDialog;
import com.uwusoft.timesheet.extensionpoint.AllDayTaskService;
import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
import com.uwusoft.timesheet.jira3.Jira3IssueService;
import com.uwusoft.timesheet.model.AllDayTaskEntry;
import com.uwusoft.timesheet.model.Project;
import com.uwusoft.timesheet.model.Task;
import com.uwusoft.timesheet.submission.model.SubmissionProject;
import com.uwusoft.timesheet.submission.model.SubmissionTask;
import com.uwusoft.timesheet.util.BusinessDayUtil;

public class NimsAllDayTaskService extends Jira3IssueService implements	AllDayTaskService {	
	public static final String FILTER = "filter";
	public static final String COMPONENT = "component";
	
	private Map<String, String> subTasks, subTaskIds;
	private Long projectId, filterId, componentId;
	private String projectName, projectKey, componentName;
	private SimpleDateFormat customFieldFormatter, summaryFormatter, updatedFormatter;
	private int vacationLeft = 0;

	public NimsAllDayTaskService() throws CoreException {
		super();
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		String s = preferenceStore.getString(AllDayTaskService.PREFIX + PROJECT);
		if (StringUtils.isEmpty(s)) { 
        	PreferencesDialog preferencesDialog;
        	do
        		preferencesDialog = new PreferencesDialog(Display.getDefault(), "com.uwusoft.timesheet.nimsalldaytaskservice.NimsAllDayTaskPreferencePage");
        	while (preferencesDialog.open() != Dialog.OK);
		}
		s = preferenceStore.getString(AllDayTaskService.PREFIX + PROJECT);
		if (!StringUtils.isEmpty(s)) projectId = new Long(s);
		s = preferenceStore.getString(AllDayTaskService.PREFIX + FILTER);
		if (!StringUtils.isEmpty(s)) filterId = new Long(s);
		s = preferenceStore.getString(AllDayTaskService.PREFIX + COMPONENT);
		if (!StringUtils.isEmpty(s)) componentId = new Long(s);
		
		subTasks = new HashMap<String, String>();
		subTaskIds = new HashMap<String, String>();
		for (Object subTaskMap : getSubTaskIssueTypesForProject("" + projectId)) {
			@SuppressWarnings("unchecked")
			Map<String, String> subTask = (Map<String, String>) subTaskMap;
			subTasks.put(subTask.get("id"), subTask.get("name"));
			subTaskIds.put(subTask.get("name").replaceAll("\\s", "_"), subTask.get("id"));
		}
		for (Object projectMap : getProjects()) {
		    @SuppressWarnings("rawtypes")
			Map project =  (Map) projectMap;
		    if (new Long((String) project.get("id")).equals(projectId)) {
		    	projectName = (String) project.get("name");
		    	projectKey = (String) project.get("key");
		    	break;
		    }
		}
		for (Object componentMap : getComponents(projectKey)) {
			@SuppressWarnings("rawtypes")
			Map component = (Map) componentMap;
		    if (new Long((String) component.get("id")).equals(componentId))
		    	componentName = (String) component.get("name");
		}
		customFieldFormatter = new SimpleDateFormat("dd/MMM/yy", Locale.US);
		summaryFormatter = new SimpleDateFormat("yyyyMMdd", Locale.US);
		updatedFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
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
		String system = TimesheetApp.getDescriptiveName(Activator.getDefault().getPreferenceStore().getString(AllDayTaskService.PROPERTY),
				AllDayTaskService.SERVICE_NAME);
		Project project = new Project(projectName, system);
		for (Object issueMap : getIssuesFromFilter("" + filterId)) {
			@SuppressWarnings("rawtypes")
			Map issue = (Map) issueMap;
			String taskName = subTasks.get(issue.get("type"));
			if (taskName == null) { // Vacation Sheet
				int remainingLeave = 0;
				int vacationEntitlement = 0;
				Object[] customFieldValues = (Object[]) issue.get("customFieldValues");
				for (Object customFieldMap : customFieldValues) {
					@SuppressWarnings("rawtypes")
					Map customField = (Map) customFieldMap;
					if ("customfield_10232".equals(customField.get("customfieldId")))
						remainingLeave = new Integer((String) customField.get("values"));
					if ("customfield_10233".equals(customField.get("customfieldId")))
						vacationEntitlement = new Integer((String) customField.get("values"));
				}
				vacationLeft += remainingLeave + vacationEntitlement;
				continue;
			}
			Task task = new Task(taskName, project);

			Object[] customFieldValues = (Object[]) issue.get("customFieldValues");
			Date from = null, to = null;
			for (Object customFieldMap : customFieldValues) {
				@SuppressWarnings("rawtypes")
				Map customField = (Map) customFieldMap;
				try {
					if ("customfield_10230".equals(customField.get("customfieldId")))
						from = customFieldFormatter.parse((String) customField.get("values"));
					if ("customfield_10231".equals(customField.get("customfieldId")))
						to = customFieldFormatter.parse((String) customField.get("values"));
					if ("customfield_10234".equals(customField.get("customfieldId")))
						vacationLeft -= new Integer((String) customField.get("values"));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			allDayTaskEntries.add(new AllDayTaskEntry(from, to, (String) issue.get("key"), task, true));
		}
		return allDayTaskEntries;
	}
	
	public String createAllDayTaskEntry(String taskProperty, Date from, Date to) {
        Hashtable<String, Serializable> struct = new Hashtable<String, Serializable>();
        int requestedDays = BusinessDayUtil.getRequestedDays(from, to);
        vacationLeft -= requestedDays;
        struct.put("summary", getSummary(from, to, requestedDays, vacationLeft));
        struct.put("project", projectKey);
        struct.put("status", "10004");
        struct.put("votes", "0");
        struct.put("priority", "3");
        struct.put("type", subTaskIds.get(taskProperty));
        String created = updatedFormatter.format(new Date()) +".0";
        struct.put("created", created);
        struct.put("updated", created);
        Hashtable<String, Object> components = new Hashtable<String, Object>();
        components.put("id", componentId);
        components.put("name", componentName);
        struct.put("components", makeVector(components));
        struct.put("affectsVersions", makeVector(new Hashtable<String, Object>()));
        struct.put("fixVersions", makeVector(new Hashtable<String, Object>()));
        Vector<Object> vector = new Vector<Object>(3);
        addCustomField(vector, "customfield_10230", customFieldFormatter.format(from));
        addCustomField(vector, "customfield_10231", customFieldFormatter.format(to));
        addCustomField(vector, "customfield_10234", "" + requestedDays);
        struct.put("customFieldValues", vector);
        
        return createIssue(struct);
	}

	public boolean taskAvailable(String taskProperty) {
		return subTaskIds.get(taskProperty) != null;
	}

	@Override
	public boolean updateAllDayTaskEntry(String key, String taskProperty, Date from, Date to) {
        Hashtable<String, Serializable> struct = new Hashtable<String, Serializable>();
        int requestedDays = BusinessDayUtil.getRequestedDays(from, to);
        int requested = requestedDays;
		@SuppressWarnings("rawtypes")
		Map issue = getIssue(key);
		String summary = (String) issue.get("summary");
		int vacationLeft = Integer.parseInt(summary.substring(summary.indexOf("(") + 1, summary.indexOf("left)"))); 
		Object[] customFieldValues = (Object[]) issue.get("customFieldValues");
		for (Object customFieldMap : customFieldValues) {
			@SuppressWarnings("rawtypes")
			Map customField = (Map) customFieldMap;
			if ("customfield_10234".equals(customField.get("customfieldId")))
				requested = new Integer((String) customField.get("values"));
		}
		int requestedDiff = requestedDays - requested;
        vacationLeft -= requestedDiff;
        struct.put("summary", getSummary(from, to, requestedDays, vacationLeft));
        struct.put("type", subTaskIds.get(taskProperty));
        struct.put("updated", updatedFormatter.format(new Date()) +".0");
        Vector<Object> vector = new Vector<Object>(3);
        addCustomField(vector, "customfield_10230", customFieldFormatter.format(from));
        addCustomField(vector, "customfield_10231", customFieldFormatter.format(to));
        addCustomField(vector, "customfield_10234", "" + requestedDays);
        struct.put("customFieldValues", vector);
		
        boolean retValue = updateIssue(key, struct);
        if (/*retValue && */requestedDiff != 0)
        	for (String nextKey : LocalStorageService.getInstance().getFollowingVacationEntryKeys(to)) {
        		@SuppressWarnings("rawtypes")
        		Map nextIssue = getIssue(nextKey);
        		String nextSummary = (String) nextIssue.get("summary");
        		int nextVacationLeft = Integer.parseInt(nextSummary.substring(nextSummary.indexOf("(") + 1, nextSummary.indexOf("left)")));
        		nextVacationLeft -= requestedDiff;
                Hashtable<String, Serializable> nextStruct = new Hashtable<String, Serializable>();
                nextStruct.put("summary", nextSummary.replaceFirst("\\d*.left", nextVacationLeft + "left"));
        		if (!updateIssue(nextKey, nextStruct)) return false;
        	}
        return retValue;
	}

	private String getSummary(Date from, Date to, int requestedDays, int vacationLeft) {
		return summaryFormatter.format(from) + "-" + summaryFormatter.format(to) + "_" + requestedDays + (requestedDays > 1 ? "days" : "day")
        		+ "_(" + vacationLeft + "left)";
	}
	
	@Override
	public String getProjectName() {
		return projectName;
	}

	@Override
	public String getProjectKey() {
		return projectKey;
	}

	private void addCustomField(Vector<Object> vector, String customFieldId, String value)
    {
        Hashtable<String, Serializable> customField = new Hashtable<String, Serializable>();
        customField.put("customfieldId", customFieldId);
        customField.put("values", value);
        vector.add(customField);
    }

    private Vector<Object> makeVector(Object p0)
    {
        Vector<Object> v = new Vector<Object>(1);
        v.add(p0);
        return v;
    }
}
