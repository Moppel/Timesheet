package com.uwusoft.timesheet;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.uwusoft.timesheet.dialog.CheckoutCheckinDialog;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.util.ExtensionManager;
import com.uwusoft.timesheet.util.PropertiesUtil;

/**
 * This class controls all aspects of the application's execution
 */
public class TimesheetApp implements IApplication {

    private SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) {
		Display display = PlatformUI.createDisplay();
		try {
			RuntimeMXBean mx = ManagementFactory.getRuntimeMXBean();
			String startTime = formatter.format(mx.getStartTime());

			PropertiesUtil props = new PropertiesUtil(this.getClass(), "Timesheet");
			String shutdownTime = props.getProperty("system.shutdown");
			if (shutdownTime == null) shutdownTime = startTime;
			else shutdownTime = formatter.format(formatter.parse(shutdownTime));

			props.storeProperty("system.start", startTime);

			Calendar calDay = Calendar.getInstance();
			Calendar calWeek = new GregorianCalendar();
			int shutdownDay = 0;
			int shutdownWeek = 0;
			Date startDate = formatter.parse(startTime);
			calDay.setTime(startDate);
			calWeek.setTime(startDate);
			int startDay = calDay.get(Calendar.DAY_OF_YEAR);
			int startWeek = calWeek.get(Calendar.WEEK_OF_YEAR);
			// automatic check out
			if (props.getProperty("task.last") != null) { // last task will be set to null if a manual check out has occurred
				Date shutdownDate = formatter.parse(shutdownTime);
				calDay.setTime(shutdownDate);
				shutdownDay = calDay.get(Calendar.DAY_OF_YEAR);
				calWeek.setTime(shutdownDate);
				shutdownWeek = calWeek.get(Calendar.WEEK_OF_YEAR);
				if (startDay != shutdownDay) { // don't automatically check in/out if computer is rebooted
					CheckoutCheckinDialog checkoutCheckinDialog = new CheckoutCheckinDialog(new Shell(display), shutdownDate, startDate);
					if (checkoutCheckinDialog.open() == Dialog.OK) {
						StorageService storageService = new ExtensionManager<StorageService>(StorageService.SERVICE_ID).getService(props.getProperty(StorageService.PROPERTY));
						if (storageService == null) return IApplication.EXIT_OK;
						storageService.storeTimeEntry(checkoutCheckinDialog.getCheckoutTime(), props.getProperty("task.last"));
						storageService.storeLastDailyTotal();
						if (props.getProperty("task.daily") != null)
							storageService.storeTimeEntry(
									checkoutCheckinDialog.getCheckoutTime(), props.getProperty("task.daily"), props.getProperty("task.daily.total"));
						// automatic check in
						if (startWeek != shutdownWeek)
							storageService.storeLastWeekTotal(props.getProperty("weekly.workinghours")); // store Week and Overtime
						storageService.storeTimeEntry(checkoutCheckinDialog.getCheckinTime(), StorageService.CHECK_IN);
						props.storeProperty("task.last", props.getProperty("task.default"));
					}
				}
			}
			int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART) {
				return IApplication.EXIT_RESTART;
			}
	        ShutdownHook shutdownHook = new ShutdownHook();
	        Runtime.getRuntime().addShutdownHook(shutdownHook);
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

    private void helpAndTerminate(String message) {
        System.out.println(message);
    	// displayError(message);
        System.exit(1);
    }

    class ShutdownHook extends Thread {
        public void run() {
			PropertiesUtil props = new PropertiesUtil(this.getClass(), "Timesheet");
            try {
				props.storeProperty("system.shutdown", formatter.format(System.currentTimeMillis()));
			} catch (IOException e) {
				helpAndTerminate(e.getMessage());
			}
        }
    }
}
