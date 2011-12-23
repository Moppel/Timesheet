package com.uwusoft.timesheet.extensionpoint.model;

import java.util.*;

import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.extensionpoint.model.TaskSubmissionEntry;

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

    public void addSubmissionEntry(String task, String project, double total, SubmissionService service) {
        String key = task + project; // has to be unique
    	TaskSubmissionEntry available = entries.get(key);
        if (available == null)
        	entries.put(key, new TaskSubmissionEntry(task, project, total, service));
        else available.addTotal(total);
    }

    public void submitEntries() {
        for (TaskSubmissionEntry entry : entries.values()) {
            entry.submit(date);
        }
    }
}
