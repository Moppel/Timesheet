package com.uwusoft.timesheet.extensionpoint.model;

import java.util.Date;

import com.uwusoft.timesheet.extensionpoint.SubmissionService;

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
    private SubmissionService service;

    public TaskSubmissionEntry(SubmissionTask task, double total, SubmissionService service) {
        this.task = task;
        this.total = total;
        this.service = service;
    }

    public void addTotal(double total) {
        this.total = this.total + total;
    }

    public void submit(Date date) {
        service.submit(date, task, total);
    }
}
