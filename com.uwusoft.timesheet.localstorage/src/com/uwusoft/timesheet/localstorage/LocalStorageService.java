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

import org.eclipse.core.commands.common.EventManager;

import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.extensionpoint.model.SubmissionTask;
import com.uwusoft.timesheet.model.Project;
import com.uwusoft.timesheet.model.Task;

public class LocalStorageService extends EventManager implements StorageService {

	private EntityManager em;
    private Map<String,String> submissionSystems;

	public LocalStorageService() {
		em = TimesheetApp.factory.createEntityManager();
		submissionSystems = TimesheetApp.getSubmissionSystems();
	}

	@Override
	public List<String> getSystems() {
		Query q = em.createQuery("select p from Project p");
		@SuppressWarnings("unchecked")
		List<Project> projectList = q.getResultList();
		Set<String> systems = new HashSet<String>();
		for (Project project : projectList) systems.add(project.getName());
		return new ArrayList<String>(systems);
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
		Query q = em.createQuery("select t from Task t where t.project.name='" + project
				+ "' and t.project.system ='" + system + "'");
		@SuppressWarnings("unchecked")
		List<Task> taskList = q.getResultList();
		List<String> tasks = new ArrayList<String>();
		for (Task task : taskList) tasks.add(task.getTask());
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
	public List<Task> getTaskEntries(Date date) {
		Query q = em.createQuery("select t from Task t where {fn TIMESTAMPDIFF(SQL_TSI_DAY, t.dateTime, " + new Timestamp(date.getTime()) + ")} = 0");
		return q.getResultList();
	}

	@Override
	public String[] getUsedCommentsForTask(String task, String project,	String system) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createTaskEntry(Task task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateTaskEntry(Date time, Long id, boolean wholeDate) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateTaskEntry(Task task, Long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Task getLastTask() {
		Query q = em.createQuery("select t from Task t where t.dateTime is null order by id desc");
		if (q.getResultList().isEmpty()) return null;
		return (Task) q.getResultList().iterator().next();
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
		em.getTransaction().begin();
		for (String project : projects.keySet()) {
			Query q = em.createQuery("select t from Task t where t.project.name = '" + project + "' and t.project.system = '" + submissionSystem + "'");
			@SuppressWarnings("unchecked")
			List<Task> taskList = q.getResultList();
			Set<SubmissionTask> tasks = new HashSet<SubmissionTask>(projects.get(project));
			for (Task task : taskList) // collect available tasks
				for (SubmissionTask submissionTask : projects.get(project))
					if (submissionTask.getName().equals(task.getTask()))
						tasks.remove(submissionTask);							
			projects.put(project, tasks);
		}
		
		//em.persist(task);
		em.getTransaction().commit();
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
