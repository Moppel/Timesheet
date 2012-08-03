package com.uwusoft.timesheet;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.services.ISourceProviderService;

import com.uwusoft.timesheet.commands.SessionSourceProvider;
import com.uwusoft.timesheet.commands.WholeDayTaskFactory;
import com.uwusoft.timesheet.util.AutomaticCheckoutCheckinUtil;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	private IWorkbenchWindow window;
	private TrayItem trayItem;
	private Image trayImage;
	private ApplicationActionBarAdvisor actionBarAdvisor;

	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
		actionBarAdvisor = new ApplicationActionBarAdvisor(configurer);
		return actionBarAdvisor;
	}

	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setInitialSize(new Point(900, 500));
		configurer.setShowCoolBar(true);
		configurer.setShowProgressIndicator(true);
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
			window.getShell().setVisible(false); // initially minimize to system tray
			hookPopupMenu();
		}
		AutomaticCheckoutCheckinUtil.execute();
	}

	public boolean preWindowShellClose() {
		if (trayItem != null) {
			window.getShell().setVisible(false);
			return false; // if window close button pressed only minimize to system tray (don't exit the program)
		}
		return true;
	}
	
	private void hookPopupMenu() {
		trayItem.addListener(SWT.MenuDetect, new Listener() {
			public void handleEvent(Event event) {
				MenuManager trayMenu = new MenuManager();
                
				ISourceProviderService sourceProviderService = (ISourceProviderService) window.getService(ISourceProviderService.class);
				SessionSourceProvider commandStateService = (SessionSourceProvider) sourceProviderService.getSourceProvider(SessionSourceProvider.SESSION_STATE);
				String sessionState = (String) commandStateService.getCurrentState().get(SessionSourceProvider.SESSION_STATE);
				String breakState = (String) commandStateService.getCurrentState().get(SessionSourceProvider.BREAK_STATE);
				
				if (SessionSourceProvider.DISABLED.equals(sessionState)) {					
                    CommandContributionItemParameter p = new CommandContributionItemParameter(window, null, "Timesheet.checkin", CommandContributionItem.STYLE_PUSH);
                    p.icon = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "/icons/check_in_16.png");
                    trayMenu.add(new CommandContributionItem(p));
                }
                else {
                	CommandContributionItemParameter p = new CommandContributionItemParameter(window, null, "Timesheet.changeTask", CommandContributionItem.STYLE_PUSH);
                    p.icon = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "/icons/task_16.png");
                    trayMenu.add(new CommandContributionItem(p));
            				
                    if (SessionSourceProvider.DISABLED.equals(breakState)) {
                        p = new CommandContributionItemParameter(window, null, "Timesheet.setBreak", CommandContributionItem.STYLE_PUSH);
                        p.icon = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "/icons/pause_16.png");
                        trayMenu.add(new CommandContributionItem(p));
                    }

                    MenuManager wholeDayTask = new MenuManager("Set whole day task",
                    		AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "/icons/day_16.png"), "Timesheet.wholeDayTask");
                    
                    new WholeDayTaskFactory(wholeDayTask).createContributionItems(window, null);

                    trayMenu.add(wholeDayTask);

                    p = new CommandContributionItemParameter(window, null, "Timesheet.checkout", CommandContributionItem.STYLE_PUSH);
                    p.icon = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "/icons/check_out_16.png");
                    trayMenu.add(new CommandContributionItem(p));
                }
                trayMenu.add(new Separator());
                
                CommandContributionItemParameter p = 
        				new CommandContributionItemParameter(window, null, "Timesheet.importTasks", CommandContributionItem.STYLE_PUSH);
                p.icon = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "/icons/import_16.png");
                trayMenu.add(new CommandContributionItem(p));

				p = new CommandContributionItemParameter(window, null, "Timesheet.submit", CommandContributionItem.STYLE_PUSH);
                p.icon = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "/icons/signs_16.png");
                trayMenu.add(new CommandContributionItem(p));
                
                trayMenu.add(new Separator());
			    
                Menu menu = trayMenu.createContextMenu(window.getShell());
			    actionBarAdvisor.fillTrayItem(trayMenu);
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
