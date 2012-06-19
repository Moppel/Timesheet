package com.uwusoft.timesheet.commands;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.extensionpoint.StorageService;
import com.uwusoft.timesheet.util.ExtensionManager;

public class SessionSourceProvider extends AbstractSourceProvider {
	public final static String SESSION_STATE = "com.uwusoft.timesheet.commands.sourceprovider.active";
	public final static String ENABLED = "ENABLED";
	public final static String DISABLED = "DISABLED";
	private boolean enabled;

	public SessionSourceProvider() {
		super();
		StorageService storageService = new ExtensionManager<StorageService>(
				StorageService.SERVICE_ID).getService(Activator.getDefault().getPreferenceStore().getString(StorageService.PROPERTY));
		enabled = storageService.getLastTask() != null;  					
	}

	@Override
	public void dispose() {
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public Map getCurrentState() {
		Map<String, String> map = new HashMap<String, String>(1);
		String value = enabled ? ENABLED : DISABLED;
		map.put(SESSION_STATE, value);
		return map;
	}

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] { SESSION_STATE };
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled ;
		String value = enabled ? ENABLED : DISABLED;
		fireSourceChanged(ISources.WORKBENCH, SESSION_STATE, value);
	}
}
