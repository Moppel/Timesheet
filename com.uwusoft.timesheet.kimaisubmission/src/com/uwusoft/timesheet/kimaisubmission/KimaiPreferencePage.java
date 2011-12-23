package com.uwusoft.timesheet.kimaisubmission;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.uwusoft.timesheet.Activator;

public class KimaiPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public KimaiPreferencePage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Kimai preferences");
		getPreferenceStore().addPropertyChangeListener(this);
	}

	@Override
	protected void createFieldEditors() {
		addField(new StringFieldEditor(KimaiSubmissionService.URL, "Server Url:",
				getFieldEditorParent()));
		addField(new StringFieldEditor(KimaiSubmissionService.USERNAME, "User name:",
				getFieldEditorParent()));
	}

}
