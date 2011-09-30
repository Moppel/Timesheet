package com.uwusoft.timesheet.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.util.ExtensionManager;

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
		addField(new ComboFieldEditor(StorageService.PROPERTY, "Storage System:", getSystemArray(StorageService.SERVICE_ID, "storage"),
				getFieldEditorParent()));
		addField(new ComboFieldEditor(SubmissionService.PROPERTY, "Submission System:", getSystemArray(SubmissionService.SERVICE_ID, "submission"),
				getFieldEditorParent())); // TODO add custom list editor
		addField(new IntegerFieldEditor(TimesheetApp.WORKING_HOURS, "Weekly working hours:",
				getFieldEditorParent()));
		addField(new ComboFieldEditor(TimesheetApp.HOLIDAY_TASK, "Statutory holiday task:", getTaskArray("Primavera"), // TODO
				getFieldEditorParent()));
		addField(new ComboFieldEditor(TimesheetApp.VACATION_TASK, "Vacation task:", getTaskArray("Primavera"), // TODO
				getFieldEditorParent()));
		addField(new ComboFieldEditor(TimesheetApp.SICK_TASK, "Sick task:", getTaskArray("Primavera"), // TODO
				getFieldEditorParent()));
		//if (getTaskArray("Primavera").length > 0) {
			addField(new ComboFieldEditor(TimesheetApp.DEFAULT_TASK, "Default task:", getTaskArray("Primavera"), // TODO
					getFieldEditorParent()));
			addField(new ComboFieldEditor(TimesheetApp.DAILY_TASK, "Daily task:", getTaskArray("Primavera"), // TODO
					getFieldEditorParent()));
			addField(new StringFieldEditor(TimesheetApp.DAILY_TASK_TOTAL, "Daily task total:",
					getFieldEditorParent()));
		//}
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
	
	private String[][] getTaskArray(String name) {
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
