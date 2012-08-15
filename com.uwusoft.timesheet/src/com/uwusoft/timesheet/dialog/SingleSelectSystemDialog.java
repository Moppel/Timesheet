package com.uwusoft.timesheet.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class SingleSelectSystemDialog extends SystemDialog {
	private String selectedSystem;

	public SingleSelectSystemDialog(Display display, String serviceId, String serviceName) {
		super(display, serviceId, serviceName);
	}

	protected Composite createComponent(final Composite composite) {
		return new Combo(composite, SWT.READ_ONLY);
	}

	protected void fillComponent(Composite composite, Composite component, final String descriptiveName, String contributorName) {
		selectedSystems.put(descriptiveName, contributorName);
		Combo systemCombo = (Combo) component;
		systemCombo.add(descriptiveName);
		systemCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				selectedSystem = ((Combo) evt.getSource()).getText();
			}
		});
		systemCombo.select(0);
		selectedSystem = systemCombo.getItem(0);
	}
	
	public String getSelectedSystem() {
		return selectedSystems.get(selectedSystem);
	}
}
