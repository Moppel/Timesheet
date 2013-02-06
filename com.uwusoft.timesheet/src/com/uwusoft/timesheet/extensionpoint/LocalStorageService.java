package com.uwusoft.timesheet.extensionpoint;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.ui.PlatformUI;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.SystemShutdownTimeCaptureService;
import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.extensionpoint.model.DailySubmissionEntry;
import com.uwusoft.timesheet.extensionpoint.model.SubmissionEntry;
import com.uwusoft.timesheet.model.AllDayTasks;
import com.uwusoft.timesheet.model.Project;
import com.uwusoft.timesheet.model.Project_;
import com.uwusoft.timesheet.model.Task;
import com.uwusoft.timesheet.model.TaskEntry;
import com.uwusoft.timesheet.model.TaskEntry_;
import com.uwusoft.timesheet.model.Task_;
import com.uwusoft.timesheet.submission.model.SubmissionProject;
import com.uwusoft.timesheet.submission.model.SubmissionTask;
import com.uwusoft.timesheet.util.BusinessDayUtil;
import com.uwusoft.timesheet.util.ExtensionManager;
import com.uwusoft.timesheet.util.MessageBox;
import com.uwusoft.timesheet.util.StorageSystemSetup;

public class LocalStorageService extends EventManager implements ImportTaskService {

	private static final String PERSISTENCE_UNIT_NAME = "timesheet";
	public static EntityManagerFactory factory;	
	private static EntityManager em;
    private Map<String,String> submissionSystems;
    private Job firstImportJob, syncEntriesJob, syncTasksJob;
    private static LocalStorageService instance;
    private static StorageService storageService;
    private static IssueService jiraService;
    private ILog logger;
    private static ISchedulingRule mutex = new Mutex();

	private LocalStorageService() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		Calendar cal = new GregorianCalendar();
		cal.setTime(new Date());
		String timesheetName = StorageService.TIMESHEET_PREFIX + cal.get(Calendar.YEAR);
		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, getConfigOverrides(timesheetName));
		em = factory.createEntityManager();
				
		submissionSystems = TimesheetApp.getSubmissionSystems();
        logger = Activator.getDefault().getLog();
		
        if (StringUtils.isEmpty(preferenceStore.getString(StorageService.PROPERTY)))
			StorageSystemSetup.execute();
				
		final Date lastTaskEntryDate = getLastTaskEntryDate();
		final Date importedEndDate = importLastEntryDate(lastTaskEntryDate);
		
		firstImportJob = new Job("Importing entries") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {				
				if (getStorageService() == null)
					return Status.CANCEL_STATUS;				
				
				Calendar cal = GregorianCalendar.getInstance();
				cal.set(cal.get(Calendar.YEAR), Calendar.JANUARY, 1);
				Date startDate = cal.getTime();
				if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
					cal.set(cal.get(Calendar.YEAR), Calendar.JANUARY, 2);
					startDate = cal.getTime();
				}
				cal.setFirstDayOfWeek(Calendar.MONDAY);
				cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
				if (lastTaskEntryDate != null && getTaskEntries(startDate, cal.getTime()).size() > 0) // if already imported
			        return Status.OK_STATUS;
				
				cal.setTime(startDate);
				int startWeek = cal.get(Calendar.WEEK_OF_YEAR);
				cal.setTime(new Date());
				int endWeek = cal.get(Calendar.WEEK_OF_YEAR);
				monitor.beginTask("Import " + (endWeek - startWeek) + " weeks", endWeek - startWeek);
				for (int i = startWeek; i <= endWeek; i++) {
					cal.set(Calendar.WEEK_OF_YEAR, i);
					cal.setFirstDayOfWeek(Calendar.MONDAY);
					cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
					Date endDate = DateUtils.truncate(cal.getTime(), Calendar.DATE);
					if (endDate != null && importedEndDate != null && !endDate.before(importedEndDate))
						endDate = BusinessDayUtil.getPreviousBusinessDay(importedEndDate);
					if (startDate != null && startDate.after(endDate))
						startDate = endDate;
					List<TaskEntry> entries = storageService.getTaskEntries(startDate, endDate);
					if (entries.isEmpty()) break;
					logger.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "import task entries for week " + i));
					for (TaskEntry entry : entries) {
						entry.setRowNum(entry.getId());
						entry.setSyncStatus(true);
						logger.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "import task entry: " + entry));
						createTaskEntry(entry, false);
					}
					cal.set(Calendar.WEEK_OF_YEAR, i + 1);
					cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
					startDate = DateUtils.truncate(cal.getTime(), Calendar.DATE);
					monitor.worked(1);
				}
				getJiraService();
				monitor.done();
				firePropertyChangeEvent(new PropertyChangeEvent(this, PROPERTY_WEEK, null, cal.get(Calendar.WEEK_OF_YEAR) - 2));
		        return Status.OK_STATUS;
			}
		};
		//if (importedEndDate != null) {
			firstImportJob.setRule(mutex);
			firstImportJob.schedule();
		//}
		
		syncEntriesJob = new Job("Synchronizing entries") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (getStorageService() == null)
					return Status.CANCEL_STATUS;
				boolean active = false;
				try {
					synchronized (em) {
						if (em.getTransaction().isActive())	active = true;
						else em.getTransaction().begin();
						CriteriaBuilder criteria = em.getCriteriaBuilder();
						CriteriaQuery<TaskEntry> query = criteria.createQuery(TaskEntry.class);
						Root<TaskEntry> taskEntry = query.from(TaskEntry.class);
						query.where(criteria.notEqual(taskEntry.get(TaskEntry_.syncStatus), true));
						query.orderBy(criteria.asc(taskEntry.get(TaskEntry_.dateTime)));
						List<TaskEntry> entries = em.createQuery(query).getResultList();

						monitor.beginTask("Synchronize " + entries.size() + " entries", entries.size());
						Calendar cal = new GregorianCalendar();
						cal.setFirstDayOfWeek(Calendar.MONDAY);
						TaskEntry lastEntry = getLastTaskEntry();
						int startDay = 0;
						int startWeek = 0;
						int endDay = 0;
						int endWeek = 0;
						int startYear = 0;
						int endYear = 0;
						for (TaskEntry entry : entries) {
							if (lastEntry != null && lastEntry.getDateTime() != null) {
								cal.setTime(lastEntry.getDateTime());
								startDay = cal.get(Calendar.DAY_OF_YEAR);
								startWeek = cal.get(Calendar.WEEK_OF_YEAR);
								startYear = cal.get(Calendar.YEAR);
							}
							if (entry.getDateTime() != null) {
								cal.setTime(entry.getDateTime());
								endDay = cal.get(Calendar.DAY_OF_YEAR);
								endWeek = cal.get(Calendar.WEEK_OF_YEAR);								
								endYear = cal.get(Calendar.YEAR);
							}
							else {
								endDay = startDay;
								endWeek = startWeek;
								endYear = startYear;
							}
							
							if (entry.getRowNum() == null) { 
								if (lastEntry != null && !lastEntry.isAllDay() && startDay != 0 && startDay < endDay)
									storageService.handleDayChange();
								if (startWeek != 0 && startWeek < endWeek)
									storageService.handleWeekChange();
								if (startYear != 0 && startYear != endYear) {
									if (!active) em.getTransaction().commit();
									handleYearChange(startWeek); // TODO
									if (em.getTransaction().isActive())	active = true;
									else em.getTransaction().begin();
								}
								entry.setRowNum(storageService.createTaskEntry(entry));
								calculateTotal(entry);
							}
							else
								storageService.updateTaskEntry(entry);
							entry.setSyncStatus(true);
							em.persist(entry);
							monitor.worked(1);
							lastEntry = entry;
						}
					}
					monitor.done();
				} catch (CoreException e) {
					MessageBox.setError("Remote storage service", e.getMessage());
					return Status.CANCEL_STATUS;
				}
				if (!active) em.getTransaction().commit();
				//firePropertyChangeEvent(new PropertyChangeEvent(this, PROPERTY_WEEK, null, endWeek);
		        return Status.OK_STATUS;
			}			
		};
		syncEntriesJob.setRule(mutex);
		
		syncTasksJob = new Job("Synchronizing tasks") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (getStorageService() == null)
					return Status.CANCEL_STATUS;

				synchronized (em) {
					for (String system : submissionSystems.keySet()) {
						if (!StringUtils.isEmpty(system)) {
							em.getTransaction().begin();
							CriteriaBuilder criteria = em.getCriteriaBuilder();
							CriteriaQuery<Task> query = criteria.createQuery(Task.class);
							Root<Task> rootTask = query.from(Task.class);
							Path<Project> rootProject = rootTask.get(Task_.project);
							query.where(criteria.and(criteria.notEqual(rootTask.get(Task_.syncStatus), true),
									criteria.equal(rootProject.get(Project_.system), system)));
							List<Task> tasks = em.createQuery(query).getResultList();

							Map<String, SubmissionProject> submissionProjects = new HashMap<String, SubmissionProject>();
							for (Task task : tasks) {
								if (task.getExternalId() == null) continue;
								SubmissionProject submissionProject = submissionProjects.get(task.getProject().getName());
								if (submissionProject == null)
									submissionProject = new SubmissionProject(task.getProject().getExternalId(), task.getProject().getName());
								submissionProject.addTask(new SubmissionTask(task.getExternalId(), task.getName()));
								submissionProjects.put(submissionProject.getName(),	submissionProject);
							}
							if (!submissionProjects.isEmpty()) {
								if (storageService.importTasks(system, submissionProjects.values()))
									for (Task task : tasks) {
										task.setSyncStatus(true);
										task.getProject().setSyncStatus(true);
										em.persist(task);
									}
							}
							em.getTransaction().commit();
						}
					}
				}
				return Status.OK_STATUS;
			}
		};
		syncTasksJob.setRule(mutex);
	}

	private Map<String, Object> getConfigOverrides(String timesheetName) {
		Map<String, Object> configOverrides = new HashMap<String, Object>();
		String dataBasePath;
		if (Activator.googleDrive.exists() && Activator.getDefault().getPreferenceStore() instanceof PreferenceStore)
			dataBasePath = Activator.timesheetPath + "/Databases/" + timesheetName;
		else
			dataBasePath = SystemShutdownTimeCaptureService.lckDir + "/databases/" + timesheetName;
		configOverrides.put("javax.persistence.jdbc.url", "jdbc:derby:" + dataBasePath + ";create=true");
		return configOverrides;
	}

	private Date importLastEntryDate(final Date lastTaskEntryDate) {
		if (lastTaskEntryDate == null) {
			synchronized (em) {
				em.getTransaction().begin();
				CriteriaBuilder criteria = em.getCriteriaBuilder();
				CriteriaQuery<Task> taskQuery = criteria.createQuery(Task.class);
				Root<Task> taskRoot = taskQuery.from(Task.class);
			
				taskQuery.where(criteria.equal(taskRoot.get(Task_.name), CHECK_IN));
				List<Task> tasks = em.createQuery(taskQuery).getResultList(); 
				if (tasks.isEmpty()) {
					Task task = new Task(CHECK_IN);
					task.setSyncStatus(true);
					em.persist(task);
				}
				taskQuery.where(criteria.equal(taskRoot.get(Task_.name), BREAK));
				tasks = em.createQuery(taskQuery).getResultList();
				if (tasks.isEmpty()) {
					Task task = new Task(BREAK);
					task.setSyncStatus(true);
					em.persist(task);
				}
				taskQuery.where(criteria.equal(taskRoot.get(Task_.name), AllDayTasks.BEGIN_ADT));
				tasks = em.createQuery(taskQuery).getResultList();
				if (tasks.isEmpty()) {
					Task task = new Task(AllDayTasks.BEGIN_ADT);
					task.setSyncStatus(true);
					em.persist(task);
				}
				em.getTransaction().commit();
			}
			for (String system : submissionSystems.keySet()) {
				if (!StringUtils.isEmpty(system) && getProjects(system).isEmpty()) {
					if (getStorageService() != null)
						importTasks(system, storageService.getImportedProjects(system), true);
				}
			}
			if (getStorageService() != null) {
				TaskEntry lastTask = storageService.getLastTask();
				if (lastTask != null) {
					lastTask.setRowNum(lastTask.getId());
					lastTask.setSyncStatus(true);
					createTaskEntry(lastTask);
				}
				Date date = storageService.getLastTaskEntryDate();
				if (date != null) {
					List<TaskEntry> entries = storageService.getTaskEntries(date, date);
					for (TaskEntry entry : entries) {
						entry.setRowNum(entry.getId());
						entry.setSyncStatus(true);
						createTaskEntry(entry);
					}
					return DateUtils.truncate(date, Calendar.DATE);
				}
			}
		}
		else {
			if (getStorageService() != null) {
				TaskEntry lastTask = storageService.getLastTask();
				if (lastTask != null && getLastTask() != null && !lastTask.getId().equals(getLastTask().getRowNum())) {
					lastTask.setRowNum(lastTask.getId());
					lastTask.setSyncStatus(true);
					createTaskEntry(lastTask);
					Date date = storageService.getLastTaskEntryDate();
					if (date != null && date.after(lastTaskEntryDate)) {
						List<TaskEntry> entries = storageService.getTaskEntries(lastTaskEntryDate, date);
						synchronized (entries) {
							for (TaskEntry entry : entries) {
								entry.setRowNum(entry.getId());
								entry.setSyncStatus(true);
								createOrUpdate(entry);
								logger.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "import task entry: " + entry));
							}
						}
					}
					return DateUtils.truncate(date, Calendar.DATE);
				}
			}
		}
		return getLastTaskEntryDate();
	}
    
    public static LocalStorageService getInstance() {
    	if (instance == null)
    		instance = new LocalStorageService();
    	return instance;
    }
	
	public List<String> getProjects(String system) {
		List<String> projects = new ArrayList<String>();
		for (Project project : getProjectList(system)) projects.add(project.getName());
		return projects;
	}

	private List<Project> getProjectList(String system) {
		CriteriaBuilder criteria = em.getCriteriaBuilder();
		CriteriaQuery<Project> query = criteria.createQuery(Project.class);
		Root<Project> project = query.from(Project.class);
		query.where(criteria.equal(project.get(Project_.system), system));
		return em.createQuery(query).getResultList();
	}

	public Collection<SubmissionProject> getImportedProjects(String system) {
		List<SubmissionProject> projects = new ArrayList<SubmissionProject>();
		for (Project project : getProjectList(system)) {
			SubmissionProject submissionProject = new SubmissionProject(project.getExternalId(), project.getName());
			for (Task task : project.getTasks())
				submissionProject.addTask(new SubmissionTask(task.getExternalId(), task.getName()));
			projects.add(submissionProject);
		}
		return projects;
	}
	
	public List<String> findTasksBySystemAndProject(String system, String project) {
		List<Task> taskList = findTasksByProjectAndSystem(project, system);
		List<String> tasks = new ArrayList<String>();
		for (Task task : taskList) tasks.add(task.getName());
		return tasks;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		addListenerObject(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		removeListenerObject(listener);
	}

    protected void firePropertyChangeEvent(final PropertyChangeEvent event) {
		if (event == null) {
			throw new NullPointerException();
		}

        synchronized (getListeners()) {
    		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
    			public void run() {
    				if (!PlatformUI.getWorkbench().getDisplay().isDisposed())
    					for (Object listener : getListeners()) {
    						((PropertyChangeListener) listener).propertyChange(event);
    					}
    			}
        	});    
        }
    }
    
	public List<TaskEntry> getTaskEntries(Date startDate, Date endDate) {
		CriteriaBuilder criteria = em.getCriteriaBuilder();
		CriteriaQuery<TaskEntry> query = criteria.createQuery(TaskEntry.class);
		Root<TaskEntry> entry = query.from(TaskEntry.class);
		Path<Task> task = entry.get(TaskEntry_.task);
		query.where(criteria.and(criteria.notEqual(task.get(Task_.name), AllDayTasks.BEGIN_ADT),
				criteria.greaterThanOrEqualTo(entry.get(TaskEntry_.dateTime), new Timestamp(startDate.getTime())),
				criteria.lessThanOrEqualTo(entry.get(TaskEntry_.dateTime), new Timestamp(endDate.getTime()))));
		query.orderBy(criteria.asc(entry.get(TaskEntry_.dateTime)));
		return em.createQuery(query).getResultList();
	}

	public String[] getUsedCommentsForTask(String task, String project,	String system) {
		Set<String> comments = new HashSet<String>();
		CriteriaBuilder criteria = em.getCriteriaBuilder();
		CriteriaQuery<TaskEntry> query = criteria.createQuery(TaskEntry.class);
		Root<TaskEntry> taskEntry = query.from(TaskEntry.class);
		Path<Task> rootTask = taskEntry.get(TaskEntry_.task);
		Path<Project> rootProject = rootTask.get(Task_.project);
		query.where(criteria.and(criteria.equal(rootTask.get(Task_.name), task),
				criteria.equal(rootProject.get(Project_.name), project),
				criteria.equal(rootProject.get(Project_.system), system)));
		List<TaskEntry> results = em.createQuery(query).getResultList();
		for (TaskEntry entry : results)
			if (entry.getComment() != null)
				comments.add(entry.getComment());
		return comments.toArray(new String[comments.size()]);
	}

	public void createTaskEntry(TaskEntry task) {
		createTaskEntry(task, true);
	}
	
	private void createTaskEntry(TaskEntry entry, boolean firePropertyChangeEvent) {
		boolean active = false;
		synchronized (entry) {
			if (em.getTransaction().isActive())	active = true;
			else em.getTransaction().begin();
			entry.setTask(findTaskByNameProjectAndSystem(entry.getTask().getName(),
					entry.getTask().getProject() == null ? null : entry.getTask().getProject().getName(),
							entry.getTask().getProject() == null ? null : entry.getTask().getProject().getSystem()));
			em.persist(entry);
			if (!active) em.getTransaction().commit();
		}
		if (firePropertyChangeEvent) {
	        Calendar cal = new GregorianCalendar();
	        cal.setTime(entry.getDateTime() == null ? new Date() : entry.getDateTime());
			firePropertyChangeEvent(new PropertyChangeEvent(this, PROPERTY_WEEK, null, cal.get(Calendar.WEEK_OF_YEAR)));
		}
	}

	public void updateTaskEntry(TaskEntry entry) {
		boolean active = false;
        Calendar cal = new GregorianCalendar();
		synchronized (entry) {
			if (em.getTransaction().isActive())	active = true;
			else em.getTransaction().begin();
			entry.setSyncStatus(false);
			if (calculateTotal(entry))
				cal.setTime(entry.getDateTime());
			else cal.setTime(new Date());
			em.persist(entry);
			if (!active) em.getTransaction().commit();
		}
		firePropertyChangeEvent(new PropertyChangeEvent(this, PROPERTY_WEEK, null, cal.get(Calendar.WEEK_OF_YEAR)));
	}

	private boolean calculateTotal(TaskEntry entry) {
		if (entry.getDateTime() == null || entry.isAllDay() || CHECK_IN.equals(entry.getTask().getName()) || BREAK.equals(entry.getTask().getName()))
			return false;
		try {
			CriteriaBuilder criteria = em.getCriteriaBuilder();
			CriteriaQuery<TaskEntry> query = criteria.createQuery(TaskEntry.class);
			Root<TaskEntry> taskEntry = query.from(TaskEntry.class);
			query.where(criteria.equal(taskEntry.get(TaskEntry_.rowNum), entry.getRowNum() - 1));
			TaskEntry previousEntry = (TaskEntry) em.createQuery(query).getSingleResult();
			entry.setTotal((entry.getDateTime().getTime() - previousEntry.getDateTime().getTime()) / 1000f / 60f / 60f);
			query.where(criteria.equal(taskEntry.get(TaskEntry_.rowNum), entry.getRowNum() + 1));
			TaskEntry nextEntry = (TaskEntry) em.createQuery(query).getSingleResult();
			if (nextEntry.getDateTime() != null && !nextEntry.isAllDay() && !CHECK_IN.equals(nextEntry.getTask().getName()) && !BREAK.equals(nextEntry.getTask().getName()))
				nextEntry.setTotal((nextEntry.getDateTime().getTime() - entry.getDateTime().getTime()) / 1000f / 60f / 60f);
		} catch (Exception e) {}
		return true;
	}

    protected void createOrUpdate(TaskEntry entry) {
		CriteriaBuilder criteria = em.getCriteriaBuilder();
		CriteriaQuery<TaskEntry> query = criteria.createQuery(TaskEntry.class);
		Root<TaskEntry> taskEntry = query.from(TaskEntry.class);
		query.where(criteria.equal(taskEntry.get(TaskEntry_.rowNum), entry.getId()));
		List<TaskEntry> availableEntries = em.createQuery(query).getResultList();
		if (availableEntries.size() == 1) {
			TaskEntry availableEntry = availableEntries.iterator().next();
			synchronized (availableEntry) {
				em.getTransaction().begin();
				availableEntry.setDateTime(entry.getDateTime());
				availableEntry.setTask(findTaskByNameProjectAndSystem(entry.getTask().getName(),
						entry.getTask().getProject() == null ? null : entry.getTask().getProject().getName(),
								entry.getTask().getProject() == null ? null : entry.getTask().getProject().getSystem()));
				calculateTotal(availableEntry);
				availableEntry.setComment(entry.getComment());
				em.persist(availableEntry);
				em.getTransaction().commit();
			}
		}
		else {
			if (availableEntries.size() > 1) {
				synchronized (availableEntries) {
					em.getTransaction().begin();
					for (TaskEntry availableEntry : availableEntries)
						em.remove(availableEntry);
					em.getTransaction().commit();
				}
			}
			entry.setRowNum(entry.getId());
			createTaskEntry(entry);
		}
	}

	/** 
	 * @return the last (incomplete) task entry
	 */
    public TaskEntry getLastTask() {
		CriteriaBuilder criteria = em.getCriteriaBuilder();
		CriteriaQuery<TaskEntry> query = criteria.createQuery(TaskEntry.class);
		Root<TaskEntry> entry = query.from(TaskEntry.class);
		query.where(criteria.isNull(entry.get(TaskEntry_.dateTime)));
		query.orderBy(criteria.desc(entry.get(TaskEntry_.id)));
		List<TaskEntry> taskEntries = em.createQuery(query).getResultList();
		if (taskEntries.isEmpty()) return null;
		return taskEntries.iterator().next();
	}

	public Date getLastTaskEntryDate() {
		TaskEntry entry = getLastTaskEntry();
		if (entry == null) return null;
		return entry.getDateTime();
	}
	
	/** 
	 * @return the last complete task entry
	 */
	public TaskEntry getLastTaskEntry() {
		CriteriaBuilder criteria = em.getCriteriaBuilder();
		CriteriaQuery<TaskEntry> query = criteria.createQuery(TaskEntry.class);
		Root<TaskEntry> entry = query.from(TaskEntry.class);
		query.where(criteria.and(entry.get(TaskEntry_.rowNum).isNotNull(), // only synchronized
				entry.get(TaskEntry_.dateTime).isNotNull()));
		query.orderBy(criteria.desc(entry.get(TaskEntry_.dateTime)));
		List<TaskEntry> taskEntries = em.createQuery(query).getResultList();
		if (taskEntries.isEmpty()) return null;
		return taskEntries.iterator().next();		
	}

	public void handleYearChange(int lastWeek) {
		if (getStorageService() == null) return;
		storageService.handleYearChange(lastWeek);
		if (lastWeek != 0) {
			CriteriaBuilder criteria = em.getCriteriaBuilder();
			CriteriaQuery<Task> query = criteria.createQuery(Task.class);
			List<Task> tasks = em.createQuery(query).getResultList();
			Calendar cal = new GregorianCalendar();
			cal.setTime(new Date());
			String year = "" + cal.get(Calendar.YEAR); 
			cal.add(Calendar.YEAR, 1);
			factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, getConfigOverrides(StorageService.TIMESHEET_PREFIX + cal.get(Calendar.YEAR)));
			em = factory.createEntityManager();
			for (Task task : tasks) {
				Project project = task.getProject();
				em.getTransaction().begin();
				if (project == null) {
					em.persist(task);
					em.getTransaction().commit();
					continue;
				}
				if (project.getName().contains(year)) {
					// roll over tasks and projects to new year
					SubmissionService submissionService = new ExtensionManager<SubmissionService>(SubmissionService.SERVICE_ID).getService(
							TimesheetApp.getSubmissionSystems().get(project.getSystem()));
					Map<String, SubmissionProject> assignedProjects = submissionService.getAssignedProjects();
					SubmissionProject submissionProject = assignedProjects.get(project.getName().replaceFirst("(.*)" + year + "(.*)", "$1" + cal.get(Calendar.YEAR) + "$2"));
					if (submissionProject != null) {
						List<SubmissionTask> tasksToRemove = new ArrayList<SubmissionTask>();
						for (SubmissionTask submissionTask : submissionProject.getTasks()) {							
							if (!task.getName().equals(submissionTask.getName()))
								tasksToRemove.add(submissionTask);
						}
						for (SubmissionTask taskToRemove : tasksToRemove) submissionProject.removeTask(taskToRemove);
						Project foundProject = findProjectByNameAndSystem(submissionProject.getName(), project.getSystem());
						if (foundProject == null) {
							foundProject = new Project(submissionProject.getName(), project.getSystem());
							foundProject.setExternalId(submissionProject.getId());
							em.persist(foundProject);
						}
						for (SubmissionTask submissionTask : submissionProject.getTasks()) {
							Task newTask = new Task(submissionTask.getName(), foundProject);
							newTask.setExternalId(submissionTask.getId());
							em.persist(newTask);
						}
						em.getTransaction().commit();
						continue;
					}
				}
				if (findProjectByNameAndSystem(project.getName(), project.getSystem()) == null)
					em.persist(project);
				task.setSyncStatus(false);
				em.persist(task);
				em.getTransaction().commit();
			}
			syncTasksJob.schedule();
		}
	}

	public boolean importTasks(String submissionSystem, Collection<SubmissionProject> projects) {
		importTasks(submissionSystem, projects, false);
		return true;
	}
	
	private void importTasks(String submissionSystem, Collection<SubmissionProject> projects, boolean isSynchronized) {
		for (SubmissionProject submissionProject : projects) {
			for (SubmissionTask submissionTask : submissionProject.getTasks()) {
				Task foundTask = findTaskByNameProjectAndSystem(submissionTask.getName(), submissionProject.getName(), submissionSystem);
				if (foundTask == null) {
					synchronized (em) {
						em.getTransaction().begin();
						Project foundProject = findProjectByNameAndSystem(submissionProject.getName(), submissionSystem);
						if (foundProject == null) {
							foundProject = new Project(submissionProject.getName(), submissionSystem);
							foundProject.setExternalId(submissionProject.getId());
							em.persist(foundProject);
							if (isSynchronized) foundProject.setSyncStatus(true);
						}
						Task task = new Task(submissionTask.getName(), foundProject);
						task.setExternalId(submissionTask.getId());
						if (isSynchronized) task.setSyncStatus(true);
					
						logger.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "Import task: " + submissionTask.getName() + " id=" + submissionTask.getId()
								+ " (" + submissionProject.getName() + " id=" + submissionProject.getId() + ") "));
					
						em.persist(task);
						em.getTransaction().commit();
					}
				}
			}
		}
		if (!isSynchronized)
			syncTasksJob.schedule();
	}

	public Set<String> submitEntries(Date startDate, Date endDate) {
		waitUntilJobsFinished();
        Set<String> systems = new HashSet<String>();
		synchronized (em) {
			em.getTransaction().begin();
			CriteriaBuilder criteria = em.getCriteriaBuilder();
			CriteriaQuery<TaskEntry> query = criteria.createQuery(TaskEntry.class);
			Root<TaskEntry> entry = query.from(TaskEntry.class);
			Path<Task> task = entry.get(TaskEntry_.task);
			query.where(criteria.and(criteria.notEqual(entry.get(TaskEntry_.status), true),
					criteria.notEqual(task.get(Task_.name), AllDayTasks.BEGIN_ADT),
					//criteria.notEqual(task.get(Task_.name), CHECK_IN),
					//criteria.notEqual(task.get(Task_.name), BREAK),
					entry.get(TaskEntry_.dateTime).isNotNull(),
					criteria.greaterThanOrEqualTo(entry.get(TaskEntry_.dateTime), new Timestamp(startDate.getTime())),
					criteria.lessThanOrEqualTo(entry.get(TaskEntry_.dateTime), new Timestamp(endDate.getTime()))));
			query.orderBy(criteria.asc(entry.get(TaskEntry_.dateTime)));
		
			List<TaskEntry> entries = em.createQuery(query).getResultList();
			if (entries.isEmpty()) return systems;
        
			Date lastDate = DateUtils.truncate(entries.iterator().next().getDateTime(), Calendar.DATE);
			DailySubmissionEntry submissionEntry = new DailySubmissionEntry(lastDate);
        
			for (TaskEntry taskEntry : entries) {
				Date date = DateUtils.truncate(taskEntry.getDateTime(), Calendar.DATE);
				if (!date.equals(lastDate)) { // another day
					submissionEntry.submitEntries();
					submissionEntry = new DailySubmissionEntry(date);
					lastDate = date;
				}
				String system = taskEntry.getTask().getProject() == null ? null : taskEntry.getTask().getProject().getSystem();
				if (submissionSystems.containsKey(system)) {
					systems.add(system);
					SubmissionEntry submissionTask = new SubmissionEntry(taskEntry.getTask().getProject().getExternalId(), taskEntry.getTask().getExternalId(),
							taskEntry.getTask().getName(), taskEntry.getTask().getProject().getName(), system);
					submissionEntry.addSubmissionEntry(submissionTask, taskEntry.getTotal());
				}
				taskEntry.setStatus(true);
				taskEntry.setSyncStatus(false);
				em.persist(taskEntry);
			}
			submissionEntry.submitEntries();
			em.getTransaction().commit();
		}
		return systems;
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
			return new SubmissionEntry(defaultTask.getProject().getExternalId(), defaultTask.getExternalId(), defaultTask.getName(), defaultTask.getProject().getName(), system);
		} catch (Exception e) {
			return null;
		}
	}
	public void openUrl(String openBrowser) {
		if (getStorageService() == null) return;
		storageService.openUrl(openBrowser);
	}
	
	public Project findProjectByNameAndSystem(String name, String system) {
		try {	
			CriteriaBuilder criteria = em.getCriteriaBuilder();
			CriteriaQuery<Project> query = criteria.createQuery(Project.class);
			Root<Project> project = query.from(Project.class);
			query.where(criteria.and(criteria.equal(project.get(Project_.name), name),
					criteria.equal(project.get(Project_.system), system)));
			return (Project) em.createQuery(query).getSingleResult();
		} catch (Exception e) {
			return null;
		}
	}
	
	public List<Task> findTasksByProjectAndSystem(String project, String system) {
		CriteriaBuilder criteria = em.getCriteriaBuilder();
		CriteriaQuery<Task> query = criteria.createQuery(Task.class);
		Root<Task> task = query.from(Task.class);
		Path<Project> proj = task.get(Task_.project);
		query.where(criteria.and(criteria.equal(proj.get(Project_.name), project),
				criteria.equal(proj.get(Project_.system), system)));
		return em.createQuery(query).getResultList();
	}
	
	public Task findTaskByNameProjectAndSystem(String name, String project, String system) {
		CriteriaBuilder criteria = em.getCriteriaBuilder();
		CriteriaQuery<Task> query = criteria.createQuery(Task.class);
		Root<Task> task = query.from(Task.class);
		Predicate p = criteria.equal(task.get(Task_.name), name);
		if (project == null)
			query.where(p);
		else {
			Path<Project> proj = task.get(Task_.project);
			query.where(criteria.and(p, criteria.equal(proj.get(Project_.name), project),
					criteria.equal(proj.get(Project_.system), system)));
		}
		try {
			return em.createQuery(query).getSingleResult();
		} catch (Exception e) {
			return null;
		}
	}

	public void synchronize() {
		if (getStorageService() == null) return;
		syncEntriesJob.schedule();
	}
	
	public void reload() {
		if (getStorageService() == null) return;
		storageService.reload();		
	}
	
	private StorageService getStorageService() {
		//checks for connection to the internet through dummy request (http://stackoverflow.com/q/1139547)
        try {
            URL url = new URL("http://www.google.com"); // TODO storageService.getServiceUrlString()
            //open a connection to that source
            HttpURLConnection urlConnect = (HttpURLConnection)url.openConnection();
            urlConnect.setConnectTimeout(1000);
            //trying to retrieve data from the source. If there
            //is no connection, this line will fail
            urlConnect.getContent();
        } catch (UnknownHostException e) {
        	MessageBox.setError("Storage service", e.getMessage());
            return null;
        }
        catch (IOException e) {
        	MessageBox.setError("Storage service", e.getMessage());
            return null;
        }
		if (storageService == null) {
			storageService = new ExtensionManager<StorageService>(StorageService.SERVICE_ID)
					.getService(Activator.getDefault().getPreferenceStore().getString(StorageService.PROPERTY));
			if (storageService == null) MessageBox.setError("Storage service", "Can't reach remote storage service");
		}
		return storageService;
	}
	
	private IssueService getJiraService() {
		if (jiraService == null) {
			jiraService = new ExtensionManager<IssueService>(IssueService.SERVICE_ID)
					.getService(Activator.getDefault().getPreferenceStore().getString(IssueService.PROPERTY));
			if (jiraService == null) MessageBox.setError("Jira service", "Can't reach remote Jira service");
		}
		return jiraService;
	}
	
	public void waitUntilJobsFinished() {
		try {
			firstImportJob.join();
			syncTasksJob.join();
			syncEntriesJob.join();
		} catch (InterruptedException e) {
			MessageBox.setError("Remote storage service", e.getMessage());
		}
	}
	
	// see http://stackoverflow.com/a/9110269: 
	private static class Mutex implements ISchedulingRule {
        public boolean contains(ISchedulingRule rule) {
            return (rule == this);
        }

        public boolean isConflicting(ISchedulingRule rule) {
            return (rule == this);
        }
	}
}
