package com.uwusoft.timesheet.util;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.dialog.MultipleSelectSystemDialog;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.wizard.ImportTaskWizard;

public class ImportTasksUtil {

	public static Object execute(StorageService storageService) {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		if (StringUtils.isEmpty(preferenceStore.getString(SubmissionService.PROPERTY))) {
			// TODO first setup of submission system
			MultipleSelectSystemDialog systemDialog;
			do
				systemDialog = new MultipleSelectSystemDialog(Display.getDefault(), SubmissionService.SERVICE_ID, SubmissionService.SERVICE_NAME);
			while (systemDialog.open() != Dialog.OK);
			preferenceStore.setValue(SubmissionService.PROPERTY, StringUtils.join(systemDialog.getSelectedSystems(), SubmissionService.separator));
		}
		if (!StringUtils.isEmpty(preferenceStore.getString(SubmissionService.PROPERTY))) {
			Collection<String> systems = Arrays.asList(preferenceStore.getString(SubmissionService.PROPERTY).split(SubmissionService.separator));
			for (String system : systems) {
				WizardDialog dialog = new WizardDialog(new Shell(Display.getDefault(), SWT.NO_TRIM | SWT.ON_TOP), new ImportTaskWizard(storageService, system));
				dialog.open();							
			}
		}
		return null;
	}
}
