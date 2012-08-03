package com.uwusoft.timesheet.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.wizard.ImportTaskWizard;

public class ImportTasksHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		Collection<String> systems = Arrays.asList(preferenceStore.getString(SubmissionService.PROPERTY).split(SubmissionService.separator));
		if (systems.size() == 0) {
			// This should only be the first setup
			Map<String, String> availableSystems = new HashMap<String, String>();
			for (IConfigurationElement e : Platform.getExtensionRegistry().getConfigurationElementsFor(SubmissionService.SERVICE_ID)) {
				String contributorName = e.getContributor().getName();
				availableSystems.put(Character.toUpperCase(contributorName.toCharArray()[contributorName.lastIndexOf('.') + 1])
						+ contributorName.substring(contributorName.lastIndexOf('.') + 2, contributorName.indexOf(SubmissionService.SERVICE_NAME)),
						contributorName);
			}
			if (!availableSystems.isEmpty()) {
				// TODO create dialog (with check boxes) to select available systems and set them up
				systems = availableSystems.values();
				preferenceStore.setValue(SubmissionService.PROPERTY, StringUtils.join(systems, SubmissionService.separator));
			}
		}
		for (String system : systems) {
			WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(), new ImportTaskWizard(system));
			dialog.open();							
		}
		return null;
	}
}
