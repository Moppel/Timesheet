package com.uwusoft.timesheet.googlestorage;

import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.*;
import com.google.gdata.util.ServiceException;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.extensionpoint.model.DailySubmitEntry;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * storage service for Google Docs spreadsheet
 *
 * @author Uta Wunderlich
 * @version $Revision: $, $Date: Aug 15, 2011
 * @since Aug 15, 2011
 */
public class GoogleStorageService implements StorageService {

    private static final String dateFormat = "MM/dd/yyyy";
    private static final String timeFormat = "HH:mm";
    private String spreadsheetKey;
    private SpreadsheetService service;
    private FeedURLFactory factory;
    private URL listFeedUrl;
    private List<WorksheetEntry> worksheets;
    private WorksheetEntry defaultWorksheet;
    private Map<String,String> taskLinkMap;
    private Map<String, List<String>> tasks;

    public GoogleStorageService() throws IOException, ServiceException {
        this.spreadsheetKey = "";
        service = new SpreadsheetService("Timesheet");
        service.setProtocolVersion(SpreadsheetService.Versions.V1);
        service.setUserCredentials("user", "password");
        factory = FeedURLFactory.getDefault();
        listFeedUrl = factory.getListFeedUrl(spreadsheetKey, "od6", "private", "full");
        reloadWorksheets();
        loadTasks();
    }

    private void reloadWorksheets() throws IOException, ServiceException {
        WorksheetFeed feed = service.getFeed(factory.getWorksheetFeedUrl(spreadsheetKey, "private", "full"), WorksheetFeed.class);
        worksheets = feed.getEntries();
        defaultWorksheet = worksheets.get(0);
        worksheets.remove(defaultWorksheet); // only task sheets remaining
    }

    public Map<String, List<String>> getTasks() {
        return tasks;
    }

    public void storeTimeEntry(Date dateTime, String task) {
        storeTimeEntry(dateTime, task, null);
    }

    public void storeTimeEntry(Date dateTime, String task, String defaultTotal) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(dateTime);
        try {
			reloadWorksheets();
			ListEntry timeEntry = new ListEntry();
			String taskLink = null;
			timeEntry.getCustomElements().setValueLocal(WEEK,
					Integer.toString(cal.get(Calendar.WEEK_OF_YEAR)));
			timeEntry.getCustomElements().setValueLocal(DATE,
					new SimpleDateFormat(dateFormat).format(dateTime));
			if (defaultTotal == null)
				timeEntry.getCustomElements().setValueLocal(TIME,
						new SimpleDateFormat(timeFormat).format(dateTime));
			if (CHECK_IN.equals(task))
				timeEntry.getCustomElements().setValueLocal(TASK, task);
			else {
				if (defaultTotal != null)
					timeEntry.getCustomElements().setValueLocal(TOTAL,
							defaultTotal);
				taskLink = taskLinkMap.get(task);
			}
			service.insert(listFeedUrl, timeEntry);

			reloadWorksheets();
			if (taskLink != null)
				createUpdateCellEntry(defaultWorksheet,
						defaultWorksheet.getRowCount(), 7, "=" + taskLink); // todo column 7 for task
			if (defaultTotal == null && !task.equals(CHECK_IN))
				createUpdateCellEntry(defaultWorksheet,
						defaultWorksheet.getRowCount(), 3,
						"=(R[0]C[-1]-R[-1]C[-1])*24"); // calculate task total
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void storeLastDailyTotal() {
        try {
            reloadWorksheets();
            ListFeed feed = service.getFeed(listFeedUrl, ListFeed.class);
            List<ListEntry> listEntries = feed.getEntries();
            int rowsOfDay = 0;
            for (int i = defaultWorksheet.getRowCount() - 3; i > 0; i--, rowsOfDay++) {
                CustomElementCollection elements = listEntries.get(i).getCustomElements();
                if (CHECK_IN.equals(elements.getValue(TASK))) break; // begin of day
            }
            createUpdateCellEntry(defaultWorksheet, defaultWorksheet.getRowCount(), 4, "=SUM(R[0]C[-1]:R[-" + rowsOfDay + "]C[-1])"); // todo column DT=4
        } catch (IOException e) {
            e.printStackTrace();  //Todo
        } catch (ServiceException e) {
            e.printStackTrace();  //Todo
        }
    }

    public void storeLastWeekTotal() {
        try {
            reloadWorksheets();
            ListFeed feed = service.getFeed(listFeedUrl, ListFeed.class);
            List<ListEntry> listEntries = feed.getEntries();
            int rowsOfWeek = 0;
            for (int i = defaultWorksheet.getRowCount() - 3; i > 0; i--, rowsOfWeek++) {
                CustomElementCollection elements = listEntries.get(i).getCustomElements();
                if (elements.getValue(WEEKLY_TOTAL) != null) break; // end of last week
            }
            createUpdateCellEntry(defaultWorksheet, defaultWorksheet.getRowCount(), 5, "=SUM(R[0]C[-1]:R[-" + rowsOfWeek + "]C[-1])"); // todo column WT=5
        } catch (IOException e) {
            e.printStackTrace();  //Todo
        } catch (ServiceException e) {
            e.printStackTrace();  //Todo
        }        
    }

    public void submitEntries() {
        try {
			reloadWorksheets();
	        ListFeed feed = service.getFeed(listFeedUrl, ListFeed.class);
	        List<ListEntry> listEntries = feed.getEntries();

	        Date lastDate = null;
	        DailySubmitEntry entry = null;
	        int i = defaultWorksheet.getRowCount() - 2;
	        for (; i > 0; i--) {
	            CustomElementCollection elements = listEntries.get(i).getCustomElements();
	            if ("Submitted".equals(elements.getValue(SUBMIT_STATUS))) break;

	            if (elements.getValue(DATE) == null) continue;
	            lastDate = new SimpleDateFormat(dateFormat).parse(elements.getValue(DATE)); // search for last date and break
	            entry = new DailySubmitEntry(lastDate);
	            break;
	        }
	        for (; i > 0; i--) {
	            CustomElementCollection elements = listEntries.get(i).getCustomElements();
	            if ("Submitted".equals(elements.getValue(SUBMIT_STATUS))) break;

	            if (elements.getValue(DATE) == null) continue;
	            Date date = new SimpleDateFormat(dateFormat).parse(elements.getValue(DATE));
	            if (!date.equals(lastDate)) { // another day
	                entry.submitEntries();
	                entry = new DailySubmitEntry(date);
	                lastDate = date;
	            }

	            String task = elements.getValue(TASK);
	            if (task == null || CHECK_IN.equals(task) || taskLinkMap.get(task) == null) continue;
	            String system = taskLinkMap.get(task).split("!")[0]; // todo get submit service for task
	            //entry.addSubmitEntry(task, Double.valueOf(elements.getValue(TOTAL)), new SubmitServiceImpl());
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        Date lastDate = null;
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
						taskLinkMap.put(value, title + "!$A$" + (i + 2)); // todo hardcoded: task is in the first column
						tasks.get(title).add(value);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

    private void createUpdateCellEntry(WorksheetEntry worksheet, int row, int col, String value) {
        CellEntry entry;
		try {
			entry = service.getEntry(new URL(worksheet.getCellFeedUrl().toString() + "/" + "R" + row + "C" + col), CellEntry.class);
	        entry.changeInputValueLocal(value);
	        entry.update();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
