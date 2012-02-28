package com.uwusoft.timesheet.extensionpoint;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.uwusoft.timesheet.Activator;

public abstract class SubmissionSystemPreferencePage extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public SubmissionSystemPreferencePage() {
		super(GRID);
	}

	public void init(String system) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(system + " preferences");
		getPreferenceStore().addPropertyChangeListener(this);
	}

	protected void createFieldEditors(String prefix) {
		addField(new StringFieldEditor(prefix + SubmissionService.URL, "Server Url:",
				getFieldEditorParent()));
		addField(new StringFieldEditor(prefix + SubmissionService.USERNAME, "User name:",
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(prefix + SubmissionService.OPEN_BROWSER, "Open Url after submission",
				getFieldEditorParent()));
	}
}
