package com.uwusoft.timesheet.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public class ExtensionUtil<T> {

	private IConfigurationElement[] config;

	/**
	 * @param serviceId
	 */
	public ExtensionUtil(String serviceId) {
		config = Platform.getExtensionRegistry().getConfigurationElementsFor(serviceId);
	}
	
	@SuppressWarnings("unchecked")
	public T getService(String contributorName) {
		try {
			for (IConfigurationElement e : config) {
				System.out.println("Evaluating extension " + e.getContributor().getName());
				if (e.getContributor().getName().equals(contributorName))
					return (T) e.createExecutableExtension("class");
			}
		} catch (ClassCastException ex) {
            ex.printStackTrace(); // TODO
		} catch (CoreException ex) {
            ex.printStackTrace(); // TODO
		}
		return null;
	}
}
