/* Copyright (c) 2005 by net-linx; All rights reserved */
package com.uwusoft.timesheet.extensionpoint.model;

import java.util.Date;

import com.uwusoft.timesheet.extensionpoint.SubmitService;

/**
 * contains all information about a task to be submitted to a special time management system
 *
 * @author Uta Wunderlich
 * @version $Revision: $, $Date: Aug 17, 2011
 * @since Aug 17, 2011
 */
public class TaskSubmitEntry {
    private String name;
    private double total;
    private SubmitService service;

    public TaskSubmitEntry(String name, double total, SubmitService service) {
        this.name = name;
        this.total = total;
        this.service = service;
    }

    public void addTotal(double total) {
        this.total = this.total + total;
    }

    public void submit(Date date) {
        service.submit(date, name, total);
    }
}
