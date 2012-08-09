package com.uwusoft.timesheet.googlestorage;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.util.ExtensionManager;

public class GooglePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public GooglePreferencePage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Google preferences");
		getPreferenceStore().addPropertyChangeListener(this);
	}

	@Override
	protected void createFieldEditors() {
		addField(new StringFieldEditor(GoogleStorageService.PREFIX + StorageService.USERNAME, "User name:",
				getFieldEditorParent()));
		//addField(new SpreadsheetFieldEditor(GoogleStorageService.SPREADSHEET_KEY, "Spreadsheet key:", getFieldEditorParent()));
		addField(new BooleanFieldEditor(GoogleStorageService.PREFIX + StorageService.OPEN_BROWSER_CHECKIN, "Open Url after Check in",
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(GoogleStorageService.PREFIX + StorageService.OPEN_BROWSER_CHANGE_TASK, "Open Url after Change task",
				getFieldEditorParent()));
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getSource() instanceof IPreferenceStore && GoogleStorageService.SPREADSHEET_KEY.equals(event.getProperty())) {
			StorageService storageService  = new ExtensionManager<StorageService>(StorageService.SERVICE_ID)
					.getService(getPreferenceStore().getString(StorageService.PROPERTY));
			if(storageService != null) storageService.reload();
		}
	}
}
