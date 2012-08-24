package com.uwusoft.timesheet.extensionpoint.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.uwusoft.timesheet.extensionpoint.LocalStorageService;

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
	private static LocalStorageService storageService = LocalStorageService.getInstance();

    public DailySubmissionEntry(Date date) {
        this.date = date;
        entries = new HashMap<String, TaskSubmissionEntry>();
    }

    public void addSubmissionEntry(SubmissionEntry task, double total) {
        String key = task.getName() + task.getProjectName(); // has to be unique
    	TaskSubmissionEntry available = entries.get(key);
        if (available == null)
        	entries.put(key, new TaskSubmissionEntry(task, total));
        else available.addTotal(total);
    }

    public void submitEntries() {
        for (TaskSubmissionEntry entry : entries.values()) {
            entry.submit(date);
        }
		storageService.submitFillTask(date);
    }
}
