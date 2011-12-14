package com.uwusoft.timesheet.kimaisubmission;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.dialog.LoginDialog;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.util.SecurePreferencesManager;

public class KimaiSubmissionService implements SubmissionService {

	public static final String USERNAME = "kimai.user.name";
	public static final String PASSWORD = "kimai.user.password";
    private static final String dateFormat = "MM/dd/yyyy";
    private static final String timeFormat = "HH:mm";
	private ILog logger;
	private GregorianCalendar start;
	private Date lastDate;
	private Map<String, List<String>> projects;

    static {
       	while (!authenticate());
    }
    
	private static boolean authenticate() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		SecurePreferencesManager secureProps = new SecurePreferencesManager("Kimai");
		String userName = preferenceStore.getString(USERNAME);
		String password = secureProps.getProperty(PASSWORD);
		if (!StringUtils.isEmpty(userName) && !StringUtils.isEmpty(password)) {
			// service.setUserCredentials(userName, password);
			return true;
		}

		Display display = Display.getDefault();
		LoginDialog loginDialog = new LoginDialog(display, "Kimai Log in",	"", userName, password);
		if (loginDialog.open() == Dialog.OK) {
			// service.setUserCredentials(loginDialog.getUser(), loginDialog.getPassword());
			preferenceStore.setValue(USERNAME, loginDialog.getUser());
			if (loginDialog.isStorePassword())
				secureProps.storeProperty(PASSWORD, loginDialog.getPassword());
			else
				secureProps.removeProperty(PASSWORD);
			return true;
		}
		/*Display.getDefault().dispose();
		System.exit(1);
		return false;*/
		return true;
	}
	
	public KimaiSubmissionService() {
		logger = Activator.getDefault().getLog();
		start = new GregorianCalendar();
		resetStartTime();
		lastDate = new Date();
		projects = new HashMap<String, List<String>>();
	}
	
	/**
	 * reset start time to 8 am
	 */
	private void resetStartTime() {
		start.set(Calendar.HOUR, 8);
		start.set(Calendar.AM_PM, 0);
		start.set(Calendar.MINUTE, 0);		
	}

	@Override
	public Map<String, List<String>> getAssignedProjects() {
		// TODO implement
		projects.put("Overhead", new ArrayList<String>());
		projects.put("WebPac", new ArrayList<String>());
		projects.get("Overhead").add("General Administration & Time Entry");
		projects.get("WebPac").add("Core Development WP");
		projects.get("WebPac").add("General Project Meetings WP");
		return projects;
	}

	@Override
	public void submit(Date date, String task, String project, Double total) {
		if(!new SimpleDateFormat(dateFormat).format(date).equals(new SimpleDateFormat(dateFormat).format(lastDate)))
			resetStartTime();
		int hours = total.intValue();
		GregorianCalendar end = new GregorianCalendar();
		end.setTime(start.getTime());
		Double minutes = (total - hours) * 60.0;
		end.add(Calendar.HOUR, hours);
		end.add(Calendar.MINUTE, (int) Math.round(minutes));
		
		// TODO implement
		logger.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "submit: "
        		+ new SimpleDateFormat(dateFormat).format(date) + "\t" + task + " (" + project + ")" + "\t"
        		+ new SimpleDateFormat(timeFormat).format(start.getTime()) + " - " + new SimpleDateFormat(timeFormat).format(end.getTime())));

		lastDate = date;
		start.setTime(end.getTime());
	}
}
