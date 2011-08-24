/* Copyright (c) 2005 by net-linx; All rights reserved */
package com.uwusoft.timesheet;

import java.util.*;

/**
 * container for all {@link com.uwusoft.timesheet.TaskSubmitEntry}'s for one day 
 *
 * @author Uta Wunderlich
 * @version $Revision: $, $Date: Aug 17, 2011
 * @since Aug 17, 2011
 */
public class DailySubmitEntry {
    private Date date;
    private Map<String, TaskSubmitEntry> entries;

    public DailySubmitEntry(Date date) {
        this.date = date;
        entries = new HashMap<String, TaskSubmitEntry>();
    }

    public void addSubmitEntry(String task, double total, SubmissionService service) {
        TaskSubmitEntry available = entries.get(task);
        if (available == null) entries.put(task, new TaskSubmitEntry(task, total, service));
        else available.addTotal(total);
    }

    public void submitEntries() {
        for (TaskSubmitEntry entry : entries.values()) {
            entry.submit(date);
        }
    }
}
