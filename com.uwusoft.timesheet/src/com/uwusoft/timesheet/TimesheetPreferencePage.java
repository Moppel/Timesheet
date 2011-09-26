package com.uwusoft.timesheet;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;

public class TimesheetPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {
	
	/**
	 * 
	 */
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
		addField(new StringFieldEditor("weekly.workinghours", "Weekly working hours:",
				getFieldEditorParent()));
		addField(new StringFieldEditor("task.default", "Default task:",
				getFieldEditorParent()));
		addField(new StringFieldEditor("task.daily", "Daily task:",
				getFieldEditorParent()));
		addField(new StringFieldEditor("task.daily.total", "Daily task total:",
				getFieldEditorParent()));
		addField(new ComboFieldEditor("storage.system", "Storage System:", getSystemArray(StorageService.SERVICE_ID, "storage"),
				getFieldEditorParent()));
		addField(new ComboFieldEditor("submission.system", "Submission System:", getSystemArray(SubmissionService.SERVICE_ID, "submission"),
				getFieldEditorParent())); // TODO add custom list editor
	}

	private String[][] getSystemArray(String serviceId, String serviceName) {
		Map<String, String> storageSystems = new HashMap<String, String>(); 
		for (IConfigurationElement e : Platform.getExtensionRegistry().getConfigurationElementsFor(serviceId)) {
			String contributorName = e.getContributor().getName();
			storageSystems.put(Character.toUpperCase(contributorName.toCharArray()[contributorName.lastIndexOf('.') + 1])
					+ contributorName.substring(contributorName.lastIndexOf('.') + 2, contributorName.indexOf(serviceName)),
					contributorName);
		}
		String[][] storageSystemArray = new String[storageSystems.size()][2];

		String[] keys = storageSystems.keySet().toArray(new String[0]);
		String[] values = storageSystems.values().toArray(new String[0]);

		for (int row = 0; row < storageSystemArray.length; row++) {
			storageSystemArray[row][0] = keys[row];
			storageSystemArray[row][1] = values[row];
		}
		return storageSystemArray;
	}
}
