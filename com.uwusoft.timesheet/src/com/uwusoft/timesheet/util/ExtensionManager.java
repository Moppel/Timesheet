package com.uwusoft.timesheet.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import com.uwusoft.timesheet.extensionpoint.SubmissionServiceImpl;

public class ExtensionManager<T> {

	private IConfigurationElement[] config;
	private static Map<String, List<Object>> services = new HashMap<String, List<Object>>();

	/**
	 * @param serviceId
	 */
	public ExtensionManager(String serviceId) {
		config = Platform.getExtensionRegistry().getConfigurationElementsFor(serviceId);
	}
	
	public static List<Object> getServices(String serviceId, String contributorName) {
		if (services.get(serviceId) == null) {
			services.put(serviceId, new ArrayList<Object>());
			for (IConfigurationElement e : Platform.getExtensionRegistry().getConfigurationElementsFor(serviceId)) {
				if (e.getContributor().getName().equals(contributorName))
					try {
						services.get(serviceId).add(e.createExecutableExtension("class"));
					} catch (CoreException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			}
		}
		return services.get(serviceId);
	}
	
	@SuppressWarnings("unchecked")
	public T getService(String contributorName) {
		try {
			for (IConfigurationElement e : config) {
				//System.out.println("Evaluating extension " + e.getContributor().getName());
				if (e.getContributor().getName().equals(contributorName))
					return (T) e.createExecutableExtension("class");
			}
		} catch (ClassCastException ex) {
            ex.printStackTrace(); // TODO
		} catch (CoreException ex) {
            ex.printStackTrace(); // TODO
		}
		return (T) new SubmissionServiceImpl(); // TODO submission service plug-in
	}
}
