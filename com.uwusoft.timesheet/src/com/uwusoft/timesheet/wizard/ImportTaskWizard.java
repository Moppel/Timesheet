package com.uwusoft.timesheet.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.submission.model.SubmissionProject;
import com.uwusoft.timesheet.submission.model.SubmissionTask;
import com.uwusoft.timesheet.util.ExtensionManager;

public class ImportTaskWizard extends Wizard {

	private SubmissionService submissionService;
	private StorageService storageService;
	private List<SubmissionProject> projects;
	private String system;

	public ImportTaskWizard(String system) {
		super();
		projects = new ArrayList<SubmissionProject>();
		submissionService = new ExtensionManager<SubmissionService>(SubmissionService.SERVICE_ID).getService(system);
		this.system = Character.toUpperCase(system.toCharArray()[system.lastIndexOf('.') + 1])
				+ system.substring(system.lastIndexOf('.') + 2, system.indexOf(SubmissionService.SERVICE_NAME));
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		storageService = new ExtensionManager<StorageService>(
                StorageService.SERVICE_ID).getService(preferenceStore.getString(StorageService.PROPERTY));
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		Collection<SubmissionProject> projects = submissionService.getAssignedProjects().values();
		for (SubmissionProject project : projects) {
			List<String> tasks = storageService.findTasksBySystemAndProject(system, project.getName());
			for (SubmissionTask task : new ArrayList<SubmissionTask>(project.getTasks()))
				if (tasks.contains(task.getName()))
					project.removeTask(task); // remove already imported tasks
			addPage(new TaskListPage(system,  project));			
		}
	}
	
	@Override
	public boolean performFinish() {
		for (IWizardPage page : getPages())
			((TaskListPage)page).addTasksToProjects(projects);
		storageService.importTasks(system, projects);
		return true;
	}
}
