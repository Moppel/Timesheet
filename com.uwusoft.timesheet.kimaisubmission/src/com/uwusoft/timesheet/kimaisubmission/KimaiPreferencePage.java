package com.uwusoft.timesheet.kimaisubmission;

import org.eclipse.ui.IWorkbench;

import com.uwusoft.timesheet.extensionpoint.SubmissionSystemPreferencePage;

public class KimaiPreferencePage extends SubmissionSystemPreferencePage {

	@Override
	public void init(IWorkbench workbench) {
		super.init("Kimai");
	}

	@Override
	protected void createFieldEditors() {
		super.createFieldEditors(KimaiSubmissionService.PREFIX);
	}
}
