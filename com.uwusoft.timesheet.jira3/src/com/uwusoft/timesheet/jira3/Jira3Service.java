package com.uwusoft.timesheet.jira3;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.dialog.LoginDialog;
import com.uwusoft.timesheet.dialog.PreferencesDialog;
import com.uwusoft.timesheet.extensionpoint.JiraService;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.util.SecurePreferencesManager;

public class Jira3Service implements JiraService {
	public static final String PREFIX = "jira3.";
	
    private String message;
    
	public Jira3Service() throws CoreException {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
        if (StringUtils.isEmpty(preferenceStore.getString(PREFIX + SubmissionService.URL))
        		|| StringUtils.isEmpty(preferenceStore.getString(PREFIX + StorageService.USERNAME))) {
        	PreferencesDialog preferencesDialog;
        	do
        		preferencesDialog = new PreferencesDialog(Display.getDefault(), "com.uwusoft.timesheet.jira3.Jira3PreferencePage");
        	while (preferencesDialog.open() != Dialog.OK);
            boolean lastSuccess = true;
            do lastSuccess = authenticate(lastSuccess);
           	while (!lastSuccess);
        }
	}

    private boolean authenticate(boolean lastSuccess) throws CoreException {
		final IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		final SecurePreferencesManager secureProps = new SecurePreferencesManager("Google");
    	final String userName = preferenceStore.getString(PREFIX + StorageService.USERNAME);
    	final String password = secureProps.getProperty(PREFIX + StorageService.PASSWORD);
    	if (lastSuccess && !StringUtils.isEmpty(userName) && !StringUtils.isEmpty(password)) {
        	// TODO login
        	return true;
    	}
    	
    	final Display display = Display.getDefault();
    	display.syncExec(new Runnable() {
			@Override
			public void run() {
		    	LoginDialog loginDialog = new LoginDialog(display, "Jira Log in", message, userName, password);
				if (loginDialog.open() == Dialog.OK) {
		        	message = null;
					// TODO service.setUserCredentials(loginDialog.getUser(), loginDialog.getPassword());
		        	preferenceStore.setValue(PREFIX + StorageService.USERNAME, loginDialog.getUser());
		        	if (loginDialog.isStorePassword())
		        		secureProps.storeProperty(PREFIX + StorageService.PASSWORD, loginDialog.getPassword());
		        	else
		        		secureProps.removeProperty(PREFIX + StorageService.PASSWORD);
				}
				else message = "Not logged in";
			}
    	});
    	if (message == null) return true;
        throw new CoreException(new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.ERROR, message, null));
   }
}
