package com.uwusoft.timesheet.googlestorage;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.swt.widgets.Composite;

public class SpreadsheetFieldEditor extends StringButtonFieldEditor {

	public SpreadsheetFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
        setChangeButtonText("Select spreadsheet");
	}

	@Override
	protected String changePressed() {
		SpreadsheetListDialog listDialog = new SpreadsheetListDialog(getShell());
		if (listDialog.open() == Dialog.OK)
		    return listDialog.getSpreadsheetKey();
		return null;
	}
}
