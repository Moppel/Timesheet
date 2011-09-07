/* Copyright (c) 2005 by net-linx; All rights reserved */
package com.uwusoft.timesheet.extensionpoint;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * todo: add class doc
 *
 * @author Uta Wunderlich
 * @version $Revision: $, $Date: Aug 18, 2011
 * @since Aug 18, 2011
 */
public class SubmissionServiceImpl implements SubmissionService {

    public SubmissionServiceImpl() {
    }

    public void submit(Date date, String task, Double total) {
        //Todo implement
        System.out.println(new SimpleDateFormat("dd/MM/yyyy").format(date) + "\t" + task + "\t" + total);
    }
}
