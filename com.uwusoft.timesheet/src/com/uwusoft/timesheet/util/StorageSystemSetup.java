package com.uwusoft.timesheet.util;

import java.io.IOException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.swt.widgets.Display;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.dialog.PreferencesDialog;
import com.uwusoft.timesheet.dialog.SingleSelectSystemDialog;
import com.uwusoft.timesheet.extensionpoint.StorageService;

public class StorageSystemSetup {

	public static void execute() {
		// first setup of storage system and import of tasks and projects
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		SingleSelectSystemDialog systemDialog;
		do
			systemDialog = new SingleSelectSystemDialog(Display.getDefault(), StorageService.SERVICE_ID, StorageService.SERVICE_NAME);
		while (systemDialog.open() != Dialog.OK);
		preferenceStore.setValue(StorageService.PROPERTY, systemDialog.getSelectedSystem());
		try {
			((IPersistentPreferenceStore) preferenceStore).save();
		} catch (IOException e) {
			MessageBox.setError("StorageSystemSetup", e.getMessage());
		}
		new ExtensionManager<StorageService>(
				StorageService.SERVICE_ID).getService(preferenceStore.getString(StorageService.PROPERTY)); // construct storage service
    	PreferencesDialog preferencesDialog;
    	do
    		preferencesDialog = new PreferencesDialog(Display.getDefault(), "com.uwusoft.timesheet.preferences.TimesheetPreferencePage");
    	while (preferencesDialog.open() != Dialog.OK);		
	}
}
