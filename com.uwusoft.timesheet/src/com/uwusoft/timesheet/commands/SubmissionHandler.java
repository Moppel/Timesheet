package com.uwusoft.timesheet.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.dialog.SubmissionDialog;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.util.ExtensionManager;

public class SubmissionHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		int weekNum = Integer.parseInt(event.getParameter("Timesheet.commands.weekNum"));
		SubmissionDialog submissionDialog = new SubmissionDialog(Display.getDefault(), weekNum);
		if (submissionDialog.open() == Dialog.OK) {
			new ExtensionManager<StorageService>(StorageService.SERVICE_ID).getService(preferenceStore
					.getString(StorageService.PROPERTY)).submitEntries(submissionDialog.getWeekNum());
		}                            							
		return null;
	}


}
