package com.uwusoft.timesheet.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.dialog.TaskListDialog;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.extensionpoint.model.SubmissionEntry;
import com.uwusoft.timesheet.util.ExtensionManager;
import com.uwusoft.timesheet.wizard.ImportTaskWizard;

public class ImportTasksHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		/*String system;
		String[] systems = preferenceStore.getString(SubmissionService.PROPERTY).split(SubmissionService.separator);
		if (systems.length == 1) system = systems[0];
		else return null; // TODO list dialog for available submission services
		WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(), new ImportTaskWizard(Character.toUpperCase(system.toCharArray()[system.lastIndexOf('.') + 1])
				+ system.substring(system.lastIndexOf('.') + 2, system.indexOf(SubmissionService.SERVICE_NAME))));
		dialog.open();*/							
		TaskListDialog listDialog = new TaskListDialog(Display.getDefault().getActiveShell(), null);
		listDialog.setTitle("Tasks");
		listDialog.setMessage("Select task");
		listDialog.setWidthInChars(70);
		if (listDialog.open() == Dialog.OK) {
		    String selectedTask = Arrays.toString(listDialog.getResult());
		    selectedTask = selectedTask.substring(selectedTask.indexOf("[") + 1, selectedTask.indexOf("]"));
			if (StringUtils.isEmpty(selectedTask)) return null;
			Map<String, Set<SubmissionEntry>> projects = new HashMap<String, Set<SubmissionEntry>>();
			Set<SubmissionEntry> tasks = new HashSet<SubmissionEntry>();
			//tasks.add(new SubmissionTask(projectId, id, name, projectName));
			projects.put(listDialog.getProject(), tasks);
			StorageService storageService = new ExtensionManager<StorageService>(
					StorageService.SERVICE_ID).getService(preferenceStore.getString(StorageService.PROPERTY));
			storageService.importTasks(listDialog.getSystem(), projects);
		}
		return null;
	}

}
