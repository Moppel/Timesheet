package com.uwusoft.timesheet.model;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.preference.IPreferenceStore;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.util.BusinessDayUtil;
import com.uwusoft.timesheet.util.ExtensionManager;
import com.uwusoft.timesheet.util.MessageBox;

public class WholeDayTasks {
	public static final String BEGIN_WDT="BEGIN_WDT";
	private static WholeDayTasks instance;
	private Date begin, nextBegin;
	private float total;
	private IPreferenceStore preferenceStore;
	private EntityManager em;
	
	public static WholeDayTasks getInstance() {
		if (instance == null) instance = new WholeDayTasks();
		return instance;
	}
	
	private WholeDayTasks() {
		begin = BusinessDayUtil.getNextBusinessDay(new Date()); // TODO date of last task entry
		nextBegin = begin;
		
		preferenceStore = Activator.getDefault().getPreferenceStore(); 
		total = new Float(preferenceStore.getInt(TimesheetApp.WORKING_HOURS) / 5); // TODO define non working days
		
		em = TimesheetApp.factory.createEntityManager();
		
		em.getTransaction().begin();
		Query q = em.createQuery("select t from Task t where t.name='" + BEGIN_WDT + "'");
		Task task = new Task(BEGIN_WDT);
		@SuppressWarnings("unchecked")
		List<Task> taskList = q.getResultList();
		if (taskList.isEmpty()) em.persist(task);
		else task = taskList.iterator().next();
		
		em.persist(new TaskEntry(begin, task));
		em.getTransaction().commit();
	}

	public void addNextTask(Date to, String name) {
		em.getTransaction().begin();
		String[] tasks = name.split(SubmissionService.separator);
		Project project = new Project();
        if (tasks.length > 2) {
        	project.setName(tasks[1]);
        	project.setSystem(tasks[2]);
        }
		Query q = em.createQuery("select p from Project p where p.name='" + project.getName()
				+ "' and p.system ='" + project.getSystem() + "'");
		@SuppressWarnings("unchecked")
		List<Project> projectList = q.getResultList();
		if (projectList.isEmpty()) em.persist(project);
		else project = projectList.iterator().next();
		/*else {
			Iterator<Project> iterator = projectList.iterator();
			project = iterator.next();
			while (iterator.hasNext())
				em.remove(iterator.next());
		}*/
		
		q = em.createQuery("select t from Task t where t.name='" + tasks[0]
				+ "' and t.project.name ='" + project.getName() + "' and t.project.system ='" + project.getSystem() + "'");
		Task task = new Task(tasks[0], project);
		@SuppressWarnings("unchecked")
		List<Task> taskList = q.getResultList();
		if (taskList.isEmpty()) em.persist(task);
		else task = taskList.iterator().next();
		
		TaskEntry taskEntry = new TaskEntry(to, task, total, true);
		taskEntry.getTask().setProject(project);
		em.persist(taskEntry);
		em.getTransaction().commit();
		
		nextBegin = BusinessDayUtil.getNextBusinessDay(to);
	}	
	
	/**
	 * @return next business day after end date of all stored whole day tasks
	 */
	public Date getNextBegin() {
		return nextBegin;
	}

	public void createTaskEntries() {
		StorageService storageService = new ExtensionManager<StorageService>(StorageService.SERVICE_ID)
				.getService(preferenceStore.getString(StorageService.PROPERTY));
		Query q = em.createQuery("select t from TaskEntry t where t.wholeDay=true order by t.dateTime asc");
		@SuppressWarnings("unchecked")
		List<TaskEntry> taskEntryList = q.getResultList();
		if (taskEntryList.isEmpty()) return;
		
		q = em.createQuery("select t from TaskEntry t where t.task.name='" + BEGIN_WDT + "' order by t.dateTime asc");
		
		em.getTransaction().begin();
		for (Object beginTask : q.getResultList()) { // should be only a single result but who knows ...
			begin = new Date(((TaskEntry) beginTask).getDateTime().getTime());
			em.remove(beginTask);
		}		
		for (TaskEntry taskEntry : taskEntryList) {
			Date end = new Date(taskEntry.getDateTime().getTime());
			do {
				if (BusinessDayUtil.isAnotherWeek())
					storageService.storeLastWeekTotal(preferenceStore.getString(TimesheetApp.WORKING_HOURS)); // store Week and Overtime
				taskEntry.setDateTime(new Timestamp(begin.getTime()));
				storageService.createTaskEntry(taskEntry);
				if (!StringUtils.isEmpty(preferenceStore.getString(TimesheetApp.DAILY_TASK))) {
					String[] dailyTask = preferenceStore.getString(TimesheetApp.DAILY_TASK).split(SubmissionService.separator);
					Project project = new Project();
	                if (dailyTask.length > 2) {
	                	project.setName(dailyTask[1]);
	                	project.setSystem(dailyTask[2]);
	                }
	        		
	                q = em.createQuery("select p from Project p where p.name='" + project.getName()
	        				+ "' and p.system ='" + project.getSystem() + "'");
	        		@SuppressWarnings("unchecked")
	        		List<Project> projectList = q.getResultList();
	        		if (projectList.isEmpty()) em.persist(project);
	        		else project = projectList.iterator().next();
	        		
	        		q = em.createQuery("select t from Task t where t.name='" + dailyTask[0]
	        				+ "' and t.project.name ='" + project.getName() + "' and t.project.system ='" + project.getSystem() + "'");
	        		Task task = new Task(dailyTask[0], project);
	        		@SuppressWarnings("unchecked")
	        		List<Task> taskList = q.getResultList();
	        		if (taskList.isEmpty()) em.persist(task);
	        		else task = taskList.iterator().next();
                	
	        		storageService.createTaskEntry(new TaskEntry(begin, task,
                			Float.parseFloat(preferenceStore.getString(TimesheetApp.DAILY_TASK_TOTAL))));
				}
				MessageBox.setMessage("Set whole day task", begin + "\n" + taskEntry); // TODO create confirm dialog
			} while (!(begin = BusinessDayUtil.getNextBusinessDay(begin)).after(end));
			em.remove(taskEntry);
		}
		em.getTransaction().commit();
	}
}
