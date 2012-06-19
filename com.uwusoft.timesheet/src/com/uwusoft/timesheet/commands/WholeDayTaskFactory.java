package com.uwusoft.timesheet.commands;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

import com.uwusoft.timesheet.Messages;
import com.uwusoft.timesheet.model.WholeDayTasks;


public class WholeDayTaskFactory extends ExtensionContributionFactory {

	public WholeDayTaskFactory() {
	}

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
        Map <String, String> parameters = new HashMap<String, String>();
        for (String task : WholeDayTasks.wholeDayTasks) {
        	parameters.put("Timesheet.commands.task", task);
        	CommandContributionItemParameter p = new CommandContributionItemParameter(serviceLocator, null, "Timesheet.wholeDayTask", CommandContributionItem.STYLE_PUSH);
        	p.label = Messages.getString(task);
        	p.parameters = parameters;
        	additions.addContributionItem(new CommandContributionItem(p), null);
        }
	}
}
