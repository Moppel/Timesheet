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
import org.eclipse.swt.widgets.Display;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.dialog.PreferencesDialog;
import com.uwusoft.timesheet.extensionpoint.AllDayTaskService;
import com.uwusoft.timesheet.jira3.Jira3IssueService;
import com.uwusoft.timesheet.model.AllDayTaskEntry;
import com.uwusoft.timesheet.model.Project;
import com.uwusoft.timesheet.model.Task;
import com.uwusoft.timesheet.submission.model.SubmissionProject;
import com.uwusoft.timesheet.submission.model.SubmissionTask;
import com.uwusoft.timesheet.util.BusinessDayUtil;

public class NimsAllDayTaskService extends Jira3IssueService implements	AllDayTaskService {	
	public static final String PROJECT = "project";
	public static final String FILTER = "filter";
	public static final String COMPONENT = "component";
	
	private Map<String, String> subTasks, subTaskIds;
	private Long projectId, filterId, componentId;
	private String projectName, projectKey, componentName;
	private SimpleDateFormat customFieldFormatter;

	public NimsAllDayTaskService() throws CoreException {
		super();
		String s = Activator.getDefault().getPreferenceStore().getString(AllDayTaskService.PREFIX + PROJECT);
		if (StringUtils.isEmpty(s)) { 
        	PreferencesDialog preferencesDialog;
        	do
        		preferencesDialog = new PreferencesDialog(Display.getDefault(), "com.uwusoft.timesheet.nimsalldaytaskservice.NimsAllDayTaskPreferencePage");
        	while (preferencesDialog.open() != Dialog.OK);
		}
		if (!StringUtils.isEmpty(s)) projectId = new Long(s);
		s = Activator.getDefault().getPreferenceStore().getString(AllDayTaskService.PREFIX + FILTER);
		if (!StringUtils.isEmpty(s)) filterId = new Long(s);
		s = Activator.getDefault().getPreferenceStore().getString(AllDayTaskService.PREFIX + COMPONENT);
		if (!StringUtils.isEmpty(s)) componentId = new Long(s);
		
		subTasks = new HashMap<String, String>();
		subTaskIds = new HashMap<String, String>();
		for (Object subTaskMap : getSubTaskIssueTypesForProject("" + projectId)) {
			@SuppressWarnings("unchecked")
			Map<String, String> subTask = (Map<String, String>) subTaskMap;
			subTasks.put(subTask.get("id"), subTask.get("name"));
			subTaskIds.put(subTask.get("name").replaceAll("\\s", "_").toLowerCase(), subTask.get("id"));
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
				try {
					if ("customfield_10230".equals(customField.get("customfieldId")))
						from = customFieldFormatter.parse((String) customField.get("values"));
					if ("customfield_10231".equals(customField.get("customfieldId")))
						to = customFieldFormatter.parse((String) customField.get("values"));
					/*if ("customfield_10234".equals(customField.get("customfieldId")))
						System.out.println("requested days: " + customField.get("values"));*/
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmdd");
        int left=26; // TODO parent issue (vacation sheet)?
        struct.put("summary", dateFormat.format(from) + "-" + dateFormat.format(to) + "_(" + left + "left)");
        struct.put("project", projectKey);
        struct.put("status", "10004");
        struct.put("votes", "0");
        struct.put("priority", "3");
        struct.put("type", subTaskIds.get(taskProperty));
        struct.put("created", new SimpleDateFormat("yyyy-mm-dd hh:mm:ss.S").format(new Date()));
        Hashtable<String, Object> components = new Hashtable<String, Object>();
        components.put("id", componentId);
        components.put("name", componentName);
        struct.put("components", makeVector(components));
        struct.put("affectsVersions", makeVector(new Hashtable<String, Object>()));
        struct.put("fixVersions", makeVector(new Hashtable<String, Object>()));
        Hashtable<String, Serializable> customField = new Hashtable<String, Serializable>();
        addCustomField(customField, "customfield_10230", customFieldFormatter.format(from));
        addCustomField(customField, "customfield_10231", customFieldFormatter.format(to));
        addCustomField(customField, "customfield_10234", "" + BusinessDayUtil.getRequestedDays(from, to));
        struct.put("customFieldValues", makeVector(customField));
        
        return createIssue(struct);
	}
	
	@Override
	public String getProjectKey() {
		return projectKey;
	}

	@Override
	public String getSystem() {
		return "NIMS";
	}
    
	private void addCustomField(Hashtable<String, Serializable> customField, String customFieldId, String value)
    {
        customField.put("customfieldId", customFieldId);
        customField.put("values", makeVector(value));
    }

    private Vector<Object> makeVector(Object p0)
    {
        Vector<Object> v = new Vector<Object>(1);
        v.add(p0);
        return v;
    }
}
