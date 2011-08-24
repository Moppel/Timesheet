/* Copyright (c) 2005 by net-linx; All rights reserved */
package com.uwusoft.timesheet;

import java.util.Date;

/**
 * todo: add class doc
 *
 * @author Uta Wunderlich
 * @version $Revision: $, $Date: Aug 17, 2011
 * @since Aug 17, 2011
 */
public interface SubmissionService {

    void submit(Date date, String task, Double total);
}
