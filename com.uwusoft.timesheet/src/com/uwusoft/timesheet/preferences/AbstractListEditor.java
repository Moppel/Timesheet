package com.uwusoft.timesheet.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ListDialog;

import com.uwusoft.timesheet.extensionpoint.SubmissionService;


public abstract class AbstractListEditor extends ListEditor {

	public AbstractListEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
	}

	@Override
	protected String createList(String[] items) {
        StringBuffer path = new StringBuffer();
        for (String item : items) {
            path.append(getItem(item));
            path.append(SubmissionService.separator);
        }
        return path.toString();
	}
	
	protected abstract String getItem(String item);

	@Override
	protected String getNewInputObject() {
		ListDialog listDialog = new ListDialog(getShell());
		listDialog.setTitle(getLabelText());
		listDialog.setMessage("Select " + getLabelText());
		listDialog.setContentProvider(ArrayContentProvider.getInstance());
		listDialog.setLabelProvider(new LabelProvider());
		listDialog.setWidthInChars(70);
		List<String> list = new ArrayList<String>(getListForDialog());
		list.removeAll(Arrays.asList(getList().getItems()));
		listDialog.setInput(list);
		if (listDialog.open() == Dialog.OK) {
		    String selectedObject = Arrays.toString(listDialog.getResult());
		    selectedObject = selectedObject.substring(selectedObject.indexOf("[") + 1, selectedObject.indexOf("]"));
			return selectedObject;
		}
		return null;
	}
	
	protected abstract List<String> getListForDialog();

	@Override
	protected String[] parseString(String stringList) {
        StringTokenizer st = new StringTokenizer(stringList, SubmissionService.separator + "\n\r");//$NON-NLS-1$
        List<String> v = new LinkedList<String>();
        while (st.hasMoreElements()) {
        	String nextToken = st.nextToken();
            v.add(getStringForToken(nextToken));
        }
        return (String[]) v.toArray(new String[v.size()]);
	}

	protected abstract String getStringForToken(String token);
}
