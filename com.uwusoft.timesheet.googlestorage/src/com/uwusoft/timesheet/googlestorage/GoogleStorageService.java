package com.uwusoft.timesheet.googlestorage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;

import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.CustomElementCollection;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.dialog.LoginDialog;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.extensionpoint.model.DailySubmitEntry;
import com.uwusoft.timesheet.extensionpoint.model.TaskEntry;
import com.uwusoft.timesheet.model.Task;
import com.uwusoft.timesheet.util.ExtensionManager;
import com.uwusoft.timesheet.util.MessageBox;
import com.uwusoft.timesheet.util.SecurePreferencesManager;

/**
 * storage service for Google Docs spreadsheet
 *
 * @author Uta Wunderlich
 * @version $Revision: $, $Date: Aug 15, 2011
 * @since Aug 15, 2011
 */
public class GoogleStorageService implements StorageService {

    public static final String USERNAME="user.name";
    public static final String PASSWORD="user.password";
    public static final String SPREADSHEET_KEY="spreadsheet.key";

    private static final String dateFormat = "MM/dd/yyyy";
    private static final String timeFormat = "HH:mm";
    private static String spreadsheetKey;
    private static SpreadsheetService service;
    private static FeedURLFactory factory;
    private static URL listFeedUrl;
	private static Map<String, Integer> headingIndex;
    private static List<WorksheetEntry> worksheets = new ArrayList<WorksheetEntry>();
    private static WorksheetEntry defaultWorksheet;
    private static Map<String,String> taskLinkMap;
    private static Map<String, List<String>> tasks;
    private static String message;
    private static List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
    private static String title = "Google Storage Service";
    
    static {
        service = new SpreadsheetService("Timesheet");
        service.setProtocolVersion(SpreadsheetService.Versions.V1);
       	while (!authenticate());
		factory = FeedURLFactory.getDefault();
		headingIndex = new LinkedHashMap<String, Integer>();
    }
    
    private static boolean authenticate() {
        try {
			IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
			SecurePreferencesManager secureProps = new SecurePreferencesManager("Google");
	    	String userName = preferenceStore.getString(USERNAME);
	    	String password = secureProps.getProperty(PASSWORD);
	    	if (!StringUtils.isEmpty(userName) && !StringUtils.isEmpty(password)) {
	        	service.setUserCredentials(userName, password);
	            spreadsheetKey = preferenceStore.getString(SPREADSHEET_KEY);
	        	return true;
	    	}
	    	
	    	Display display = Display.getDefault();
	    	LoginDialog loginDialog = new LoginDialog(display, "Google Log in", message, userName, password);
			if (loginDialog.open() == Dialog.OK) {
	        	service.setUserCredentials(loginDialog.getUser(), loginDialog.getPassword());
	        	preferenceStore.setValue(USERNAME, loginDialog.getUser());
	        	if (loginDialog.isStorePassword())
	        		secureProps.storeProperty(PASSWORD, loginDialog.getPassword());
	        	else
	        		secureProps.removeProperty(PASSWORD);
	            spreadsheetKey = preferenceStore.getString(SPREADSHEET_KEY);
	        	return true;
			}
		} catch (AuthenticationException e) {
			message = e.getLocalizedMessage();
			return false;
		}
        Display.getDefault().dispose();
		System.exit(1);
		return false; 
   }

    public GoogleStorageService() {
        if (!reloadWorksheets()) return;
        loadTasks();
    }
    
    private boolean reloadSpreadsheetKey() {
    	if (StringUtils.isEmpty(spreadsheetKey)) {
			IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
            spreadsheetKey = preferenceStore.getString(SPREADSHEET_KEY);
    	}
		try {
	    	if (StringUtils.isEmpty(spreadsheetKey)) {
	    		MessageBox.setMessage("Create spreadsheet", "Please manually create a spreadsheet and copy the spreadsheet key to the Google Spreadsheet Preferences!");
	    		return false;
	    	}
			listFeedUrl = factory.getListFeedUrl(spreadsheetKey, "od6", "private", "full");
			CellFeed cellFeed = service.getFeed(factory.getCellFeedUrl(spreadsheetKey, "od6", "private", "full"), CellFeed.class);
			for (CellEntry entry : cellFeed.getEntries()) {
				if (entry.getCell().getRow() == 1)
					headingIndex.put(entry.getCell().getValue(), entry.getCell().getCol());
				else
					break;
			}
		} catch (MalformedURLException e) {
			MessageBox.setError(title, e.getLocalizedMessage());
		} catch (IOException e) {
			MessageBox.setError(title, e.getLocalizedMessage());
		} catch (ServiceException e) {
			MessageBox.setError(title, e.getLocalizedMessage());
		}
    	return true;
    }

    private boolean reloadWorksheets() {
    	if (!reloadSpreadsheetKey()) return false;
        WorksheetFeed feed;
		try {
			feed = service.getFeed(factory.getWorksheetFeedUrl(spreadsheetKey, "private", "full"), WorksheetFeed.class);
	        worksheets = feed.getEntries();
	        defaultWorksheet = worksheets.get(0);
	        worksheets.remove(defaultWorksheet); // only task sheets remaining
		} catch (MalformedURLException e) {
			MessageBox.setError(title, e.getLocalizedMessage());
		} catch (IOException e) {
			MessageBox.setError(title, e.getLocalizedMessage());
		} catch (ServiceException e) {
			MessageBox.setError(title, e.getLocalizedMessage());
		}
		return true;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) 
    { 
    	listeners.add(listener); 
    } 
   
    public void removePropertyChangeListener(PropertyChangeListener listener) 
    { 
    	listeners.remove(listener); 
    } 

    public Map<String, List<String>> getTasks() {
        return tasks;
    }

    public List<TaskEntry> getTaskEntries(Date date) {
    	List <TaskEntry> taskEntries = new ArrayList<TaskEntry>();
		try {
	        if (!reloadWorksheets()) return taskEntries;
	        ListFeed feed = service.getFeed(listFeedUrl, ListFeed.class);
	        List<ListEntry> listEntries = feed.getEntries();
	        for (int i = defaultWorksheet.getRowCount() - 2; i > 0; i--) {
	            CustomElementCollection elements = listEntries.get(i).getCustomElements();
	            if (elements.getValue(DATE) == null) continue;
	            if (!new SimpleDateFormat(dateFormat).format(new SimpleDateFormat(dateFormat).parse(elements.getValue(DATE)))
	            		.equals(new SimpleDateFormat(dateFormat).format(date))) break;
	            
	            String task = elements.getValue(TASK);
	            if (task == null || elements.getValue(TIME) == null) break;
	            taskEntries.add(0, new TaskEntry(new SimpleDateFormat(timeFormat).format(
	            		new SimpleDateFormat(timeFormat).parse(elements.getValue(TIME))), task, new Long(i + 2)));
	            if (CHECK_IN.equals(task)) break;
	        }
		} catch (IOException e) {
			MessageBox.setError(title, e.getLocalizedMessage());
		} catch (ServiceException e) {
			MessageBox.setError(title, e.getLocalizedMessage());
		} catch (ParseException e) {
			MessageBox.setError(title, e.getLocalizedMessage());
		}
		return taskEntries;
    }
    
    public void createTaskEntry(Task task) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(task.getDateTime());
        try {
            if (!reloadWorksheets()) return;
			ListEntry timeEntry = new ListEntry();
			String taskLink = null;
			timeEntry.getCustomElements().setValueLocal(WEEK, Integer.toString(cal.get(Calendar.WEEK_OF_YEAR) - 1));
			
			timeEntry.getCustomElements().setValueLocal(DATE,
					new SimpleDateFormat(dateFormat).format(task.getDateTime()));
			
			if (task.getTotal() == 0)
				timeEntry.getCustomElements().setValueLocal(TIME, new SimpleDateFormat(timeFormat).format(task.getDateTime()));
			
			if (CHECK_IN.equals(task.getTask()) || BREAK.equals(task.getTask()))
				timeEntry.getCustomElements().setValueLocal(TASK, task.getTask());
			else {
				if (task.getTotal() != 0) {
					timeEntry.getCustomElements().setValueLocal(TOTAL, Float.toString(task.getTotal()));
					if (task.isWholeDay())
						timeEntry.getCustomElements().setValueLocal(DAILY_TOTAL, Float.toString(task.getTotal()));
				}
				taskLink = taskLinkMap.get(task.getTask());
			}
			service.insert(listFeedUrl, timeEntry);

            if (!reloadWorksheets()) return;
			if (taskLink != null) {
				createUpdateCellEntry(defaultWorksheet,	defaultWorksheet.getRowCount(), headingIndex.get(TASK), "=" + taskLink);
				// if no total set: the (temporary) total of the task will be calculated by: end time - end time of the previous task
				if (task.getTotal() == 0)
					createUpdateCellEntry(defaultWorksheet,	defaultWorksheet.getRowCount(), headingIndex.get(TOTAL), "=(R[0]C[-1]-R[-1]C[-1])*24"); // calculate task total
			}
			for (PropertyChangeListener listener : listeners)
				listener.propertyChange(new PropertyChangeEvent(this, "tasks", null, null));
		} catch (IOException e) {
			MessageBox.setError(title, e.getLocalizedMessage());
		} catch (ServiceException e) {
			MessageBox.setError(title, e.getLocalizedMessage());
		}
    }

    public void storeLastDailyTotal() {
        try {
            if (!reloadWorksheets()) return;
            ListFeed feed = service.getFeed(listFeedUrl, ListFeed.class);
            List<ListEntry> listEntries = feed.getEntries();			
            int rowsOfDay = 0;
            for (int i = defaultWorksheet.getRowCount() - 3; i > 0; i--, rowsOfDay++) {
                CustomElementCollection elements = listEntries.get(i).getCustomElements();
                if (CHECK_IN.equals(elements.getValue(TASK))) break; // begin of day
            }
            createUpdateCellEntry(defaultWorksheet, defaultWorksheet.getRowCount(), headingIndex.get(DAILY_TOTAL), "=SUM(R[0]C[-1]:R[-" + rowsOfDay + "]C[-1])");
        } catch (IOException e) {
			MessageBox.setError(title, e.getLocalizedMessage());
        } catch (ServiceException e) {
			MessageBox.setError(title, e.getLocalizedMessage());
        }
    }

    public void storeLastWeekTotal(String weeklyWorkingHours) {
        try {
            if (!reloadWorksheets()) return;
            ListFeed feed = service.getFeed(listFeedUrl, ListFeed.class);
            List<ListEntry> listEntries = feed.getEntries();
            int rowsOfWeek = 0;
            for (int i = defaultWorksheet.getRowCount() - 3; i > 0; i--, rowsOfWeek++) {
                CustomElementCollection elements = listEntries.get(i).getCustomElements();
                if (elements.getValue(WEEKLY_TOTAL) != null) break; // end of last week
                if (i==1) break;
            }            
            ListEntry timeEntry = new ListEntry();
			timeEntry.getCustomElements().setValueLocal(WEEKLY_TOTAL, "0");
			service.insert(listFeedUrl, timeEntry);
            if (!reloadWorksheets()) return;
			
			createUpdateCellEntry(defaultWorksheet, defaultWorksheet.getRowCount(), headingIndex.get(WEEKLY_TOTAL), "=SUM(R[-1]C[-1]:R[-" + ++rowsOfWeek + "]C[-1])");
			createUpdateCellEntry(defaultWorksheet, defaultWorksheet.getRowCount(), headingIndex.get(OVERTIME), "=R[0]C[-3]-" +weeklyWorkingHours+ "+" +"R[-" + ++rowsOfWeek + "]C[0]");
        } catch (IOException e) {
			MessageBox.setError(title, e.getLocalizedMessage());
        } catch (ServiceException e) {
			MessageBox.setError(title, e.getLocalizedMessage());
        }        
    }

	public void updateTaskEntry(Date time, Long id) {
		createUpdateCellEntry(defaultWorksheet, id.intValue(), headingIndex.get(TIME), new SimpleDateFormat(timeFormat).format(time));
	}

	public void updateTaskEntry(String task, Long id) {
		createUpdateCellEntry(defaultWorksheet, id.intValue(), headingIndex.get(TASK), task);
	}

	public void submitEntries() {
        try {
            if (!reloadWorksheets()) return;
	        ListFeed feed = service.getFeed(listFeedUrl, ListFeed.class);
	        List<ListEntry> listEntries = feed.getEntries();

	        Date lastDate = null;
	        DailySubmitEntry entry = null;
	        int i = defaultWorksheet.getRowCount() - 2;
	        for (; i > 0; i--) {
	            CustomElementCollection elements = listEntries.get(i).getCustomElements();
	            if ("Submitted".equals(elements.getValue(SUBMIT_STATUS))) break;

				if (elements.getValue(TIME) == null || elements.getValue(DAILY_TOTAL) != null) { // search for last complete day and break
					lastDate = new SimpleDateFormat(dateFormat).parse(elements.getValue(DATE));
					entry = new DailySubmitEntry(lastDate);
					break;
	            }
	        }
	        for (; i > 0; i--) {
	            CustomElementCollection elements = listEntries.get(i).getCustomElements();
	            if ("Submitted".equals(elements.getValue(SUBMIT_STATUS))) {
	            	entry.submitEntries();
	            	break;
	            }

	            if (elements.getValue(DATE) == null) continue;
	            Date date = new SimpleDateFormat(dateFormat).parse(elements.getValue(DATE));
	            if (!date.equals(lastDate)) { // another day
	                entry.submitEntries();
	                entry = new DailySubmitEntry(date);
	                lastDate = date;
	            }

	            String task = elements.getValue(TASK);
	            if (task == null || CHECK_IN.equals(task) || taskLinkMap.get(task) == null) continue;
	            String system = taskLinkMap.get(task).split("!")[0];	             
	            entry.addSubmitEntry(task, Double.valueOf(elements.getValue(TOTAL)),
	            		new ExtensionManager<SubmissionService>(SubmissionService.SERVICE_ID).getService(system));
	        }
		} catch (IOException e) {
			MessageBox.setError(title, e.getLocalizedMessage());
		} catch (ServiceException e) {
			MessageBox.setError(title, e.getLocalizedMessage());
		} catch (ParseException e) {
			MessageBox.setError(title, e.getLocalizedMessage());
		}
    }

    private void loadTasks() {
        tasks = new HashMap<String, List<String>> ();
        taskLinkMap = new HashMap<String, String>();
        for (WorksheetEntry worksheet : worksheets) {
            String title = worksheet.getTitle().getPlainText();
            tasks.put(title, new ArrayList<String>());
            ListFeed feed;
			try {
				feed = service.getFeed(worksheet.getListFeedUrl(), ListFeed.class);
				List<ListEntry> entries = feed.getEntries();
				for (int i = 0; i < entries.size(); i++) {
					ListEntry entry = entries.get(i);
					String value = entry.getCustomElements().getValue(TASK);
					if (value != null) {
						taskLinkMap.put(value, title + "!$A$" + (i + 2)); // TODO hardcoded: task must be in the first column
						tasks.get(title).add(value);
					}
				}
			} catch (IOException e) {
				MessageBox.setError(title, e.getLocalizedMessage());
			} catch (ServiceException e) {
				MessageBox.setError(title, e.getLocalizedMessage());
			}
        }
		for (PropertyChangeListener listener : listeners)
			listener.propertyChange(new PropertyChangeEvent(this, "tasks", null, null));
    }

    private void createUpdateCellEntry(WorksheetEntry worksheet, int row, int col, String value) {
		try {
			CellEntry entry = service.getEntry(new URL(worksheet.getCellFeedUrl().toString() + "/" + "R" + row + "C" + col), CellEntry.class);
	        entry.changeInputValueLocal(value);
	        entry.update();
		} catch (MalformedURLException e) {
			MessageBox.setError(title, e.getLocalizedMessage());
		} catch (IOException e) {
			MessageBox.setError(title, e.getLocalizedMessage());
		} catch (ServiceException e) {
			MessageBox.setError(title, e.getLocalizedMessage());
		}
    }
}
