package com.uwusoft.timesheet;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.uwusoft.timesheet.dialog.DateDialog;
import com.uwusoft.timesheet.dialog.TimeDialog;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.util.ExtensionManager;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	private IWorkbenchWindow window;
	private TrayItem trayItem;
	private Image trayImage;
	private ApplicationActionBarAdvisor actionBarAdvisor;
	private IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();

	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
		actionBarAdvisor = new ApplicationActionBarAdvisor(configurer);
		return actionBarAdvisor;
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
			window.getShell().setVisible(false);
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
				MenuManager trayMenu = new MenuManager();
                trayMenu.add(new CommandContributionItem(
        				new CommandContributionItemParameter(window, null, "Timesheet.changeTask", CommandContributionItem.STYLE_PUSH)));
                
                Map <String, String> parameters = new HashMap<String, String>();
                parameters.put("Timesheet.commands.shutdownTime", TimesheetApp.formatter.format(new Date()));
                CommandContributionItemParameter p = new CommandContributionItemParameter(window, null, "Timesheet.checkout", CommandContributionItem.STYLE_PUSH);
                p.parameters = parameters;         
                trayMenu.add(new CommandContributionItem(p));
                
                trayMenu.add(new ContributionItem() {
                    @Override
                    public void fill(final Menu menu, final int index) {
                        MenuItem submit = new MenuItem(menu, SWT.PUSH);
                        submit.setText("Submit");
                        submit.addSelectionListener(new SelectionAdapter() {
                            @Override
                            public void widgetSelected(final SelectionEvent e) {
        						new ExtensionManager<StorageService>(StorageService.SERVICE_ID).getService(preferenceStore
										.getString(StorageService.PROPERTY)).submitEntries();
                            }
                        });
                    }
                });
			    
                Menu menu = trayMenu.createContextMenu(window.getShell());
			    actionBarAdvisor.fillTrayItem(trayMenu);
			
			    /*if (!StringUtils.isEmpty(preferenceStore.getString(TimesheetApp.LAST_TASK))) {
					MenuItem checkout = new MenuItem(menu, SWT.NONE);
					checkout.setText("Check out");
					checkout.addListener(SWT.Selection, new Listener() { // TODO extract check out command
						public void handleEvent(Event event) {
							TimeDialog timeDialog = new TimeDialog(window.getShell().getDisplay(), "Check out",
									preferenceStore.getString(TimesheetApp.LAST_TASK), new Date());
							if (timeDialog.open() == Dialog.OK) {
								StorageService storageService = new ExtensionManager<StorageService>(
										StorageService.SERVICE_ID).getService(preferenceStore.getString(StorageService.PROPERTY));
								storageService.createTaskEntry(timeDialog.getTime(), preferenceStore.getString(TimesheetApp.LAST_TASK));
								storageService.storeLastDailyTotal();
								if (!StringUtils.isEmpty(preferenceStore.getString(TimesheetApp.DAILY_TASK)))
									storageService.createTaskEntry(timeDialog.getTime(), preferenceStore.getString(TimesheetApp.DAILY_TASK),
											preferenceStore.getString(TimesheetApp.DAILY_TASK_TOTAL));
								preferenceStore.setValue(TimesheetApp.LAST_TASK, StringUtils.EMPTY);
				            	preferenceStore.setValue(TimesheetApp.SYSTEM_SHUTDOWN, TimesheetApp.formatter.format(timeDialog.getTime()));
							}
						}
					});
				}

				MenuItem wholeDayTask = new MenuItem(menu, SWT.CASCADE);
				wholeDayTask.setText("Set whole day task");
				Menu subMenu = new Menu(wholeDayTask);
				wholeDayTask.setMenu(subMenu);
				
				MenuItem holiday = new MenuItem(subMenu, SWT.NONE);
				holiday.setText("Holiday");
				holiday.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						DateDialog dateDialog = new DateDialog(window.getShell().getDisplay(), "Holiday",
								preferenceStore.getString("task.holiday"), new Date());
						if (dateDialog.open() == Dialog.OK) {
							
						}						
					}
				});

				MenuItem vacation = new MenuItem(subMenu, SWT.NONE);
				vacation.setText("Vacation");
				vacation.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						
					}
				});

				MenuItem sick = new MenuItem(subMenu, SWT.NONE);
				sick.setText("Sick Leave");
				sick.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						
					}
				});*/
			    menu.setVisible(true);
			}
		});
	}

	private TrayItem initTaskItem() {
		final Tray tray = window.getShell().getDisplay().getSystemTray();
		final TrayItem trayItem = new TrayItem(tray, SWT.NONE);
		ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "/icons/clock.png");
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
