package com.uwusoft.timesheet;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.uwusoft.timesheet.dialog.TimeDialog;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.util.ExtensionManager;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	private IWorkbenchWindow window;
	private TrayItem trayItem;
	private Image trayImage;
	private final static String COMMAND_ID = "org.eclipse.ui.file.exit";
	private IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();

	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setInitialSize(new Point(400, 300));
		configurer.setShowCoolBar(false);
		configurer.setShowStatusLine(false);
		configurer.setTitle("Timesheet");
	}
	
	public void postWindowOpen() {
		super.postWindowOpen();
		window = getWindowConfigurer().getWindow();
		window.getShell().addShellListener(new ShellAdapter() {
			public void shellIconified(ShellEvent e) { // If the window is minimized hide the window
				preWindowShellClose();
			}
		});
		trayItem = initTaskItem();
		// Some OS might not support tray items
		if (trayItem != null) {
			hookPopupMenu();
		}
	}

	public boolean preWindowShellClose() {
		window.getShell().setVisible(false);
		return false; // if window close button pressed only minimize to system tray (don't exit the program)
	}

	private void hookPopupMenu() {
		trayItem.addListener(SWT.MenuDetect, new Listener() {
			public void handleEvent(Event event) {
				Menu menu = new Menu(window.getShell(), SWT.POP_UP);

				MenuItem changeTasks = new MenuItem(menu, SWT.NONE);
				changeTasks.setText("Change Task");
				changeTasks.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						StorageService storageService = new ExtensionManager<StorageService>(
								StorageService.SERVICE_ID).getService(preferenceStore.getString(StorageService.PROPERTY));
						ListDialog listDialog = new ListDialog(window.getShell());
						listDialog.setTitle("Tasks");
						listDialog.setMessage("Select next task");
						listDialog.setContentProvider(ArrayContentProvider.getInstance());
						listDialog.setLabelProvider(new LabelProvider());
						listDialog.setWidthInChars(70);
						List<String> tasks = storageService.getTasks().get("Primavera"); // TODO
						tasks.remove(preferenceStore.getString(TimesheetApp.LAST_TASK));
						listDialog.setInput(tasks);
						if (listDialog.open() == Dialog.OK) {
						    String selectedTask = Arrays.toString(listDialog.getResult());
						    selectedTask = selectedTask.substring(selectedTask.indexOf("[") + 1, selectedTask.indexOf("]"));
							if (StringUtils.isEmpty(selectedTask)) return;
							TimeDialog timeDialog = new TimeDialog(getWindowConfigurer().getWindow().getShell().getDisplay(), selectedTask, new Date());
							if (timeDialog.open() == Dialog.OK) {
				                storageService.createTaskEntry(timeDialog.getTime(), preferenceStore.getString(TimesheetApp.LAST_TASK));
								preferenceStore.setValue(TimesheetApp.LAST_TASK, selectedTask);
							}
						}						
					}
				});
				MenuItem checkout = new MenuItem(menu, SWT.NONE);
				checkout.setText("Check out");
				checkout.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						TimeDialog timeDialog = new TimeDialog(window.getShell().getDisplay(), "Check out",
								preferenceStore.getString(TimesheetApp.LAST_TASK), new Date());
						if (timeDialog.open() == Dialog.OK) {
							StorageService storageService = new ExtensionManager<StorageService>(
									StorageService.SERVICE_ID).getService(preferenceStore.getString(StorageService.PROPERTY));
							storageService.createTaskEntry(timeDialog.getTime(), preferenceStore.getString(TimesheetApp.LAST_TASK));
							storageService.storeLastDailyTotal();
							if (preferenceStore.getString(TimesheetApp.DAILY_TASK) != null)
								storageService.createTaskEntry(timeDialog.getTime(), preferenceStore.getString(TimesheetApp.DAILY_TASK),
										preferenceStore.getString(TimesheetApp.DAILY_TASK_TOTAL));
							preferenceStore.setValue(TimesheetApp.LAST_TASK, StringUtils.EMPTY);
			            	preferenceStore.setValue(TimesheetApp.SYSTEM_SHUTDOWN, TimesheetApp.formatter.format(timeDialog.getTime()));
						}
					}
				});

				MenuItem wholeDayTask = new MenuItem(menu, SWT.CASCADE);
				wholeDayTask.setText("Set whole day task");
				Menu subMenu = new Menu(wholeDayTask);
				wholeDayTask.setMenu(subMenu);
				
				MenuItem holiday = new MenuItem(subMenu, SWT.NONE);
				holiday.setText("Holiday");
				holiday.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						
					}
				});

				MenuItem submit = new MenuItem(menu, SWT.NONE);
				submit.setText("Submit");
				submit.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						new ExtensionManager<StorageService>(
								StorageService.SERVICE_ID).getService(preferenceStore
										.getString(StorageService.PROPERTY)).submitEntries();
					}
				});
				MenuItem prefs = new MenuItem(menu, SWT.NONE);
				prefs.setText("Settings");
				prefs.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						ActionFactory.PREFERENCES.create(window).run();						
					}
				});
				
				// Creates a new menu item that terminates the program
				// when selected
				MenuItem exit = new MenuItem(menu, SWT.NONE);
				exit.setText("Exit");
				exit.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						preferenceStore.setValue(TimesheetApp.SYSTEM_SHUTDOWN, TimesheetApp.formatter.format(System.currentTimeMillis()));
						IHandlerService handlerService = (IHandlerService) window.getService(IHandlerService.class);
						try {
							handlerService.executeCommand(COMMAND_ID, null);
						} catch (Exception ex) {
							throw new RuntimeException(COMMAND_ID);
						}
					}
				});
				menu.setVisible(true);
			}
		});
	}

	private TrayItem initTaskItem() {
		final Tray tray = window.getShell().getDisplay().getSystemTray();
		final TrayItem trayItem = new TrayItem(tray, SWT.NONE);
		ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/clock.png");
		if (descriptor != null) {
			trayImage = descriptor.createImage();
			trayItem.setImage(trayImage);
		}
		trayItem.setToolTipText("Timesheet");
		trayItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				Shell shell = window.getShell();
				shell.setVisible(true);
				shell.setActive();
				shell.setFocus();
				shell.setMinimized(false);
				//trayItem.dispose();
			}
		});
		return trayItem;
	}

	// We need to clean-up after ourself
	@Override
	public void dispose() {
		if (trayImage != null) {
			trayImage.dispose();
		}
		if (trayItem != null) {
			trayItem.dispose();
		}
	}
}
