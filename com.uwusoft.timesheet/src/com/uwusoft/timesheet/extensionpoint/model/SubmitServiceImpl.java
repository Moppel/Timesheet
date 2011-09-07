/* Copyright (c) 2005 by net-linx; All rights reserved */
package com.uwusoft.timesheet.extensionpoint.model;

import java.util.Date;
import java.text.SimpleDateFormat;

import com.uwusoft.timesheet.extensionpoint.SubmissionService;

/**
 * todo: add class doc
 *
 * @author Uta Wunderlich
 * @version $Revision: $, $Date: Aug 18, 2011
 * @since Aug 18, 2011
 */
public class SubmitServiceImpl implements SubmissionService {

    public void submit(Date date, String task, Double total) {
        //Todo implement
        System.out.println(new SimpleDateFormat().format(date) + "\t" + task + "\t" + total);
    }
}
