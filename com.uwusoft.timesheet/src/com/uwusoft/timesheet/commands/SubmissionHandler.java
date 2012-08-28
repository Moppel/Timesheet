package com.uwusoft.timesheet.commands;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;

import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.dialog.SubmissionDialog;
import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
import com.uwusoft.timesheet.extensionpoint.SubmissionService;
import com.uwusoft.timesheet.util.ExtensionManager;
import com.uwusoft.timesheet.util.MessageBox;

public class SubmissionHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		LocalStorageService storageService = LocalStorageService.getInstance();
        
		Calendar cal = new GregorianCalendar();
    	cal.setTime(new Date());
		int weekNum = cal.get(Calendar.WEEK_OF_YEAR) - 1;
		
		String parameter = event.getParameter("Timesheet.commands.weekNum");
		if (parameter != null) weekNum = Integer.parseInt(parameter);
    	
		Date lastTaskEntryDate = storageService.getLastTaskEntryDate();
		if (lastTaskEntryDate != null) cal.setTime(lastTaskEntryDate);
		SubmissionDialog submissionDialog = new SubmissionDialog(Display.getDefault(), weekNum, cal.get(Calendar.WEEK_OF_YEAR));
		if (submissionDialog.open() == Dialog.OK) {
			Set<String> systems = storageService.submitEntries(submissionDialog.getStartDate(), submissionDialog.getEndDate());
			storageService.synchronize(null);
			if (systems.isEmpty())
				MessageBox.setMessage("Submission", "No more entries to submit for " + submissionDialog.getWeekNum());			
			else {
				MessageBox.setMessage("Submission", "Submission of week " + submissionDialog.getWeekNum() + " successful!");
				Map<String, String> submissionSystems = TimesheetApp.getSubmissionSystems();
				for (String system : systems) {
					new ExtensionManager<SubmissionService>(SubmissionService.SERVICE_ID).getService(submissionSystems.get(system)).openUrl();
				}
			}
		}                            							
		return null;
	}
}
