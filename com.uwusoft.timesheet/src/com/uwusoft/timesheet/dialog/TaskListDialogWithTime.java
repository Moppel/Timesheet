package com.uwusoft.timesheet.dialog;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.services.ISourceProviderService;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.commands.CheckinHandler;
import com.uwusoft.timesheet.commands.SessionSourceProvider;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.model.Task;

public class TaskListDialogWithTime extends TaskListDialogWithComment {
	private Date changeDate;
    private int day, month, year, hours, minutes;
    private String changeTitle;

    public TaskListDialogWithTime(Display display, Task taskSelected, Date changeDate, String changeTitle) {
    	super(new Shell(display, SWT.NO_TRIM | SWT.ON_TOP), taskSelected, null);
    	this.changeDate = changeDate;
    	this.changeTitle = changeTitle;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(changeDate);
		day = calendar.get(Calendar.DAY_OF_MONTH);
		month = calendar.get(Calendar.MONTH);
		year = calendar.get(Calendar.YEAR);
		hours = calendar.get(Calendar.HOUR_OF_DAY);
		minutes = calendar.get(Calendar.MINUTE);		
    }

	@Override
	protected void setTimePanel(Composite parent) {
        if (changeDate != null) {
    		final Composite timePanel = new Composite(parent, SWT.NONE);
    		timePanel.moveAbove(getContents());
    		timePanel.setLayout(new GridLayout(4, false));
    		timePanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    		(new Label(timePanel, SWT.NULL)).setText(changeTitle + " at : ");
    		(new Label(timePanel, SWT.NULL)).setText(DateFormat.getDateInstance(DateFormat.SHORT).format(changeDate));
    		
    		DateTime timeEntry = new DateTime(timePanel, SWT.TIME | SWT.SHORT);
    		timeEntry.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
    		timeEntry.setHours(hours);
    		timeEntry.setMinutes(minutes);
    		timeEntry.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
    		timeEntry.addSelectionListener(new SelectionListener() {			
    			public void widgetSelected(SelectionEvent e) {
    				hours = ((DateTime) e.getSource()).getHours();
    				minutes = ((DateTime) e.getSource()).getMinutes();
    			}
    			public void widgetDefaultSelected(SelectionEvent e) {
    			}
    		});
    		timeEntry.setFocus();
    		Button breakButton = new Button(timePanel, SWT.PUSH);
    		breakButton.setImage(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "/icons/pause_16.png").createImage());
    		breakButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
    		breakButton.setText("Set Break");
    		ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI.getWorkbench().getService(ISourceProviderService.class);
    		SessionSourceProvider commandStateService = (SessionSourceProvider) sourceProviderService.getSourceProvider(SessionSourceProvider.SESSION_STATE);
    		breakButton.setVisible(!CheckinHandler.title.equals(changeTitle)
    				&& commandStateService.getCurrentState().get(SessionSourceProvider.BREAK_STATE) == SessionSourceProvider.DISABLED);
    		breakButton.addSelectionListener(new SelectionAdapter() {
    		    public void widgetSelected(SelectionEvent e) {
    		    	selectedTask = StorageService.BREAK;
    		    	projectSelected = null;
    		    	systemSelected = null;
    		    	okPressed();
    		    }
    		});        	
        }
	}

	public Date getTime() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.HOUR_OF_DAY, hours);
		calendar.set(Calendar.MINUTE, minutes);
		return calendar.getTime();
    }
}
