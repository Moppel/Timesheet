package com.uwusoft.timesheet.kimaisubmission;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
	private ILog logger;

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
	}

	@Override
	public List<String> getAssignedTasks() {
		// Todo implement
		List<String> assignedTasks = new ArrayList<String>();
		assignedTasks.add("Sample Task 1;Sample Project");
		assignedTasks.add("Sample Task 2;Sample Project");
		assignedTasks.add("Sample Task 3;Sample Project");
		return assignedTasks;
	}

	@Override
	public void submit(Date date, String task, String project, Double total) {
		// Todo implement
        logger.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "submit: "
        		+ new SimpleDateFormat("dd/MM/yyyy").format(date) + "\t" + task + " (" + project + ")" + "\t" + total));
	}
}
