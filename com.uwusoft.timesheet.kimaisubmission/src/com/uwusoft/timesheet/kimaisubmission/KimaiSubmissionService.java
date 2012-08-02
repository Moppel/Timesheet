package com.uwusoft.timesheet.kimaisubmission;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;

import com.kiwisoft.tires.kimai.KimaiConnection;
import com.kiwisoft.tires.kimai.model.KimaiServer;
import com.kiwisoft.tires.kimai.model.KimaiTask;
import com.kiwisoft.tires.kimai.model.KimaiTimeEntry;
import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.dialog.LoginDialog;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.extensionpoint.model.SubmissionEntry;
import com.uwusoft.timesheet.submission.model.SubmissionProject;
import com.uwusoft.timesheet.submission.model.SubmissionTask;
import com.uwusoft.timesheet.util.DesktopUtil;
import com.uwusoft.timesheet.util.MessageBox;
import com.uwusoft.timesheet.util.SecurePreferencesManager;

public class KimaiSubmissionService implements SubmissionService {

	public static final String PREFIX = "kimai.";
    private static final String dateFormat = "MM/dd/yyyy";
    private static final String timeFormat = "HH:mm";
    private static final String title = "Kimai";
    private KimaiServer server;
    private KimaiConnection connection;
	private ILog logger;
	private GregorianCalendar start;
	private Date lastDate;
    
	private boolean authenticate() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		SecurePreferencesManager secureProps = new SecurePreferencesManager(title);
		String userName = preferenceStore.getString(PREFIX + USERNAME);
		String password = secureProps.getProperty(PREFIX + PASSWORD + "." + userName);
		if (!StringUtils.isEmpty(userName) && !StringUtils.isEmpty(password)) {
			server.setUser(userName);
			server.setPassword(password);
			connection=new KimaiConnection(server);
			return true;
		}

		Display display = Display.getDefault();
		LoginDialog loginDialog = new LoginDialog(display, "Kimai Log in",	"", userName, password);
		if (loginDialog.open() == Dialog.OK) {
			server.setUser(loginDialog.getUser());
			server.setPassword(loginDialog.getPassword());
			connection=new KimaiConnection(server);
			preferenceStore.setValue(PREFIX + USERNAME, loginDialog.getUser());
			if (loginDialog.isStorePassword())
				secureProps.storeProperty(PREFIX + PASSWORD + "." + userName, loginDialog.getPassword());
			else
				secureProps.removeProperty(PREFIX + PASSWORD + "." + userName);
			return true;
		}
		return true;
	}
	
	public KimaiSubmissionService() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		logger = Activator.getDefault().getLog();
		server=new KimaiServer();
		server.setUrl(preferenceStore.getString(PREFIX + URL));
       	while (!authenticate());
		
		start = new GregorianCalendar();
		lastDate = new Date();
		resetStartTime(lastDate);
	}
	
	/**
	 * reset start time to 8 am
	 */
	private void resetStartTime(Date date) {
		start.setTime(date);
		start.set(Calendar.HOUR, 8);
		start.set(Calendar.AM_PM, 0);
		start.set(Calendar.MINUTE, 0);		
	}

	@Override
	public Map<String, SubmissionProject> getAssignedProjects() {
		Map<String, SubmissionProject> projects = new HashMap<String, SubmissionProject>();
		Set<KimaiTask> tasks;
		KimaiConnection connection=new KimaiConnection(server);
		try {
			tasks=connection.loadTasks();
			if (tasks==null) tasks = Collections.emptySet();
		} catch (IOException e) {
			return projects;
		}
		
		for (KimaiTask task : tasks) {
			String projectName = task.getProjectName() == null ? "" : task.getProjectName()
					+ (task.getCustomerName() == null ? "" : " (" + task.getCustomerName() + ")");
			if (projects.get(projectName) == null)
				projects.put(projectName, new SubmissionProject(task.getProjectId(), projectName));
			projects.get(projectName).addTask(new SubmissionTask(task.getEventId(), task.getEventName()));
		}
		return projects;
	}

	@Override
	public void submit(Date date, SubmissionEntry task, Double total) {
		if (task.getId() == null) return;
		
		if(!new SimpleDateFormat(dateFormat).format(date).equals(new SimpleDateFormat(dateFormat).format(lastDate)))
			resetStartTime(date);
		int hours = total.intValue();
		GregorianCalendar end = new GregorianCalendar();
		end.setTime(start.getTime());
		Double minutes = (total - hours) * 60.0;
		end.add(Calendar.HOUR, hours);
		end.add(Calendar.MINUTE, (int) Math.round(minutes));
		
		Long entryId = 0L;
		try {
			KimaiTimeEntry kmEntry=new KimaiTimeEntry();
			kmEntry.setStartDate(start.getTime());
			kmEntry.setEndDate(end.getTime());
			kmEntry.setProjectId(task.getProjectId());
			kmEntry.setEventId(task.getId());
			//kmEntry.setComment(entry.getDetails());
			entryId = connection.submitTimeEntry(kmEntry);
		} catch (IOException e) {
			MessageBox.setError(title, e.getLocalizedMessage());
		}
		logger.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "submit: entry id=" + entryId + " at "
        		+ new SimpleDateFormat(dateFormat).format(start.getTime()) + "\t" + task.getName() + " (" + task.getProjectName() + ")" + "\t"
        		+ new SimpleDateFormat(timeFormat).format(start.getTime()) + " - " + new SimpleDateFormat(timeFormat).format(end.getTime())));
		lastDate = date;
		start.setTime(end.getTime());
	}

	@Override
	public void openUrl() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		if (preferenceStore.getBoolean(PREFIX + OPEN_BROWSER))
			DesktopUtil.openUrl(preferenceStore.getString(PREFIX + URL));
	}
}
