package com.uwusoft.timesheet.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.IPreferenceStore;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.util.ExtensionManager;

public class ImportTasksHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		StorageService storageService = new ExtensionManager<StorageService>(
				StorageService.SERVICE_ID).getService(preferenceStore.getString(StorageService.PROPERTY));
		String system;
		String[] systems = preferenceStore.getString(SubmissionService.PROPERTY).split(SubmissionService.separator);
		if (systems.length == 1) system = systems[0];
		else return null; // TODO list dialog for available submission services
		SubmissionService submissionService = new ExtensionManager<SubmissionService>(
				SubmissionService.SERVICE_ID).getService(system);
		storageService.importTasks(Character.toUpperCase(system.toCharArray()[system.lastIndexOf('.') + 1])
				+ system.substring(system.lastIndexOf('.') + 2, system.indexOf(SubmissionService.SERVICE_NAME)),
				submissionService.getAssignedTasks());
		return null;
	}

}
