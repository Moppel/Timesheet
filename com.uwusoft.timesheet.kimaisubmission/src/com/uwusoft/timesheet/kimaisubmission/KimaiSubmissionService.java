package com.uwusoft.timesheet.kimaisubmission;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import com.uwusoft.timesheet.extensionpoint.model.SubmissionTask;
import com.uwusoft.timesheet.util.DesktopUtil;
import com.uwusoft.timesheet.util.MessageBox;
import com.uwusoft.timesheet.util.SecurePreferencesManager;

public class KimaiSubmissionService implements SubmissionService {

	public static final String PREFIX = "kimai.";
    private static final String dateFormat = "MM/dd/yyyy";
    private static final String timeFormat = "HH:mm";
    private static final String title = "Kimai";
	private ILog logger;
	private GregorianCalendar start;
	private Date lastDate;
    
	private boolean authenticate() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		SecurePreferencesManager secureProps = new SecurePreferencesManager(title);
		String userName = preferenceStore.getString(PREFIX + USERNAME);
		String password = secureProps.getProperty(PREFIX + PASSWORD + "." + userName);
		if (!StringUtils.isEmpty(userName) && !StringUtils.isEmpty(password)) {
			// TODO implement
			return true;
		}

		Display display = Display.getDefault();
		LoginDialog loginDialog = new LoginDialog(display, "Kimai Log in",	"", userName, password);
		if (loginDialog.open() == Dialog.OK) {
			// TODO implement
			preferenceStore.setValue(PREFIX + USERNAME, loginDialog.getUser());
			if (loginDialog.isStorePassword())
				secureProps.storeProperty(PASSWORD + "." + userName, loginDialog.getPassword());
			else
				secureProps.removeProperty(PREFIX + PASSWORD + "." + userName);
			return true;
		}
		return true;
	}
	
	public KimaiSubmissionService() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		logger = Activator.getDefault().getLog();
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
	public Map<String, Set<SubmissionTask>> getAssignedProjects() {
		Map<String, Set<SubmissionTask>> projects = new HashMap<String, Set<SubmissionTask>>();
		// TODO implement
		return projects;
	}

	@Override
	public void submit(Date date, SubmissionTask task, Double total) {
		if (task.getId() == null) return;
		
		if(!new SimpleDateFormat(dateFormat).format(date).equals(new SimpleDateFormat(dateFormat).format(lastDate)))
			resetStartTime(date);
		int hours = total.intValue();
		GregorianCalendar end = new GregorianCalendar();
		end.setTime(start.getTime());
		Double minutes = (total - hours) * 60.0;
		end.add(Calendar.HOUR, hours);
		end.add(Calendar.MINUTE, (int) Math.round(minutes));
		
		// TODO implement
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
