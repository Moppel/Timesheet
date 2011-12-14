package com.uwusoft.timesheet;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * This class controls all aspects of the application's execution
 */
public class TimesheetApp implements IApplication {
	private static final String PERSISTENCE_UNIT_NAME = "timesheet";
	public static EntityManagerFactory factory;

	public static final String WORKING_HOURS = "weekly.workinghours";
    public static final String HOLIDAY_TASK = "task.holiday";
    public static final String VACATION_TASK = "task.vacation";
    public static final String SICK_TASK = "task.sick";
    public static final String DEFAULT_TASK = "task.default";
    public static final String DAILY_TASK = "task.daily";
    public static final String DAILY_TASK_TOTAL = "task.daily.total";
    public static final String LAST_TASK = "task.last";
    public static final String SYSTEM_SHUTDOWN = "system.shutdown";
    public static final String SYSTEM_START= "system.start";
    public static Date startDate;
    
	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) {
		Map<String, Object> configOverrides = new HashMap<String, Object>();
		configOverrides.put("javax.persistence.jdbc.url",
				"jdbc:derby:" + System.getProperty("user.home") + "/.eclipse/databases/timesheet;create=true");
		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, configOverrides);
		
		RuntimeMXBean mx = ManagementFactory.getRuntimeMXBean();
		startDate = new Date(mx.getStartTime());

		Display display = PlatformUI.createDisplay();
		/*try {
			Runtime.getRuntime().exec("java -jar Timesheet.jar");
		} catch (IOException e) {
			MessageBox.setError("Couldn't start shutdown service", e.getLocalizedMessage());
		}*/
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
