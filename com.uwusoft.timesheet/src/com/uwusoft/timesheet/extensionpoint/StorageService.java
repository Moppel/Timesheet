package com.uwusoft.timesheet.extensionpoint;

import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;

import com.uwusoft.timesheet.model.TaskEntry;
import com.uwusoft.timesheet.submission.model.SubmissionProject;

/**
 * todo: add class doc
 *
 * @author Uta Wunderlich
 * @version $Revision: $, $Date: Aug 15, 2011
 * @since Aug 15, 2011
 */
public interface StorageService extends ImportTaskService {
	public static final String SERVICE_ID = "com.uwusoft.timesheet.storageservice";
	public static final String SERVICE_NAME = "storage";
	public static final String PROPERTY = "storage.system";
	public static final String OPEN_BROWSER_CHECKIN = "openbrowser.checkin";
	public static final String OPEN_BROWSER_CHANGE_TASK = "openbrowser.changetask";
	public static final String USERNAME = "user.name";
	public static final String PASSWORD = "user.password";
    public static final String TIMESHEET_PREFIX = "Timesheet";

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
	public static SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");

    List<String> getProjects(String system);
    
    Collection<SubmissionProject> getImportedProjects(String system);
    
    /**
     * full reload
     */
    void reload();
    
    void addPropertyChangeListener(PropertyChangeListener listener);
    
    void removePropertyChangeListener(PropertyChangeListener listener);
    
    String[] getUsedCommentsForTask(String task, String project, String system);
    
    /**
     * @param startDate
     * @param endDate
     * @return a list of {@link TaskEntry}'s for the week
     */
    List<TaskEntry> getTaskEntries(Date startDate, Date endDate);
    
    /**
     * store task
     * the (temporary) total of the task will be calculated by: end time - end time of the previous task
     * @param task
     * @return row number of the created entry
     * @throws CoreException TODO
     */
    Long createTaskEntry(TaskEntry entry) throws CoreException;
   
    /**
     * update task entry
     * @param entry the task entry
     * @throws CoreException TODO
     */
    void updateTaskEntry(TaskEntry entry) throws CoreException;

    /**
     * only to be implemented for sequential storage system
     */
    void handleDayChange();

    /**
     * only to be implemented for sequential storage system
     */
    void handleWeekChange();
    
    void handleYearChange(int lastWeek);
    
    TaskEntry getLastTask();
    
    Date getLastTaskEntryDate();
    
    Set<String> submitEntries(Date startDate, Date endDate);
    
    void submitFillTask(Date date);
    
    void openUrl(String openBrowser);
}
