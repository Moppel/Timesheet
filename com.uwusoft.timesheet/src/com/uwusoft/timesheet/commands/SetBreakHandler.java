package com.uwusoft.timesheet.commands;

import java.sql.Timestamp;
import java.util.Date;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.ISourceProviderService;

import com.uwusoft.timesheet.dialog.TimeDialog;
import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.model.Task;
import com.uwusoft.timesheet.model.TaskEntry;

public class SetBreakHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		LocalStorageService storageService = LocalStorageService.getInstance();
		TaskEntry lastTask = storageService.getLastTask();
		TimeDialog timeDialog = new TimeDialog(Display.getDefault(), StorageService.BREAK, new Date());
		if (timeDialog.open() == Dialog.OK) {
			lastTask.setDateTime(new Timestamp(timeDialog.getTime().getTime()));
			storageService.updateTaskEntry(lastTask);
			TaskEntry task = new TaskEntry(null, new Task(StorageService.BREAK));
			storageService.createTaskEntry(task);				
			storageService.openUrl(StorageService.OPEN_BROWSER_CHANGE_TASK);
			ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI.getWorkbench().getService(ISourceProviderService.class);
			SessionSourceProvider commandStateService = (SessionSourceProvider) sourceProviderService.getSourceProvider(SessionSourceProvider.SESSION_STATE);
			commandStateService.setEnabled(true);
			commandStateService.setBreak(true);
		}
		return null;
	}
}
