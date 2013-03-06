package com.uwusoft.timesheet.nimsalldaytaskservice;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.extensionpoint.AllDayTaskService;
import com.uwusoft.timesheet.extensionpoint.IssueService;
import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
import com.uwusoft.timesheet.preferences.TaskFieldEditor;
import com.uwusoft.timesheet.submission.model.SubmissionProject;
import com.uwusoft.timesheet.submission.model.SubmissionTask;
import com.uwusoft.timesheet.util.ExtensionManager;

public class NimsAllDayTaskPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public NimsAllDayTaskPreferencePage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("All Day Task preferences");
		getPreferenceStore().addPropertyChangeListener(this);
	}

	@Override
	protected void createFieldEditors() {
		addField(new ComboFieldEditor(AllDayTaskService.PREFIX + NimsAllDayTaskService.PROJECT, "Project:", getProjectArray(),
				getFieldEditorParent()));
		addField(new ComboFieldEditor(AllDayTaskService.PREFIX + NimsAllDayTaskService.FILTER, "Filter:", getFilterArray(),
				getFieldEditorParent()));
		if (StringUtils.isEmpty(getPreferenceStore().getString(AllDayTaskService.PREFIX + NimsAllDayTaskService.PROJECT))) return;
		addField(new ComboFieldEditor(AllDayTaskService.PREFIX + NimsAllDayTaskService.COMPONENT, "Component:", getComponentArray(),
				getFieldEditorParent()));
		for (SubmissionProject project : LocalStorageService.getAllDayTaskService().getAssignedProjects())
			for (SubmissionTask task : project.getTasks())
				addField(new TaskFieldEditor(AllDayTaskService.PREFIX + task.getName().replaceAll("\\s", "_"), task.getName() + ":", getFieldEditorParent()));
		addField(new TaskFieldEditor(AllDayTaskService.PREFIX + AllDayTaskService.VACATION_PLANNING_TASK, "Vacation Planning task" + ":", getFieldEditorParent(), true));
	}

	private String[][] getProjectArray() {
		Map<String, String> projects = new HashMap<String, String>();
		IssueService jiraService = new ExtensionManager<IssueService>(IssueService.SERVICE_ID)
				.getService(Activator.getDefault().getPreferenceStore().getString(IssueService.PROPERTY));
		for (Object projectMap : jiraService.getProjects()) {
		    @SuppressWarnings("unchecked")
			Map<String, String> project =  (Map<String, String>) projectMap;
		    projects.put(project.get("name"), project.get("id"));			
		}
		return getArray(projects);
	}
	
	private String[][] getFilterArray() {
		Map<String, String> filters = new HashMap<String, String>();
		IssueService jiraService = new ExtensionManager<IssueService>(IssueService.SERVICE_ID)
				.getService(Activator.getDefault().getPreferenceStore().getString(IssueService.PROPERTY));
		for (Object filterMap : jiraService.getSavedFilters()) {
			@SuppressWarnings("unchecked")
			Map<String, String> filter = (Map<String, String>) filterMap;
			filters.put(filter.get("name"), filter.get("id"));
		}
		return getArray(filters);
	}

	private String[][] getComponentArray() {
		Map<String, String> components = new HashMap<String, String>();
		IssueService jiraService = new ExtensionManager<IssueService>(IssueService.SERVICE_ID)
				.getService(Activator.getDefault().getPreferenceStore().getString(IssueService.PROPERTY));
		if (!StringUtils.isEmpty(getPreferenceStore().getString(AllDayTaskService.PREFIX + NimsAllDayTaskService.PROJECT)))
			for (Object componentMap : jiraService.getComponents(new ExtensionManager<AllDayTaskService>(AllDayTaskService.SERVICE_ID)
					.getService(getPreferenceStore().getString(AllDayTaskService.PROPERTY)).getProjectKey())) {
				@SuppressWarnings("unchecked")
				Map<String, String> component =  (Map<String, String>) componentMap;
				components.put(component.get("name"), component.get("id"));			
			}
		return getArray(components);
	}
	
	private String[][] getArray(Map<String, String> projects) {
		String[][] systemArray = new String[projects.size()][2];

		String[] keys = projects.keySet().toArray(new String[0]);
		String[] values = projects.values().toArray(new String[0]);

		for (int row = 0; row < systemArray.length; row++) {
			systemArray[row][0] = keys[row];
			systemArray[row][1] = values[row];
		}
		return systemArray;
	}
}
