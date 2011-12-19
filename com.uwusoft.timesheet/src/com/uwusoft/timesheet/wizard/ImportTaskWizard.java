package com.uwusoft.timesheet.wizard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.util.ExtensionManager;

public class ImportTaskWizard extends Wizard {

	private SubmissionService submissionService;
	private Map<String, List<String>> projects;
	private String system;

	public ImportTaskWizard(String system) {
		super();
		projects = new HashMap<String, List<String>>();
		this.system = system;
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		String[] systems = preferenceStore.getString(SubmissionService.PROPERTY).split(SubmissionService.separator);
		for (String _system : systems) {
			if (_system.contains(system.toLowerCase())) {
				submissionService = new ExtensionManager<SubmissionService>(SubmissionService.SERVICE_ID).getService(_system);
				break;
			}
		}
		Map<String, List<String>> projects = submissionService.getAssignedProjects();
		for (String project : projects.keySet()) {
			addPage(new TaskListPage(system,  project, projects.get(project)));			
		}
	}
	
	@Override
	public boolean performFinish() {
		for (IWizardPage page : getPages()) {
			((TaskListPage)page).addTasksToProjects(projects);
		}
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		StorageService storageService = new ExtensionManager<StorageService>(
                StorageService.SERVICE_ID).getService(preferenceStore.getString(StorageService.PROPERTY));
		storageService.importTasks(system, projects);
		return true;
	}

}
