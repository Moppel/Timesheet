package com.uwusoft.timesheet.localstorage;

import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.preference.IPreferenceStore;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.extensionpoint.model.SubmissionTask;
import com.uwusoft.timesheet.model.Task;

public class LocalStorageService implements StorageService {

	private EntityManager em;
    private Map<String,String> submissionSystems;

	public LocalStorageService() {
		em = TimesheetApp.factory.createEntityManager();
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		String[] systems = preferenceStore.getString(SubmissionService.PROPERTY).split(SubmissionService.separator);
		for (String system : systems) {
			if (!StringUtils.isEmpty(system))
				submissionSystems.put(Character.toUpperCase(system.toCharArray()[system.lastIndexOf('.') + 1])
						+ system.substring(system.lastIndexOf('.') + 2, system.indexOf(SubmissionService.SERVICE_NAME)),
						system);
		}
	}

	@Override
	public List<String> getSystems() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getProjects(String system) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> findTasksBySystemAndProject(String system,
			String project) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Task> getTaskEntries(Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createTaskEntry(Task task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateTaskEntry(Date time, Long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateTaskEntry(Task task, Long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Task getLastTask() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void storeLastDailyTotal() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void storeLastWeekTotal(String weeklyWorkingHours) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void importTasks(String submissionSystem, Map<String, Set<SubmissionTask>> projects) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<String> submitEntries(int weekNum) {
		return null;
		// TODO Auto-generated method stub
		
	}

	public void submitFillTask(Date date) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void openUrl(String openBrowser) {
	}
}
