package com.uwusoft.timesheet.nimsalldaytaskservice;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.extensionpoint.AllDayTaskService;
import com.uwusoft.timesheet.extensionpoint.IssueService;
import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
import com.uwusoft.timesheet.preferences.TaskFieldEditor;
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
		addField(new ComboFieldEditor(AllDayTaskService.PREFIX + NimsAllDayTaskService.COMPONENT, "Component:", getComponentArray(),
				getFieldEditorParent()));
        for (String task : LocalStorageService.getInstance().getAllDayTasks())
    		addField(new TaskFieldEditor(AllDayTaskService.PREFIX + task.replaceAll("\\s", "_").toLowerCase(), task + ":", getFieldEditorParent()));
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
		for (Object componentMap : jiraService.getComponents(new ExtensionManager<AllDayTaskService>(AllDayTaskService.SERVICE_ID)
				.getService(Activator.getDefault().getPreferenceStore().getString(AllDayTaskService.PROPERTY)).getProjectKey())) {
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
