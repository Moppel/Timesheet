package com.uwusoft.timesheet.extensionpoint;

import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.uwusoft.timesheet.extensionpoint.model.SubmissionEntry;
import com.uwusoft.timesheet.model.TaskEntry;

/**
 * todo: add class doc
 *
 * @author Uta Wunderlich
 * @version $Revision: $, $Date: Aug 15, 2011
 * @since Aug 15, 2011
 */
public interface StorageService {
	public static final String SERVICE_ID = "com.uwusoft.timesheet.storageservice";
	public static final String SERVICE_NAME = "storage";
	public static final String PROPERTY = "storage.system";
	public static final String OPEN_BROWSER_CHECKIN = "openbrowser.checkin";
	public static final String OPEN_BROWSER_CHANGE_TASK = "openbrowser.changetask";
	public static final String USERNAME = "user.name";
	public static final String PASSWORD = "user.password";

	public static final String DATE = "Date";
    public static final String TIME = "Time";
    public static final String TOTAL = "Total";
    public static final String COMMENT = "Comment";
    public static final String DAILY_TOTAL = "DT";
    public static final String WEEKLY_TOTAL = "WT";
    public static final String WEEK = "Week";
    public static final String TASK = "Task";
    public static final String PROJECT = "Project";
    public static final String ID = "ID";
    public static final String OVERTIME = "Overtime";
    public static final String SUBMISSION_STATUS = "SubmissionStatus";
    public static final String CHECK_IN = "Check in";
    public static final String BREAK = "Break";
	public static SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");
	
	public static final String PROPERTY_WEEK = "week";

    List<String> getProjects(String system);
    
    /**
     * full reload
     */
    void reload();
    
    /**
     * @return a list of tasks assigned to name of special time management system
     */
    List<String> findTasksBySystemAndProject(String system, String project);

    void addPropertyChangeListener(PropertyChangeListener listener);
    
    void removePropertyChangeListener(PropertyChangeListener listener);
    
    String[] getUsedCommentsForTask(String task, String project, String system);
    
    /**
     * @param startDate TODO
     * @param endDate TODO
     * @return a list of {@link TaskEntry}'s for the week
     */
    List<TaskEntry> getTaskEntries(Date startDate, Date endDate);
    
    /**
     * store task
     * the (temporary) total of the task will be calculated by: end time - end time of the previous task
     * @param task
     */
    void createTaskEntry(TaskEntry task);
   
    /**
     * update date/time for task
     * @param id the id of the task entry
     * @param time the time of the task entry
     * @param wholeDate true: set Date, Time and Week column, false: set only Time column
     */
    void updateTaskEntry(Long id, Date time, boolean wholeDate);
    
    /**
     * update task, project, system and comment
     * @param id the id of the task entry
     * @param task task name
     * @param project project name
     * @param system system name
     * @param comment comment
     */
    void updateTaskEntry(Long id, String task, String project, String system, String comment);

    /**
     * only to be implemented for sequential storage system
     */
    void storeLastDailyTotal();

    /**
     * only to be implemented for sequential storage system
     */
    void storeLastWeekTotal(String weeklyWorkingHours);
    
    TaskEntry getLastTask();
    
    Date getLastTaskEntryDate();
    
    void importTasks(String submissionSystem, Map<String, Set<SubmissionEntry>> projects);
    
    Set<String> submitEntries(int weekNum);
    
    void submitFillTask(Date date);
    
    void openUrl(String openBrowser);
}
