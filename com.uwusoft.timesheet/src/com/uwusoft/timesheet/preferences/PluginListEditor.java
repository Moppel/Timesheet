package com.uwusoft.timesheet.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Composite;

public class PluginListEditor extends AbstractListEditor {
	private Map<String, String> systems;
	private String serviceName;
	
	public PluginListEditor(String name, String labelText, String serviceId, String serviceName, Composite parent) {
		super(name, labelText, parent);
		this.serviceName = serviceName;
		systems = new HashMap<String, String>();
		for (IConfigurationElement e : Platform.getExtensionRegistry().getConfigurationElementsFor(serviceId)) {
			String contributorName = e.getContributor().getName();
			systems.put(Character.toUpperCase(contributorName.toCharArray()[contributorName.lastIndexOf('.') + 1])
					+ contributorName.substring(contributorName.lastIndexOf('.') + 2, contributorName.indexOf(serviceName)),
					contributorName);
		}
	}

	@Override
	protected String getItem(String item) {
		return systems.get(item);
	}

	@Override
	protected List<String> getListForDialog() {
		return new ArrayList<String>(systems.keySet());
	}

	@Override
	protected String getStringForToken(String token) {
		return Character.toUpperCase(token.toCharArray()[token.lastIndexOf('.') + 1])
				+ token.substring(token.lastIndexOf('.') + 2, token.indexOf(serviceName));
	}

}
