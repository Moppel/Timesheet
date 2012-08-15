package com.uwusoft.timesheet.dialog;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public abstract class SystemDialog extends Dialog {
	private String serviceId, serviceName;
	protected Map<String, String> selectedSystems = new HashMap<String, String>();
	
	public SystemDialog(Display display, String serviceId, String serviceName) {
		super(new Shell(display, SWT.NO_TRIM | SWT.ON_TOP));
		this.serviceId = serviceId;
		this.serviceName = serviceName;
	}

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Select " + serviceName + " system");
    }
	
	@Override
	protected Control createDialogArea(Composite parent) {
        final Composite composite = (Composite) super.createDialogArea(parent);
        Composite component = createComponent(composite);
        for (IConfigurationElement e : Platform.getExtensionRegistry().getConfigurationElementsFor(serviceId)) {
			final String contributorName = e.getContributor().getName();
			final String descriptiveName = Character.toUpperCase(contributorName.toCharArray()[contributorName.lastIndexOf('.') + 1])
        			+ contributorName.substring(contributorName.lastIndexOf('.') + 2, contributorName.indexOf(serviceName));
        	fillComponent(composite, component, descriptiveName, contributorName);
        }
        return composite;
	}

	protected abstract Composite createComponent(final Composite composite);
	
	protected abstract void fillComponent(Composite composite, Composite component, final String descriptiveName, final String contributorName);
}
