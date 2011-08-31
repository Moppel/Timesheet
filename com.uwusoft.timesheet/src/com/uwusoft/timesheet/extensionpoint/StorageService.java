package com.uwusoft.timesheet.extensionpoint;

import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.Date;


/**
 * todo: add class doc
 *
 * @author Uta Wunderlich
 * @version $Revision: $, $Date: Aug 15, 2011
 * @since Aug 15, 2011
 */
public interface StorageService {
    public static final String DATE = "Date";
    public static final String TIME = "Time";
    public static final String TOTAL = "Total";
    public static final String DAILY_TOTAL = "DT";
    public static final String WEEKLY_TOTAL = "WT";
    public static final String WEEK = "Week";
    public static final String TASK = "Task";
    public static final String CHECK_IN = "Check in";
    public static final String SUBMIT_STATUS = "SubmitStatus";

    /**
     * @return a list of tasks assigned to name of special time management system
     */
    Map<String, List<String>> getTasks();

    /**
     * store task
     * the (temporary) total of the task will be calculated by: end time - end time of the previous task
     * @param dateTime end time of the task
     * @param task task name
     * @throws IOException
     * @throws ServiceException
     */
    void storeTimeEntry(Date dateTime, String task);

    /**
     * store default daily task with default total
     * @param dateTime only the date of the default task
     * @param task name of the default task
     * @param defaultTotal total of the default task that shouldn't be calculated automatically
     * @throws IOException
     * @throws ServiceException
     */
    void storeTimeEntry(Date dateTime, String task, String defaultTotal);

    void storeLastDailyTotal();

    void storeLastWeekTotal();
    
    void submitEntries();
}
