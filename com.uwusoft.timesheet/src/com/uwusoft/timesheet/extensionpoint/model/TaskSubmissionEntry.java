package com.uwusoft.timesheet.extensionpoint.model;

import java.util.Date;
import java.util.Map;

import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.util.ExtensionManager;

/**
 * contains all information about a task to be submitted to a special time management system
 *
 * @author Uta Wunderlich
 * @version $Revision: $, $Date: Aug 17, 2011
 * @since Aug 17, 2011
 */
public class TaskSubmissionEntry {
    private SubmissionTask task;
    private double total;
    private static Map<String, String> submissionSystems = TimesheetApp.getSubmissionSystems();

    public TaskSubmissionEntry(SubmissionTask task, double total) {
        this.task = task;
        this.total = total;
    }

    public void addTotal(double total) {
        this.total = this.total + total;
    }

    public void submit(Date date) {
    	new ExtensionManager<SubmissionService>(SubmissionService.SERVICE_ID).getService(submissionSystems.get(task.getSystem()))
    		.submit(date, task, total);
    }
}
