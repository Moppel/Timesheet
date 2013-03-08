package com.uwusoft.timesheet.dialog;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.TimesheetApp;
import com.uwusoft.timesheet.extensionpoint.AllDayTaskService;
import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
import com.uwusoft.timesheet.model.Task;

public class AllDayTaskListDialog extends InternalAllDayTaskListDialog {
	private static String allDayTaskSystem = TimesheetApp.getDescriptiveName(Activator.getDefault().getPreferenceStore().getString(AllDayTaskService.PROPERTY),
			AllDayTaskService.SERVICE_NAME);
	private AllDayTaskDateDialog allDayTaskDateDialog;

	public AllDayTaskListDialog(Display display, Task taskSelected, Date date) {
		super(new Shell(display, SWT.NO_TRIM | SWT.ON_TOP), taskSelected);
		allDayTaskDateDialog = new AllDayTaskDateDialog(Display.getDefault(), allDayTaskSystem, date) {
			@Override
			protected void createTaskPart(Composite composite) {
			}

			@Override
			protected int getColumns() {
				return 4;
			}
		};
		Set<String> allDayTasks = ExternalAllDayTaskListDialog.getInternalTasks(false, taskSelected);
		systemMap.put(allDayTaskSystem, Collections.singleton(LocalStorageService.getAllDayTaskService().getProjectName()));
		projectMap.put(LocalStorageService.getAllDayTaskService().getProjectName(), allDayTasks);
		setTitle("All Day Tasks");
	}

	public AllDayTaskListDialog(Display display, Task taskSelected, String title, Date date) {
		this(display, taskSelected, date);
		setTitle(title);
	}
	
	@Override
	protected Control createDialogArea(Composite composite) {
		Control parent = allDayTaskDateDialog.createDialogArea((Composite) super.createDialogArea(composite));
		allDayTaskDateDialog.getStatus().addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (getButton(IDialogConstants.OK_ID) != null)
					getButton(IDialogConstants.OK_ID).setEnabled(Status.OK_STATUS.getMessage().equals(allDayTaskDateDialog.getStatus().getText()));
			}
		});
		return parent;
	}

	@Override
	protected void setSystems() {
		super.setSystems();
		List<String> list = new LinkedList<String>(Arrays.asList(systems));
		list.add(allDayTaskSystem);
		systems = list.toArray(new String[list.size()]);
	}

	@Override
	protected String getSystemText() {
		return "";
	}

	public Date getFrom() {
		return allDayTaskDateDialog.getFrom();
	}

	public Date getTo() {
		return allDayTaskDateDialog.getTo();
	}
}
