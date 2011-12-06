package com.uwusoft.timesheet.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ListDialog;

import com.uwusoft.timesheet.extensionpoint.SubmissionService;

public class PluginListEditor extends ListEditor {

	private Map<String, String> systems;
	private String serviceName;
	
	public PluginListEditor(String name, String labelText, String serviceId, String serviceName,
			Composite parent) {
		init(name, labelText);
		this.serviceName = serviceName;
		systems = new HashMap<String, String>();
		for (IConfigurationElement e : Platform.getExtensionRegistry().getConfigurationElementsFor(serviceId)) {
			String contributorName = e.getContributor().getName();
			systems.put(Character.toUpperCase(contributorName.toCharArray()[contributorName.lastIndexOf('.') + 1])
					+ contributorName.substring(contributorName.lastIndexOf('.') + 2, contributorName.indexOf(serviceName)),
					contributorName);
		}
        createControl(parent);
	}

	@Override
	protected String createList(String[] items) {
        StringBuffer path = new StringBuffer();
        for (int i = 0; i < items.length; i++) {
            path.append(systems.get(items[i]));
            path.append(SubmissionService.separator);
        }
        return path.toString();
	}

	@Override
	protected String getNewInputObject() {
		ListDialog listDialog = new ListDialog(getShell());
		listDialog.setTitle(getLabelText());
		listDialog.setMessage("Select " + getLabelText());
		listDialog.setContentProvider(ArrayContentProvider.getInstance());
		listDialog.setLabelProvider(new LabelProvider());
		listDialog.setWidthInChars(70);
		List<String> systemList = new ArrayList<String>(systems.keySet());
		systemList.removeAll(Arrays.asList(getList().getItems()));
		listDialog.setInput(systemList);
		if (listDialog.open() == Dialog.OK) {
		    String selectedSystem = Arrays.toString(listDialog.getResult());
		    selectedSystem = selectedSystem.substring(selectedSystem.indexOf("[") + 1, selectedSystem.indexOf("]"));
			return selectedSystem;
		}
		return null;
	}

	@Override
	protected String[] parseString(String stringList) {
        StringTokenizer st = new StringTokenizer(stringList, SubmissionService.separator + "\n\r");//$NON-NLS-1$
        ArrayList<String> v = new ArrayList<String>();
        while (st.hasMoreElements()) {
        	String system = st.nextToken();
            v.add(Character.toUpperCase(system.toCharArray()[system.lastIndexOf('.') + 1])
					+ system.substring(system.lastIndexOf('.') + 2, system.indexOf(serviceName)));
        }
        return (String[]) v.toArray(new String[v.size()]);
	}

}
