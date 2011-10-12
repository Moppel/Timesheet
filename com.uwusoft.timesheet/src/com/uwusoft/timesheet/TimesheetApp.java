package com.uwusoft.timesheet;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.sql.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.model.Task;

/**
 * This class controls all aspects of the application's execution
 */
public class TimesheetApp implements IApplication {
	private static final String PERSISTENCE_UNIT_NAME = "timesheet";
	private static EntityManagerFactory factory;


	public static final String WORKING_HOURS = "weekly.workinghours";
    public static final String HOLIDAY_TASK = "task.holiday";
    public static final String VACATION_TASK = "task.vacation";
    public static final String SICK_TASK = "task.sick";
    public static final String DEFAULT_TASK = "task.default";
    public static final String DAILY_TASK = "task.daily";
    public static final String DAILY_TASK_TOTAL = "task.daily.total";
    public static final String LAST_TASK = "task.last";
    public static final String SYSTEM_SHUTDOWN = "system.shutdown";
    private final IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
    public static Date startDate;
    
	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) {
		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		EntityManager em = factory.createEntityManager();
		// Read the existing entries and write to console
		Query q = em.createQuery("select t from Task t order by t.dateTime asc");
		List<Task> taskList = q.getResultList();
		for (Task task : taskList) {
			System.out.println(task);
		}
		System.out.println("Size: " + taskList.size());

		// Create new todo
		em.getTransaction().begin();
		Task task = new Task();
		task.setDateTime(new Date(System.currentTimeMillis()));
		task.setTask("Nichts");
		task.setWholeDay(false);
		
		em.persist(task);
		em.getTransaction().commit();

		em.close();
		RuntimeMXBean mx = ManagementFactory.getRuntimeMXBean();
		startDate = new Date(mx.getStartTime());

		Display display = PlatformUI.createDisplay();
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
				preferenceStore.setValue(SYSTEM_SHUTDOWN, StorageService.formatter.format(System.currentTimeMillis()));
				if (!PlatformUI.isWorkbenchRunning()) return;
		    	final IWorkbench workbench = PlatformUI.getWorkbench();
		    	final Display display = workbench.getDisplay();
		    	if (!display.isDisposed()) {
					display.syncExec(new Runnable() {
						public void run() {
							if (!display.isDisposed()) {
								preferenceStore.setValue(SYSTEM_SHUTDOWN, StorageService.formatter.format(System.currentTimeMillis()));
								workbench.close();
							}
						}
					});
		    	}
		    }
		});
		int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
		if (returnCode == PlatformUI.RETURN_RESTART) {
			return IApplication.EXIT_RESTART;
		}
		return IApplication.EXIT_OK;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		if (!PlatformUI.isWorkbenchRunning())
			return;
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}
}
