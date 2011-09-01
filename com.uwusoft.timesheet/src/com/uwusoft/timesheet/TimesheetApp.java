package com.uwusoft.timesheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.util.PropertiesUtil;

/**
 * This class controls all aspects of the application's execution
 */
public class TimesheetApp implements IApplication, ISafeRunnable {

    private StorageService storageService;
    private SimpleDateFormat formatter;

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) {
		Display display = PlatformUI.createDisplay();
		try {
			run();
			int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART) {
				return IApplication.EXIT_RESTART;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			display.dispose();
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

	@Override
	public void handleException(Throwable exception) {
		System.out.println("Exception in client " + exception.getMessage());
	}

	@Override
	public void run() throws Exception {
		IConfigurationElement[] config = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(StorageService.SERVICE_ID);
		try {
			for (IConfigurationElement e : config) {
				System.out.println("Evaluating extension "
						+ e.getContributor().getName());
				final Object o = e.createExecutableExtension("class");
				if (o instanceof StorageService) {
					storageService = (StorageService) o;
					formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");
					RuntimeMXBean mx = ManagementFactory.getRuntimeMXBean();
					String startTime = formatter.format(mx.getStartTime());

					PropertiesUtil props = new PropertiesUtil("Timesheet");
					String shutdownTime = formatter.format(formatter.parse(props.getProperty("system.shutdown")));

					props.storeProperty("system.start", startTime);

					Calendar calDay = Calendar.getInstance();
					Calendar calWeek = new GregorianCalendar();
					int shutdownDay = 0;
					int shutdownWeek = 0;
					// automatic check out
					if (props.getProperty("task.last") != null) { // last task will be set to null if a
																			// manual check out has occurred
						Date shutdownDate = formatter.parse(shutdownTime);
						calDay.setTime(shutdownDate);
						shutdownDay = calDay.get(Calendar.DAY_OF_YEAR);
						calWeek.setTime(shutdownDate);
						shutdownWeek = calWeek.get(Calendar.WEEK_OF_YEAR);
						storageService.storeTimeEntry(shutdownDate,	props.getProperty("task.last"));
						storageService.storeLastDailyTotal();
						if (props.getProperty("task.daily") != null)
							storageService.storeTimeEntry(shutdownDate,
									props.getProperty("task.daily"),
									props.getProperty("task.daily.total"));
					}
					// automatic check in
					Date startDate = formatter.parse(startTime);
					calDay.setTime(startDate);
					calWeek.setTime(startDate);
					int startDay = calDay.get(Calendar.DAY_OF_YEAR);
					int startWeek = calWeek.get(Calendar.WEEK_OF_YEAR);
					if (startDay != shutdownDay) { // don't automatically check in/out if computer is rebooted
						if (startWeek != shutdownWeek)
							storageService.storeLastWeekTotal(); // store Week and Overtime
						storageService.storeTimeEntry(startDate, StorageService.CHECK_IN);
						props.storeProperty("task.last", props.getProperty("task.default"));
					}
					// advisor.getAdvisor().displayMessage(shutdownTime +
					// System.getProperty("line.separator") + startTime);
				}
			}
		} catch (CoreException ex) {
            helpAndTerminate(ex.getMessage());
		} catch (java.io.IOException e) {
            helpAndTerminate(e.getMessage());
        } catch (ParseException e) {
            helpAndTerminate(e.getMessage());
        }
	}

    private void helpAndTerminate(String message) {
        System.out.println(message);
    	// displayError(message);
        System.exit(1);
    }
}
