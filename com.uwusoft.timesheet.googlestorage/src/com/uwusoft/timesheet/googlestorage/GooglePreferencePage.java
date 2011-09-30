package com.uwusoft.timesheet.googlestorage;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.uwusoft.timesheet.Activator;

public class GooglePreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

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
		addField(new StringFieldEditor(GoogleStorageService.USERNAME, "User name:",
				getFieldEditorParent()));
		addField(new StringFieldEditor(GoogleStorageService.SPREADSHEET_KEY, "Spreadsheet key:",
				getFieldEditorParent()));
	}
}
