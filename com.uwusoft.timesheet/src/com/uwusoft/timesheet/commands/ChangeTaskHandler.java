package com.uwusoft.timesheet.commands;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.ISourceProviderService;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.dialog.TaskListDialog;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.model.Project;
import com.uwusoft.timesheet.model.Task;
import com.uwusoft.timesheet.model.TaskEntry;
import com.uwusoft.timesheet.util.ExtensionManager;
import com.uwusoft.timesheet.util.MessageBox;

public class ChangeTaskHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		ILog logger = Activator.getDefault().getLog();
		String changeTime = event.getParameter("Timesheet.commands.changeTime");
		Date changeDate = new Date();
		if (changeTime != null)
			try {
				changeDate = StorageService.formatter.parse(changeTime);
			} catch (ParseException e) {
				MessageBox.setError("Change task", e.getMessage());
			}
		StorageService storageService = new ExtensionManager<StorageService>(
				StorageService.SERVICE_ID).getService(preferenceStore.getString(StorageService.PROPERTY));
		TaskEntry lastTask = storageService.getLastTask();
		TaskListDialog listDialog = new TaskListDialog(Display.getDefault(), lastTask.getTask(), changeDate, "Change task");
		listDialog.setMessage("Select next task");
		if (listDialog.open() == Dialog.OK) {
		    String selectedTask = Arrays.toString(listDialog.getResult());
		    selectedTask = selectedTask.substring(selectedTask.indexOf("[") + 1, selectedTask.indexOf("]"));
			if (StringUtils.isEmpty(selectedTask)) return null;
			storageService.updateTaskEntry(lastTask.getId(), listDialog.getTime(), true);
			TaskEntry task = new TaskEntry(null, new Task(selectedTask));
			task.getTask().setProject(new Project(listDialog.getProject(), listDialog.getSystem()));
			task.setComment(listDialog.getComment());
			storageService.createTaskEntry(task);				
            logger.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "change task last task: " + lastTask));
			storageService.openUrl(StorageService.OPEN_BROWSER_CHANGE_TASK);
			ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI.getWorkbench().getService(ISourceProviderService.class);
			SessionSourceProvider commandStateService = (SessionSourceProvider) sourceProviderService.getSourceProvider(SessionSourceProvider.SESSION_STATE);
			commandStateService.setEnabled(true);
			commandStateService.setBreak(false);
		}						
		return null;
	}
}
