package com.uwusoft.timesheet;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * This class controls all aspects of the application's execution
 */
public class TimesheetApp implements IApplication {

    public static SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    
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
    public static String startTime;
    
	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) {
		RuntimeMXBean mx = ManagementFactory.getRuntimeMXBean();
		startTime = formatter.format(mx.getStartTime());

		Display display = PlatformUI.createDisplay();
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
				preferenceStore.setValue(SYSTEM_SHUTDOWN, formatter.format(System.currentTimeMillis()));
				if (!PlatformUI.isWorkbenchRunning()) return;
		    	final IWorkbench workbench = PlatformUI.getWorkbench();
		    	final Display display = workbench.getDisplay();
		    	if (!display.isDisposed()) {
					display.syncExec(new Runnable() {
						public void run() {
							if (!display.isDisposed()) {
								preferenceStore.setValue(SYSTEM_SHUTDOWN, formatter.format(System.currentTimeMillis()));
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
