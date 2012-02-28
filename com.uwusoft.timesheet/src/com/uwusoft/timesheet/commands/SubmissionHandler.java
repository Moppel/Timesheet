package com.uwusoft.timesheet.commands;

import java.awt.Desktop;
import java.net.URI;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.dialog.SubmissionDialog;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.util.ExtensionManager;
import com.uwusoft.timesheet.util.MessageBox;

public class SubmissionHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		int weekNum = Integer.parseInt(event.getParameter("Timesheet.commands.weekNum"));
		SubmissionDialog submissionDialog = new SubmissionDialog(Display.getDefault(), weekNum);
		if (submissionDialog.open() == Dialog.OK) {
			Set<String> systems = new ExtensionManager<StorageService>(StorageService.SERVICE_ID).getService(preferenceStore
					.getString(StorageService.PROPERTY)).submitEntries(submissionDialog.getWeekNum());
			MessageBox.setMessage("Submission", "Submission of week " + weekNum + " successful!");
			for (String system : systems) {
				if (preferenceStore.getBoolean(system.toLowerCase() + "." + SubmissionService.OPEN_BROWSER)) {
					if(!Desktop.isDesktopSupported()) {
						MessageBox.setError(this.getClass().getSimpleName(), "Desktop is not supported (fatal)");
						return null;
				    }				
				    Desktop desktop = Desktop.getDesktop();
				    if(!desktop.isSupported(Desktop.Action.BROWSE)) {
				    	MessageBox.setError(this.getClass().getSimpleName(), "Desktop doesn't support the browse action (fatal)");
				    	return null;
				    }
			        try {
				        URI uri = new URI(preferenceStore.getString(system.toLowerCase() + ".server.url"));
				        desktop.browse(uri);
			        }
			        catch (Exception e) {
			        	MessageBox.setError(this.getClass().getSimpleName(), e.getMessage());
			        }
				}
			}
		}                            							
		return null;
	}
}
