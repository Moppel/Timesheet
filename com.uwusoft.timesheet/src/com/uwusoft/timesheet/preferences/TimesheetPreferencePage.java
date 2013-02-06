package com.uwusoft.timesheet.preferences;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.Messages;
import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.extensionpoint.HolidayService;
import com.uwusoft.timesheet.extensionpoint.IssueService;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.model.AllDayTasks;

public class TimesheetPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {
	
	public TimesheetPreferencePage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("General Timesheet preferences");
		getPreferenceStore().addPropertyChangeListener(this);
	}

	@Override
	protected void createFieldEditors() {
		if (getPreferenceStore() instanceof ScopedPreferenceStore)
			addField(new DirectoryFieldEditor(Activator.GOOGLE_DRIVE, "Use Google Drive", getFieldEditorParent()));
		addField(new ComboFieldEditor(StorageService.PROPERTY, "Storage System:", getSystemArray(StorageService.SERVICE_ID,
				StorageService.SERVICE_NAME), getFieldEditorParent()));
		addField(new ComboFieldEditor(HolidayService.PROPERTY, "Holiday System:", getSystemArray(HolidayService.SERVICE_ID,
				HolidayService.SERVICE_NAME), getFieldEditorParent()));
		addField(new ComboFieldEditor(IssueService.PROPERTY, "Task and Issue System:", getSystemArray(IssueService.SERVICE_ID,
				IssueService.SERVICE_NAME), getFieldEditorParent()));
		addField(new PluginListEditor(SubmissionService.PROPERTY, "Submission System:", SubmissionService.SERVICE_ID,
				SubmissionService.SERVICE_NAME,	getFieldEditorParent()));
		addField(new IntegerFieldEditor(TimesheetApp.WORKING_HOURS, "Weekly working hours:", getFieldEditorParent()));
		addField(new WeekdayListEditor(TimesheetApp.NON_WORKING_DAYS, "Non Working Days:",	getFieldEditorParent()));
        for (String task : AllDayTasks.allDayTasks)
    		addField(new TaskFieldEditor(task, Messages.getString(task) + " task:", getFieldEditorParent()));
		addField(new TaskFieldEditor(TimesheetApp.DEFAULT_TASK, "Default task:", getFieldEditorParent()));
		addField(new TaskFieldEditor(TimesheetApp.DAILY_TASK, "Daily task:", getFieldEditorParent()));
		if (!StringUtils.isEmpty(getPreferenceStore().getString(TimesheetApp.DAILY_TASK)))
			addField(new StringFieldEditor(TimesheetApp.DAILY_TASK_TOTAL, "Daily task total:", getFieldEditorParent()));
	}

	private String[][] getSystemArray(String serviceId, String serviceName) {
		Map<String, String> systems = new HashMap<String, String>(); 
		for (IConfigurationElement e : Platform.getExtensionRegistry().getConfigurationElementsFor(serviceId)) {
			String contributorName = e.getContributor().getName();
			systems.put(Character.toUpperCase(contributorName.toCharArray()[contributorName.lastIndexOf('.') + 1])
					+ contributorName.substring(contributorName.lastIndexOf('.') + 2, contributorName.indexOf(serviceName)),
					contributorName);
		}
		String[][] systemArray = new String[systems.size()][2];

		String[] keys = systems.keySet().toArray(new String[0]);
		String[] values = systems.values().toArray(new String[0]);

		for (int row = 0; row < systemArray.length; row++) {
			systemArray[row][0] = keys[row];
			systemArray[row][1] = values[row];
		}
		return systemArray;
	}
	
	/*private String[][] getTaskArray(String name) {
		try {
			StorageService storageService = new ExtensionManager<StorageService>(StorageService.SERVICE_ID)
					.getService(getPreferenceStore().getString(StorageService.PROPERTY));
			List<String> tasksList = storageService.getTasks() == null ? new ArrayList<String>()
					: storageService.getTasks().get(name);
			if (tasksList == null) return new String[0][2];
			String[][] tasksArray = new String[tasksList.size()][2];
			String[] tasks = tasksList.toArray(new String[0]);
			for (int row = 0; row < tasksArray.length; row++) {
				tasksArray[row][0] = tasks[row];
				tasksArray[row][1] = tasks[row];
			}
			return tasksArray;
		} catch (ClassCastException e) {
			return new String[0][2];
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#doGetPreferenceStore()
	 */
	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}
}
