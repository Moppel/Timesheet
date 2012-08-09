package com.uwusoft.timesheet.commands;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.dialog.PreferencesDialog;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.wizard.ImportTaskWizard;

public class ImportTasksHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		if (StringUtils.isEmpty(preferenceStore.getString(SubmissionService.PROPERTY))) {
			// first setup of submission system
			PreferencesDialog preferencesDialog;
			do
				preferencesDialog = new PreferencesDialog(Display.getDefault(), SubmissionService.SERVICE_ID, SubmissionService.SERVICE_NAME, true);
			while (preferencesDialog.open() != Dialog.OK);
			preferenceStore.setValue(SubmissionService.PROPERTY, StringUtils.join(preferencesDialog.getSelectedSystems(), SubmissionService.separator));
		}
		if (!StringUtils.isEmpty(preferenceStore.getString(SubmissionService.PROPERTY))) {
			Collection<String> systems = Arrays.asList(preferenceStore.getString(SubmissionService.PROPERTY).split(SubmissionService.separator));
			for (String system : systems) {
				WizardDialog dialog = new WizardDialog(new Shell(Display.getDefault(), SWT.NO_TRIM | SWT.ON_TOP), new ImportTaskWizard(system));
				dialog.open();							
			}
		}
		return null;
	}
}
