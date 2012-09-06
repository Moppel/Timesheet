package com.uwusoft.timesheet.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.custom.BusyIndicator;

import com.uwusoft.timesheet.extensionpoint.ImportTaskService;
import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.submission.model.SubmissionProject;
import com.uwusoft.timesheet.submission.model.SubmissionTask;
import com.uwusoft.timesheet.util.ExtensionManager;

public class ImportTaskWizard extends Wizard {

	private SubmissionService submissionService;
	private ImportTaskService storageService;
	private List<SubmissionProject> projects;
	private String system;

	public ImportTaskWizard(StorageService storageService, String system) {
		super();
		projects = new ArrayList<SubmissionProject>();
		submissionService = new ExtensionManager<SubmissionService>(SubmissionService.SERVICE_ID).getService(system);
		this.system = Character.toUpperCase(system.toCharArray()[system.lastIndexOf('.') + 1])
				+ system.substring(system.lastIndexOf('.') + 2, system.indexOf(SubmissionService.SERVICE_NAME));
		if (storageService == null)
			this.storageService = LocalStorageService.getInstance();
		else
			this.storageService = storageService;
		setNeedsProgressMonitor(true);
	}

	public ImportTaskWizard(String system) {
		this(null, system);
	}

	@Override
	public void addPages() {
		BusyIndicator.showWhile(getContainer().getShell().getDisplay(), new Runnable() {
			public void run() {
				Collection<SubmissionProject> projects = submissionService.getAssignedProjects().values();
				for (SubmissionProject project : projects) {
					List<String> tasks = storageService.findTasksBySystemAndProject(system, project.getName());
					for (SubmissionTask task : new ArrayList<SubmissionTask>(project.getTasks()))
						if (tasks.contains(task.getName()))
							project.removeTask(task); // remove already imported tasks
					addPage(new TaskListPage(system,  project));			
				}
			}
		});
	}
	
	@Override
	public boolean performFinish() {
		BusyIndicator.showWhile(getContainer().getShell().getDisplay(), new Runnable() {
			public void run() {
				for (IWizardPage page : getPages())
					((TaskListPage)page).addTasksToProjects(projects);
				storageService.importTasks(system, projects);
			}
		});
		return true;
	}
}
