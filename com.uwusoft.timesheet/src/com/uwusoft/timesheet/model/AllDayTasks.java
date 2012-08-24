package com.uwusoft.timesheet.model;

import java.sql.Timestamp;
import java.text.DateFormatSymbols;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.eclipse.jface.preference.IPreferenceStore;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.util.BusinessDayUtil;

public class AllDayTasks {
	private static AllDayTasks instance;
	public static String BEGIN_ADT = "BEGIN_ADT";
	private static LocalStorageService storageService;
	public static String[] allDayTasks = {TimesheetApp.VACATION_TASK, TimesheetApp.TIL_TASK, TimesheetApp.SICK_TASK, TimesheetApp.HOLIDAY_TASK};
	private Date nextBegin;
	private float total;
	private EntityManager em;
	
	public static AllDayTasks getInstance() {
		if (instance == null) instance = new AllDayTasks();
		return instance;
	}
	
	private AllDayTasks() {
		em = LocalStorageService.factory.createEntityManager();
		@SuppressWarnings("unchecked")
		List<TaskEntry> taskEntryList = em.createQuery("select t from TaskEntry t where t.allDay=true" +
				" and t.syncStatus <> :status" +
				" order by t.dateTime desc")
				.setParameter("status", true)
				.getResultList();
		Date begin;
		if (taskEntryList.isEmpty()) {
			begin = BusinessDayUtil.getNextBusinessDay(new Date(), false);
			em.getTransaction().begin();
			@SuppressWarnings("unchecked")
			List<TaskEntry> beginTaskEntries = em.createQuery("select t from TaskEntry t where t.task.name=:name")
					.setParameter("name", BEGIN_ADT)
					.getResultList();
			for (TaskEntry beginTask : beginTaskEntries) {
				em.remove(beginTask);
			}
			Task beginTask = (Task) em.createQuery("select t from Task t where t.name = :name")
				.setParameter("name", BEGIN_ADT)
				.getSingleResult();
			if (beginTask == null) {
				beginTask = new Task(BEGIN_ADT);
				em.persist(beginTask);
			}
			em.persist(new TaskEntry(begin, beginTask));
			em.getTransaction().commit();
		}
		else
			begin = BusinessDayUtil.getNextBusinessDay(taskEntryList.iterator().next().getDateTime(), false);
		nextBegin = begin;
		
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		total = new Float(preferenceStore.getInt(TimesheetApp.WORKING_HOURS) /
				(new DateFormatSymbols().getWeekdays().length - 1
				- preferenceStore.getString(TimesheetApp.NON_WORKING_DAYS).split(SubmissionService.separator).length));
		
		storageService = LocalStorageService.getInstance();
	}

	public void addNextTask(Date to, String name) {
		em.getTransaction().begin();
		Task task = TimesheetApp.createTask(name);
		Project project = storageService.findProjectByNameAndSystem(task.getProject().getName(), task.getProject().getSystem());
		if (project == null) em.persist(task.getProject());
		else task.setProject(project);
		
		Task foundTask = storageService.findTaskByNameProjectAndSystem(
				task.getName(), task.getProject().getName(), task.getProject().getSystem());
		if (foundTask == null) em.persist(task);
		else task = foundTask;
		
		TaskEntry taskEntry = new TaskEntry(to, task, total, true);
		em.persist(taskEntry);
		em.getTransaction().commit();
		
		nextBegin = BusinessDayUtil.getNextBusinessDay(to, false);
	}	
	
	/**
	 * @return next business day after end date of all stored whole day tasks
	 */
	public Date getNextBegin() {
		return nextBegin;
	}

	public void createTaskEntries(Date lastDate) {
		@SuppressWarnings("unchecked")
		List<TaskEntry> taskEntryList = em.createQuery("select t from TaskEntry t where t.allDay=true order by t.dateTime asc" +
				" and t.syncStatus <> :status" +
				" order by t.dateTime desc")
				.setParameter("status", true)
				.getResultList();
		if (taskEntryList.isEmpty()) return;
		
		@SuppressWarnings("unchecked")
		List<TaskEntry> beginTaskEntryList = em.createQuery("select t from TaskEntry t where t.task.name = :name")
				.setParameter("name", BEGIN_ADT)
				.getResultList();
		if (beginTaskEntryList.isEmpty()) return;
		
		em.getTransaction().begin();
		TaskEntry beginTaskEntry = beginTaskEntryList.iterator().next();
		Date begin = beginTaskEntry.getDateTime();
		em.remove(beginTaskEntry);
		Date end = BusinessDayUtil.getNextBusinessDay(lastDate, true); // create missing holidays and handle week change
		
		for (TaskEntry taskEntry : taskEntryList) {
			end = new Date(taskEntry.getDateTime().getTime());
			do {
				taskEntry.setDateTime(new Timestamp(begin.getTime()));
				storageService.createTaskEntry(taskEntry);
				// MessageBox.setMessage("Set whole day task", begin + "\n" + taskEntry); // TODO create confirm dialog
			} while (!(begin = BusinessDayUtil.getNextBusinessDay(begin, true)).after(end));
			em.remove(taskEntry);
		}
		em.getTransaction().commit();
	}

	public float getTotal() {
		return total;
	}	
}
