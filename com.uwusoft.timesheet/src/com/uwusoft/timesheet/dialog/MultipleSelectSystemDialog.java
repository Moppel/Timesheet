package com.uwusoft.timesheet.dialog;

import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class MultipleSelectSystemDialog extends SystemDialog {

	public MultipleSelectSystemDialog(Display display, String serviceId, String serviceName) {
		super(display, serviceId, serviceName);
	}

	@Override
	protected Composite createComponent(Composite composite) {
		return null;
	}

	@Override
	protected void fillComponent(Composite composite, Composite component, final String descriptiveName, final String contributorName) {
    	Button systemCheckBox = new Button(composite, SWT.CHECK);
    	systemCheckBox.setText(descriptiveName);
    	systemCheckBox.addSelectionListener(new SelectionAdapter() {
    		public void widgetSelected(SelectionEvent evt) {
    			if (((Button) evt.getSource()).getSelection())
    				selectedSystems.put(descriptiveName, contributorName);
    			else
    				selectedSystems.remove(descriptiveName);
    		};
    	});
	}

	public Collection<String> getSelectedSystems() {
		return selectedSystems.values();
	}
}
