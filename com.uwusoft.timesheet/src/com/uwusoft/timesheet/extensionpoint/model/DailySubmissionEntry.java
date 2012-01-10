package com.uwusoft.timesheet.extensionpoint.model;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.preference.IPreferenceStore;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.extensionpoint.model.TaskSubmissionEntry;
import com.uwusoft.timesheet.model.Task;
import com.uwusoft.timesheet.util.ExtensionManager;

/**
 * container for all {@link com.uwusoft.timesheet.TaskSubmissionEntry}'s for one day 
 *
 * @author Uta Wunderlich
 * @version $Revision: $, $Date: Aug 17, 2011
 * @since Aug 17, 2011
 */
public class DailySubmissionEntry {
    private Date date;
    private Map<String, TaskSubmissionEntry> entries;

    public DailySubmissionEntry(Date date) {
        this.date = date;
        entries = new HashMap<String, TaskSubmissionEntry>();
    }

    public void addSubmissionEntry(SubmissionTask task, double total, SubmissionService service) {
        String key = task.getName() + task.getProjectName(); // has to be unique
    	TaskSubmissionEntry available = entries.get(key);
        if (available == null)
        	entries.put(key, new TaskSubmissionEntry(task, total, service));
        else available.addTotal(total);
    }

    public void submitEntries() {
        for (TaskSubmissionEntry entry : entries.values()) {
            entry.submit(date);
        }
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		if (!StringUtils.isEmpty(preferenceStore.getString(TimesheetApp.DAILY_TASK))) {
            Task task = TimesheetApp.createTask(date, TimesheetApp.DAILY_TASK);
			StorageService storageService = new ExtensionManager<StorageService>(StorageService.SERVICE_ID)
					.getService(preferenceStore.getString(StorageService.PROPERTY));
			storageService.submitTask(date, task, Double.parseDouble(preferenceStore.getString(TimesheetApp.DAILY_TASK_TOTAL)));
		}    
    }
}
