package com.uwusoft.timesheet.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.Messages;
import com.uwusoft.timesheet.extensionpoint.AllDayTaskService;
import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
import com.uwusoft.timesheet.model.AllDayTasks;


public class AllDayTaskFactory extends ExtensionContributionFactory {

	MenuManager mgr;
	
	public AllDayTaskFactory() {
	}

	public AllDayTaskFactory(MenuManager mgr) {
		this.mgr = mgr;
	}

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
        Map <String, String> parameters = new HashMap<String, String>();
        for (String task : getAllDayTasks()) {
        	parameters.put("Timesheet.commands.task", task);
       		task = Messages.getString(task);
        	CommandContributionItemParameter p = new CommandContributionItemParameter(serviceLocator, null, "Timesheet.allDayTask", CommandContributionItem.STYLE_PUSH);
        	p.label = task;
        	p.parameters = parameters;
        	if (additions == null)
        		mgr.add(new CommandContributionItem(p));
        	else
        		additions.addContributionItem(new CommandContributionItem(p), null);
        }
	}

	public static Set<String> getAllDayTasks() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		Set<String> allDayTasks = new HashSet<String>(Arrays.asList(AllDayTasks.allDayTasks));    	
    	for (String task : LocalStorageService.getInstance().getAllDayTasks()) {
    		String property = AllDayTaskService.PREFIX + task.replaceAll("\\s", "_").toLowerCase();
    		if (StringUtils.isEmpty(preferenceStore.getString(property))) continue;
    		allDayTasks.add(property);
    	}
		return allDayTasks;
	}
}
