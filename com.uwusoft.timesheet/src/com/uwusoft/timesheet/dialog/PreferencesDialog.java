package com.uwusoft.timesheet.dialog;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class PreferencesDialog extends Dialog {
	private IPreferenceNode node;

	public PreferencesDialog(Display display, String preferencePageId) {
		super(new Shell(display, SWT.NO_TRIM | SWT.ON_TOP));
        node = findNodeMatching(preferencePageId) ;
        node.createPage();
	}

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(node.getLabelText());
    }
	
    @Override
	protected Control createDialogArea(Composite parent) {
        final Composite composite = (Composite) super.createDialogArea(parent);
        IPreferencePage page = node.getPage();
        page.createControl(composite) ;
        return composite;
	}

	@SuppressWarnings("rawtypes")
	private IPreferenceNode findNodeMatching(String nodeId) {
		List nodes = PlatformUI.getWorkbench().getPreferenceManager().getElements(PreferenceManager.POST_ORDER);
		for (Iterator i = nodes.iterator(); i.hasNext();) {
			IPreferenceNode node = (IPreferenceNode) i.next();
			if (node.getId().equals(nodeId)) {
				return node;
			}
		}
		return null;
	}

	@Override
	protected void okPressed() {
		boolean hasFailedOK = false;
		if (!node.getPage().performOk()){
			hasFailedOK = true;
			return;
		}
		//Don't bother closing if the OK failed
		if(hasFailedOK) {
			setReturnCode(2);
			getButton(IDialogConstants.OK_ID).setEnabled(true);
			return;
		}
		IPreferenceStore store = ((PreferencePage) node.getPage()).getPreferenceStore();
		if (store != null && store.needsSaving() && store instanceof IPersistentPreferenceStore) {
			try {
				((IPersistentPreferenceStore) store).save();
			} catch (IOException e) {
				String message =JFaceResources.format(
						"PreferenceDialog.saveErrorMessage", new Object[] { node.getPage().getTitle(), e.getMessage() }); //$NON-NLS-1$
				Policy.getStatusHandler().show(
						new Status(IStatus.ERROR, Policy.JFACE, message, e),
						JFaceResources.getString("PreferenceDialog.saveErrorTitle")); //$NON-NLS-1$								
			}
		}
		setReturnCode(OK);
		close();
	}

	public boolean close() {
		node.disposeResources();
		return super.close();
	}
}
