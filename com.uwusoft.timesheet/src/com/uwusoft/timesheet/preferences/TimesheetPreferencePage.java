package com.uwusoft.timesheet.preferences;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;

public class TimesheetPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {
	
	public TimesheetPreferencePage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("General Timesheet preferences");
	}

	@Override
	protected void createFieldEditors() {
		addField(new StringFieldEditor(TimesheetApp.WORKING_HOURS, "Weekly working hours:",
				getFieldEditorParent()));
		addField(new StringFieldEditor(TimesheetApp.DEFAULT_TASK, "Default task:",
				getFieldEditorParent()));
		addField(new StringFieldEditor(TimesheetApp.DAILY_TASK, "Daily task:",
				getFieldEditorParent()));
		addField(new StringFieldEditor(TimesheetApp.DAILY_TASK_TOTAL, "Daily task total:",
				getFieldEditorParent()));
		addField(new ComboFieldEditor(StorageService.PROPERTY, "Storage System:", getSystemArray(StorageService.SERVICE_ID, "storage"),
				getFieldEditorParent()));
		addField(new ComboFieldEditor(SubmissionService.PROPERTY, "Submission System:", getSystemArray(SubmissionService.SERVICE_ID, "submission"),
				getFieldEditorParent())); // TODO add custom list editor
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
}
