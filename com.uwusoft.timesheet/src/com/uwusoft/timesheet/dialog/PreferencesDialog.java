package com.uwusoft.timesheet.dialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.uwusoft.timesheet.extensionpoint.PreferencesService;
import com.uwusoft.timesheet.util.ExtensionManager;

public class PreferencesDialog extends Dialog {
	private String serviceId, serviceName;
	private boolean multipleSelect;
	private Map<String, String> selectedSystems;
	private String selectedSystem;
	private List<IPreferencePage> pages;

	public PreferencesDialog(Display display, String serviceId, String serviceName, boolean multipleSelect) {
		super(new Shell(display, SWT.NO_TRIM | SWT.ON_TOP));
		this.serviceId = serviceId;
		this.serviceName = serviceName;
		this.multipleSelect = multipleSelect;
		selectedSystems = new HashMap<String, String>();
		pages = new ArrayList<IPreferencePage>();
	}

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Select and configure " + serviceName + " system(s)");
    }
	
    @Override
	protected Control createDialogArea(Composite parent) {
        final Composite composite = (Composite) super.createDialogArea(parent);
        
        Combo systemCombo = null;
        if (!multipleSelect)
        	systemCombo = new Combo(composite, SWT.READ_ONLY);
		
        for (IConfigurationElement e : Platform.getExtensionRegistry().getConfigurationElementsFor(serviceId)) {
			final String contributorName = e.getContributor().getName();
			final String descriptiveName = Character.toUpperCase(contributorName.toCharArray()[contributorName.lastIndexOf('.') + 1])
        			+ contributorName.substring(contributorName.lastIndexOf('.') + 2, contributorName.indexOf(serviceName));
			PreferencesService preferencesService = new ExtensionManager<PreferencesService>(serviceId).getService(contributorName);
	        IPreferenceNode node = findNodeMatching(preferencesService.getPreferencePageId()) ;
	        node.createPage();
	        final IPreferencePage page = node.getPage();
	        if (multipleSelect) {
	        	Button systemCheckBox = new Button(composite, SWT.CHECK);
	        	systemCheckBox.setText(descriptiveName);
	        	systemCheckBox.addSelectionListener(new SelectionAdapter() {
	        		public void widgetSelected(SelectionEvent evt) {
	        			if (((Button) evt.getSource()).getSelection()) {
	        				page.setVisible(true);
	        				selectedSystems.put(descriptiveName, contributorName);
	        				pages.add(page);
	        			}
	        			else {
	        				page.setVisible(false);
	        				selectedSystems.remove(descriptiveName);
	        				pages.remove(page);
	        			}
	        		};
	        	});
	        }
	        else {
				selectedSystems.put(descriptiveName, contributorName);
	        	systemCombo.add(descriptiveName);
	        	systemCombo.addSelectionListener(new SelectionAdapter() {
	        		public void widgetSelected(SelectionEvent evt) {
	        			selectedSystem = ((Combo) evt.getSource()).getText();
	        			page.setVisible(true);
	        		}
	        	});
	        }	        
	        page.createControl(composite) ;
	        page.setVisible(false);
		}
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
		for (IPreferencePage page : pages) {
			if (!page.performOk()){
				hasFailedOK = true;
				return;
			}
			//Don't bother closing if the OK failed
			if(hasFailedOK) {
				setReturnCode(2);
				getButton(IDialogConstants.OK_ID).setEnabled(true);
				return;
			}
			IPreferenceStore store = ((PreferencePage) page).getPreferenceStore();
			if (store != null && store.needsSaving()
					&& store instanceof IPersistentPreferenceStore) {
				try {
					((IPersistentPreferenceStore) store).save();
				} catch (IOException e) {
					String message =JFaceResources.format(
							"PreferenceDialog.saveErrorMessage", new Object[] { page.getTitle(), e.getMessage() }); //$NON-NLS-1$
					Policy.getStatusHandler().show(
							new Status(IStatus.ERROR, Policy.JFACE, message, e),
							JFaceResources.getString("PreferenceDialog.saveErrorTitle")); //$NON-NLS-1$
								
				}
			}
		}
		setReturnCode(OK);
		close();
	}

	public boolean close() {
		for (IPreferencePage page : pages) {
			page.dispose();
		}
		return super.close();
	}
	
	public Collection<String> getSelectedSystems() {
		return selectedSystems.values();
	}

	public String getSelectedSystem() {
		return selectedSystems.get(selectedSystem);
	}
}
