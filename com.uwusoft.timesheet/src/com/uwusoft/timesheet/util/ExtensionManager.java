package com.uwusoft.timesheet.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;

import com.uwusoft.timesheet.Activator;
import com.uwusoft.timesheet.extensionpoint.StorageService;

public class ExtensionManager<T> {

	private String serviceId;
	private static Map<String, Map<String, Object>> services = new HashMap<String, Map<String, Object>>();

	/**
	 * @param serviceId
	 */
	public ExtensionManager(String serviceId) {
		this.serviceId = serviceId;
	}
	
	@SuppressWarnings("unchecked")
	public T getService(String contributorName) {
		if (services.get(serviceId) == null) {
			services.put(serviceId, new HashMap<String, Object>());
			for (IConfigurationElement e : Platform.getExtensionRegistry().getConfigurationElementsFor(serviceId)) {
				if (e.getContributor().getName().equals(contributorName))
					try {
						services.get(serviceId).put(contributorName, e.createExecutableExtension("class"));
					} catch (CoreException e1) {
						IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
						if (contributorName.equals(preferenceStore.getString(StorageService.PROPERTY))) {
							for (IConfigurationElement e2 : Platform.getExtensionRegistry().getConfigurationElementsFor(serviceId)) {
								if (e2.getContributor().getName().equals("com.uwusoft.timesheet.localstorage"))
									try {
										return (T) e2.createExecutableExtension("class");
									} catch (CoreException e3) {
										MessageBox.setError(this.getClass().getSimpleName(), e3.getLocalizedMessage());
									}
							}
						}
						MessageBox.setError(this.getClass().getSimpleName(), e1.getLocalizedMessage());
					}
			}
		}
		return (T) services.get(serviceId).get(contributorName);
	}
}
