package com.uwusoft.timesheet.localstorage;

import java.beans.PropertyChangeListener;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.common.EventManager;
import org.eclipse.jface.preference.IPreferenceStore;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.extensionpoint.model.SubmissionEntry;
import com.uwusoft.timesheet.model.Project;
import com.uwusoft.timesheet.model.Task;
import com.uwusoft.timesheet.model.TaskEntry;
import com.uwusoft.timesheet.util.ExtensionManager;

public class LocalStorageService extends EventManager implements StorageService {

	private EntityManager em;
    private Map<String,String> submissionSystems;

	public LocalStorageService() {
		em = TimesheetApp.factory.createEntityManager();
		submissionSystems = TimesheetApp.getSubmissionSystems();
	}

	@Override
	public List<String> getProjects(String system) {
		Query q = em.createQuery("select p from Project p where p.system ='" + system + "'");
		@SuppressWarnings("unchecked")
		List<Project> projectList = q.getResultList();
		List<String> projects = new ArrayList<String>();
		for (Project project : projectList) projects.add(project.getName());
		return projects;
	}

	@Override
	public List<String> findTasksBySystemAndProject(String system, String project) {
		Query q = em.createQuery("select t from Task t where t.project.name='" + project + "' and t.project.system ='" + system + "'");
		@SuppressWarnings("unchecked")
		List<Task> taskList = q.getResultList();
		List<String> tasks = new ArrayList<String>();
		for (Task task : taskList) tasks.add(task.getName());
		return tasks;
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		addListenerObject(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		removeListenerObject(listener);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TaskEntry> getTaskEntries(Date date) {
		Query q = em.createNativeQuery("select t from TaskEntry t where {fn TIMESTAMPDIFF(SQL_TSI_DAY, t.dateTime, " + new Timestamp(date.getTime()) + ")} = 0");
		return q.getResultList();
	}

	@Override
	public String[] getUsedCommentsForTask(String task, String project,	String system) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createTaskEntry(TaskEntry task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateTaskEntry(Date time, Long id, boolean wholeDate) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateTaskEntry(TaskEntry task, Long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public TaskEntry getLastTask() {
		Query q = em.createQuery("select t from TaskEntry t where t.dateTime is null order by id desc");
		if (q.getResultList().isEmpty()) return null;
		return (TaskEntry) q.getResultList().iterator().next();
	}

	@Override
	public void storeLastDailyTotal() {
	}

	@Override
	public void storeLastWeekTotal(String weeklyWorkingHours) {
	}

	@Override
	public void importTasks(String submissionSystem, Map<String, Set<SubmissionEntry>> projects) {
		for (String project : projects.keySet()) {
			Query q = em.createQuery("select t from Task t where t.project.name = '" + project + "' and t.project.system = '" + submissionSystem + "'");
			@SuppressWarnings("unchecked")
			List<Task> taskList = q.getResultList();
			Set<SubmissionEntry> tasks = new HashSet<SubmissionEntry>(projects.get(project));
			for (Task task : taskList) // collect available tasks
				for (SubmissionEntry submissionTask : projects.get(project))
					if (submissionTask.getName().equals(task.getName()))
						tasks.remove(submissionTask);							
			projects.put(project, tasks);
		}		
		em.getTransaction().begin();
		//em.persist(task);
		em.getTransaction().commit();
	}

	@Override
	public Set<String> submitEntries(int weekNum) {
		return null;
		// TODO Auto-generated method stub
		
	}

	public void submitFillTask(Date date) {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		if (!StringUtils.isEmpty(preferenceStore.getString(TimesheetApp.DAILY_TASK))) {
			Task task = TimesheetApp.createTask(TimesheetApp.DAILY_TASK);
			if (submissionSystems.containsKey(task.getProject().getSystem())) {
				SubmissionEntry submissionTask = getSubmissionTask(task.getName(), task.getProject().getName(), task.getProject().getSystem());
				if (submissionTask != null)
					new ExtensionManager<SubmissionService>(SubmissionService.SERVICE_ID).getService(submissionSystems.get(task.getProject().getSystem()))
							.submit(date, submissionTask, Double.parseDouble(preferenceStore.getString(TimesheetApp.DAILY_TASK_TOTAL)));
			}
		}
	}
	
	private SubmissionEntry getSubmissionTask(String task, String project, String system) {
		Query q = em.createQuery("select t from Task t where t.name = '" + task + "' and t.project.name = '" + project + "' and t.project.system = '" + system + "'");
		if (q.getResultList().isEmpty()) return null;
		Task defaultTask = (Task) q.getResultList().iterator().next();
		return new SubmissionEntry(defaultTask.getProject().getId(), defaultTask.getId(), defaultTask.getName(), defaultTask.getProject().getName(), system);
	}

	@Override
	public void openUrl(String openBrowser) {
	}
}
