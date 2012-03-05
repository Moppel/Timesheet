package com.uwusoft.timesheet.commands;

import java.util.Date;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.dialog.TimeDialog;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.model.Task;
import com.uwusoft.timesheet.util.ExtensionManager;

public class SetBreakHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		StorageService storageService = new ExtensionManager<StorageService>(
				StorageService.SERVICE_ID).getService(preferenceStore.getString(StorageService.PROPERTY));
		Task lastTask = storageService.getLastTask();
		TimeDialog timeDialog = new TimeDialog(Display.getDefault(), StorageService.BREAK, new Date());
		if (timeDialog.open() == Dialog.OK) {
			storageService.updateTaskEntry(timeDialog.getTime(), lastTask.getId(), true);
			Task task = new Task(null, StorageService.BREAK);
			storageService.createTaskEntry(task);				
			preferenceStore.setValue(TimesheetApp.SYSTEM_SHUTDOWN, StorageService.formatter.format(timeDialog.getTime()));
			storageService.openUrl(StorageService.OPEN_BROWSER_CHANGE_TASK);
		}
		return null;
	}
}
