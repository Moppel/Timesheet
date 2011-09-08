package com.uwusoft.timesheet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.util.ExtensionManager;
import com.uwusoft.timesheet.util.PropertiesUtil;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	private IWorkbenchWindow window;
	private TrayItem trayItem;
	private Image trayImage;
	private final static String COMMAND_ID = "org.eclipse.ui.file.exit";

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
		trayItem = initTaskItem(window);
		// Some OS might not support tray items
		if (trayItem != null) {
			minimizeBehavior();
			// Create exit and about action on the icon
			hookPopupMenu();
		}
	}

	// Add a listener to the shell
	
	private void minimizeBehavior() {
		window.getShell().addShellListener(new ShellAdapter() {
			// If the window is minimized hide the window
			public void shellIconified(ShellEvent e) {
				window.getShell().setVisible(false);
			}
		});
		// If user double-clicks on the tray icons the application will be
		// visible again
		trayItem.addListener(SWT.DefaultSelection, new Listener() {
			public void handleEvent(Event event) {
				Shell shell = window.getShell();
				if (!shell.isVisible()) {
					window.getShell().setMinimized(false);
					shell.setVisible(true);
				}
			}
		});
	}

	private void hookPopupMenu() {
		trayItem.addListener(SWT.MenuDetect, new Listener() {
			public void handleEvent(Event event) {
				Menu menu = new Menu(window.getShell(), SWT.POP_UP);

				MenuItem submit = new MenuItem(menu, SWT.NONE);
				submit.setText("Submit");
				submit.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						new ExtensionManager<StorageService>(
								StorageService.SERVICE_ID).getService(
								new PropertiesUtil("Timesheet")
										.getProperty(StorageService.PROPERTY))
								.submitEntries();
					}
				});
				MenuItem changeTasks = new MenuItem(menu, SWT.NONE);
				changeTasks.setText("Change Task");
				changeTasks.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						StorageService storageService = new ExtensionManager<StorageService>(
								StorageService.SERVICE_ID).getService(
								new PropertiesUtil("Timesheet")
										.getProperty(StorageService.PROPERTY));
						ListDialog listDialog = new ListDialog(window.getShell());
						listDialog.setTitle("Tasks");
						listDialog.setMessage("Select next task");
						listDialog.setContentProvider(ArrayContentProvider.getInstance());
						listDialog.setLabelProvider(new LabelProvider());
						listDialog.setInput(storageService.getTasks().get("Primavera")); // TODO
						if (listDialog.open() == Dialog.OK) {
						    String selectedTask = Arrays.toString(listDialog.getResult());
							if (selectedTask == null) return;
			                Date now = new Date();
			                PropertiesUtil props = new PropertiesUtil("Timesheet");
			                storageService.storeTimeEntry(now, props.getProperty("task.last"));
			                try {
								props.storeProperty("task.last", selectedTask);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}						
					}
				});
				// Creates a new menu item that terminates the program
				// when selected
				MenuItem exit = new MenuItem(menu, SWT.NONE);
				exit.setText("Exit");
				exit.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						// Lets call our command
						IHandlerService handlerService = (IHandlerService) window.getService(IHandlerService.class);
						try {
							handlerService.executeCommand(COMMAND_ID, null);
						} catch (Exception ex) {
							throw new RuntimeException(COMMAND_ID);
						}
					}
				});
				// We need to make the menu visible
				menu.setVisible(true);
			}
		});
	}

	// This methods create the tray item and return a reference
	private TrayItem initTaskItem(IWorkbenchWindow window) {
		final Tray tray = window.getShell().getDisplay().getSystemTray();
		TrayItem trayItem = new TrayItem(tray, SWT.NONE);
		trayImage = AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/clock.png").createImage();
		trayItem.setImage(trayImage);
		/*final ToolTip tip = new ToolTip(window.getShell(), SWT.BALLOON | SWT.ICON_INFORMATION);
		tip.setText("Timesheet");
		tip.setVisible(true);*/
		trayItem.setToolTipText("Timesheet");
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
