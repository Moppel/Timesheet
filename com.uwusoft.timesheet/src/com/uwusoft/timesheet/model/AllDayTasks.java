package com.uwusoft.timesheet.model;

import java.sql.Timestamp;
import java.text.DateFormatSymbols;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

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
		CriteriaBuilder criteria = em.getCriteriaBuilder();
		CriteriaQuery<TaskEntry> query = criteria.createQuery(TaskEntry.class);
		Root<TaskEntry> entry = query.from(TaskEntry.class);
		query.where(criteria.greaterThan(entry.get(TaskEntry_.dateTime), new Date()));
		query.orderBy(criteria.desc(entry.get(TaskEntry_.dateTime)));
		List<TaskEntry> taskEntryList = em.createQuery(query).getResultList();
		Date begin;
		storageService = LocalStorageService.getInstance();
		if (taskEntryList.isEmpty()) {
			begin = BusinessDayUtil.getNextBusinessDay(new Date(), false);
			em.getTransaction().begin();
			query = criteria.createQuery(TaskEntry.class);
			Path<Task> task = entry.get(TaskEntry_.task);
			query.where(criteria.equal(task.get(Task_.name), BEGIN_ADT));
			List<TaskEntry> beginTaskEntries = em.createQuery(query).getResultList();
			for (TaskEntry beginTask : beginTaskEntries) {
				em.remove(beginTask);
			}
			Task beginTask = storageService.findTaskByNameProjectAndSystem(BEGIN_ADT, null, null);
			if (beginTask == null) {
				beginTask = new Task(BEGIN_ADT);
				em.persist(beginTask);
			}
			TaskEntry taskEntry = new TaskEntry(begin, beginTask);
			taskEntry.setSyncStatus(true);
			em.persist(taskEntry);
			em.getTransaction().commit();
		}
		else
			begin = BusinessDayUtil.getNextBusinessDay(taskEntryList.iterator().next().getDateTime(), false);
		nextBegin = begin;
		
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		total = new Float(preferenceStore.getInt(TimesheetApp.WORKING_HOURS) /
				(new DateFormatSymbols().getWeekdays().length - 1
				- preferenceStore.getString(TimesheetApp.NON_WORKING_DAYS).split(SubmissionService.separator).length));
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
		taskEntry.setSyncStatus(true);
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
		em.getTransaction().begin();
		CriteriaBuilder criteria = em.getCriteriaBuilder();
		CriteriaQuery<TaskEntry> query = criteria.createQuery(TaskEntry.class);
		Root<TaskEntry> entry = query.from(TaskEntry.class);
		query.where(criteria.and(criteria.equal(entry.get(TaskEntry_.allDay), true),
				criteria.greaterThan(entry.get(TaskEntry_.dateTime), new Date())));
		query.orderBy(criteria.asc(entry.get(TaskEntry_.dateTime)));
		List<TaskEntry> taskEntryList = em.createQuery(query).getResultList();
		if (taskEntryList.isEmpty()) return;
		
		query = criteria.createQuery(TaskEntry.class);
		Path<Task> task = entry.get(TaskEntry_.task);
		query.where(criteria.equal(task.get(Task_.name), BEGIN_ADT));
		List<TaskEntry> beginTaskEntryList = em.createQuery(query).getResultList();
		if (beginTaskEntryList.isEmpty()) return;
		
		TaskEntry beginTaskEntry = beginTaskEntryList.iterator().next();
		Date begin = beginTaskEntry.getDateTime();
		em.remove(beginTaskEntry);
		Date end = BusinessDayUtil.getNextBusinessDay(lastDate, true); // create missing holidays and handle week change
		
		for (TaskEntry taskEntry : taskEntryList) {
			end = new Date(taskEntry.getDateTime().getTime());
			do {
				storageService.createTaskEntry(new TaskEntry(new Timestamp(begin.getTime()), taskEntry.getTask(), taskEntry.getTotal(), true));
				// MessageBox.setMessage("Set whole day task", begin + "\n" + taskEntry); // TODO create confirm dialog
			} while (!(begin = BusinessDayUtil.getNextBusinessDay(begin, true)).after(end));
			em.remove(taskEntry);
		}
		em.getTransaction().commit();
		storageService.synchronize();
	}

	public float getTotal() {
		return total;
	}	
}
