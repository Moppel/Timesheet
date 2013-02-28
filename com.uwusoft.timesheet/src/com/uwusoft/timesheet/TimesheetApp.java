package com.uwusoft.timesheet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.persistence.config.SystemProperties;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.uwusoft.timesheet.extensionpoint.AllDayTaskService;
import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.model.Project;
import com.uwusoft.timesheet.model.Task;
import com.uwusoft.timesheet.util.BusinessDayUtil;
import com.uwusoft.timesheet.util.MessageBox;

/**
 * This class controls all aspects of the application's execution
 */
public class TimesheetApp implements IApplication {

	public static final String WORKING_HOURS = "weekly.workinghours";
	public static final String NON_WORKING_DAYS = "weekly.nonworkingdays";
    public static final String HOLIDAY_TASK = AllDayTaskService.PREFIX + "Holiday";
    public static final String VACATION_TASK = AllDayTaskService.PREFIX + "Vacation";
    public static final String SICK_TASK = AllDayTaskService.PREFIX + "Sick";
    public static final String TIL_TASK = AllDayTaskService.PREFIX + "Til";
    public static final String DEFAULT_TASK = "task.default";
    public static final String DAILY_TASK = "task.daily";
    public static final String DAILY_TASK_TOTAL = "task.daily.total";
    public static Date startDate, shutDownDate;
	public static File settings;
    
	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) {
		RuntimeMXBean mx = ManagementFactory.getRuntimeMXBean();
		startDate = new Date(mx.getStartTime());
		shutDownDate = BusinessDayUtil.getPreviousBusinessDay(startDate);

		Activator.getDefault().getPreferenceStore();
		String settingsPath;
		if (Activator.settingsPath == null)
			settingsPath = Activator.getDefault().getStateLocation().toPortableString()
				.replaceFirst(".metadata/.plugins/.*", ".metadata/.plugins/org.eclipse.core.runtime/.settings/"); // how to get path of settings?
		else
			settingsPath = Activator.settingsPath + File.separator;
		
		File tmp = new File(settingsPath + SystemShutdownTimeCaptureService.tmpFile);
		try {
			if (tmp.exists()) {
				BufferedReader time = new BufferedReader(new InputStreamReader(new FileInputStream(tmp)));
				String line = time.readLine();
				time.close();
				if (line == null)
					shutDownDate = new Date(tmp.lastModified());
				else
					shutDownDate = SystemShutdownTimeCaptureService.formatter.parse(line);
			} else
				tmp.createNewFile();
		} catch (ParseException e) {
		} catch (IOException e) {
		}
		
		// see http://stackoverflow.com/a/4194224:
		try {
			final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
			// see http://stackoverflow.com/a/579167
			final File currentJar = new File(new URI(null, SystemShutdownTimeCaptureService.class.getProtectionDomain().getCodeSource().getLocation().toString(), null));
			final List<String> command = new ArrayList<String>();
			command.add(javaBin);
			command.add("-jar");
			command.add(currentJar.getPath());
			command.add(settingsPath);

			final ProcessBuilder builder = new ProcessBuilder(command);
			builder.start();
		} catch (IOException e) {
			MessageBox.setError("Couldn't start shutdown service", e.getMessage());
		} catch (URISyntaxException e) {
			MessageBox.setError("Couldn't start shutdown service", e.getMessage());
		}
		
		try {
			RandomAccessFile lockFile = new RandomAccessFile(new File(SystemShutdownTimeCaptureService.lckDir
					+ File.separator + "timesheet.lck"), "rw");
	        FileChannel channel = lockFile.getChannel();
	        FileLock lock = channel.tryLock();
	        if (lock == null) {
	        	MessageBox.setMessage("Error", "Another instance of " + Activator.PLUGIN_ID + " is already running.");
	        	stop();
	            System.exit(0);
	        }
		} catch (IOException e) {
		}
		
        System.setProperty(SystemProperties.ARCHIVE_FACTORY, MyArchiveFactoryImpl.class.getName()); // see http://stackoverflow.com/a/7982008
		
		Display display = PlatformUI.createDisplay();
		
		int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
		if (returnCode == PlatformUI.RETURN_RESTART) {
			return IApplication.EXIT_RESTART;
		}
		LocalStorageService.getInstance().waitUntilJobsFinished();
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

	public static Map<String, String> getSubmissionSystems() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();		
		String[] systems = preferenceStore.getString(SubmissionService.PROPERTY).split(SubmissionService.separator);
		Map<String,String> submissionSystems = new HashMap<String, String>();
		for (String system : systems) {
			if (!StringUtils.isEmpty(system)) {
				String descriptiveName = getDescriptiveName(system, SubmissionService.SERVICE_NAME);
				submissionSystems.put(descriptiveName, system);
			}
		}
		return submissionSystems;
	}

	public static String getDescriptiveName(String system, String serviceName) {
		return Character.toUpperCase(system.toCharArray()[system.lastIndexOf('.') + 1])
				+ system.substring(system.lastIndexOf('.') + 2, system.indexOf(serviceName));
	}
	
	public static String getTaskName(String propertyName) {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();		
		String[] task = preferenceStore.getString(propertyName).split(SubmissionService.separator);
		if (task.length > 2) {
			return getTaskName(task[0], task[1], task[2]); 
		}
		else return preferenceStore.getString(propertyName);
	}
	
	public static String getTaskName(String task, String project, String system) {
		return task + (project == null ? "" : ("\nProject: " + project + "\nSystem: " + system));
	}
	
	public static Task createTask(String propertyName) {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();		
		String[] task = preferenceStore.getString(propertyName).split(SubmissionService.separator);
		if (task.length < 2) task = propertyName.split(SubmissionService.separator);
		Project project = new Project();
		if (task.length > 2) {
			project.setName(task[1]);
			project.setSystem(task[2]);
		}
		return new Task(task[0], project);
	}
}
