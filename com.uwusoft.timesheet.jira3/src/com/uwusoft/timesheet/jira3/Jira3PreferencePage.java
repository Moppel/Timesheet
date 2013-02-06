package com.uwusoft.timesheet.jira3;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;

public class Jira3PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public Jira3PreferencePage() {
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
		addField(new StringFieldEditor(Jira3Service.PREFIX + SubmissionService.URL, "Server Url:",
				getFieldEditorParent()));
		addField(new StringFieldEditor(Jira3Service.PREFIX + StorageService.USERNAME, "User name:",
				getFieldEditorParent()));
	}
}
