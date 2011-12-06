package com.uwusoft.timesheet.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
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
	
	public PluginListEditor(String name, String labelText, String serviceId, String serviceName,
			Composite parent) {
		init(name, labelText);
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
        StringBuffer path = new StringBuffer("");//$NON-NLS-1$
        for (int i = 0; i < items.length; i++) {
            path.append(items[i]);
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
		listDialog.setInput(systems.keySet());
		if (listDialog.open() == Dialog.OK) {
		    String selectedSystem = Arrays.toString(listDialog.getResult());
		    selectedSystem = selectedSystem.substring(selectedSystem.indexOf("[") + 1, selectedSystem.indexOf("]"));
			if (StringUtils.isEmpty(selectedSystem)) return null;
			return systems.get(selectedSystem);
		}
		return null;
	}

	@Override
	protected String[] parseString(String stringList) {
        StringTokenizer st = new StringTokenizer(stringList, SubmissionService.separator + "\n\r");//$NON-NLS-1$
        ArrayList<String> v = new ArrayList<String>();
        while (st.hasMoreElements()) {
            v.add(st.nextToken());
        }
        return (String[]) v.toArray(new String[v.size()]);
	}

}
