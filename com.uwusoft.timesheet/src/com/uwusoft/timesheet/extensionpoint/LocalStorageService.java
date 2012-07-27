package com.uwusoft.timesheet.extensionpoint;

import java.beans.PropertyChangeListener;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.common.EventManager;
import org.eclipse.jface.preference.IPreferenceStore;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.extensionpoint.model.SubmissionEntry;
import com.uwusoft.timesheet.model.Project;
import com.uwusoft.timesheet.model.Task;
import com.uwusoft.timesheet.model.TaskEntry;
import com.uwusoft.timesheet.util.ExtensionManager;

public class LocalStorageService extends EventManager implements StorageService {

	private static final String PERSISTENCE_UNIT_NAME = "timesheet";
	public static EntityManagerFactory factory;
	
	private static EntityManager em;
    private Map<String,String> submissionSystems;

	static {
		Map<String, Object> configOverrides = new HashMap<String, Object>();
		configOverrides.put("javax.persistence.jdbc.url",
				"jdbc:derby:" + System.getProperty("user.home") + "/.eclipse/databases/timesheet;create=true");
		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, configOverrides);
	}
    
    public LocalStorageService() {
		em = factory.createEntityManager();
		submissionSystems = TimesheetApp.getSubmissionSystems();
	}
	
	@Override
	public void reload() {
		// TODO Auto-generated method stub
	}

	@Override
	public List<String> getProjects(String system) {
		@SuppressWarnings("unchecked")
		List<Project> projectList = em.createQuery(
				"select p from Project p " +
				"where p.system = :system")
				.setParameter("system", system)
				.getResultList();
		List<String> projects = new ArrayList<String>();
		for (Project project : projectList) projects.add(project.getName());
		return projects;
	}

	@Override
	public List<String> findTasksBySystemAndProject(String system, String project) {
		List<Task> taskList = findTasksByProjectAndSystem(project, system);
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
	public List<TaskEntry> getTaskEntries(Date startDate, Date endDate) {
		Date date = new Date(); // TODO 
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
	public void updateTaskEntry(Long id, Date time, boolean wholeDate) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateTaskEntry(Long id, String task, String project, String system, String comment) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public TaskEntry getLastTask() {
		@SuppressWarnings("unchecked")
		List<TaskEntry> taskEntries = em.createQuery("select t from TaskEntry t where t.dateTime is null order by t.id desc")
				.getResultList();
		if (taskEntries.isEmpty()) return null;
		return taskEntries.iterator().next();
	}

	@Override
	public Date getLastTaskEntryDate() {
		// TODO Auto-generated method stub
		return null;
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
			for (SubmissionEntry submissionTask : projects.get(project)) {
				Task foundTask = findTaskByNameProjectAndSystem(submissionTask.getName(), submissionTask.getProjectName(), submissionTask.getSystem());
				if (foundTask == null) {
					em.getTransaction().begin();
					Project foundProject = findProjectByNameAndSystem(submissionTask.getProjectName(), submissionTask.getSystem());
					if (foundProject == null) foundProject = new Project(submissionTask.getProjectName(), submissionTask.getSystem());
					em.persist(new Task(submissionTask.getName(), foundProject));
					em.getTransaction().commit();
				}
			}
		}		
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
		try {
			Task defaultTask = findTaskByNameProjectAndSystem(task, project, system);
			return new SubmissionEntry(defaultTask.getProject().getId(), defaultTask.getId(), defaultTask.getName(), defaultTask.getProject().getName(), system);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void openUrl(String openBrowser) {
	}
	
	public Project findProjectByNameAndSystem(String name, String system) {
		try {	
			return (Project) em.createQuery(
				"select p from Project p where p.name = :name "
				+ "and p.system = :system")
				.setParameter("name", name)
				.setParameter("system", system)
				.getSingleResult();
		} catch (Exception e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Task> findTasksByProjectAndSystem(String project, String system) {
		return em.createQuery(
				"select t from Task t " +
				"where t.project.name = :project " +
				"and t.project.system = :system")
				.setParameter("project", project)
				.setParameter("system", system)
				.getResultList();
	}
	
	public Task findTaskByNameProjectAndSystem(String name, String project, String system) {
		try {
			return (Task) em.createQuery(
				"select t from Task t " +
				"where t.name = :name " +
				"and t.project.name = :project " +
				"and t.project.system = :system")
				.setParameter("name", name)
				.setParameter("project", project)
				.setParameter("system", system)
				.getSingleResult();
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void handleYearChange(int lastWeek) {
		// TODO Auto-generated method stub
		
	}
}
