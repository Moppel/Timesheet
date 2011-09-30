package com.uwusoft.timesheet.util;

import java.io.IOException;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;

public class SecurePreferencesManager {
    private ISecurePreferences preferences;
    private ISecurePreferences node;
    private static String title = "Secure Preferences";

    /**
	 * @param name name of the {@link ISecurePreferences#node(String)}
	 */
	public SecurePreferencesManager(String name) {
		preferences = SecurePreferencesFactory.getDefault();
		node = preferences.node(name);
	}

	public String getProperty(String key) {
		try {
			return node.get(key, null);
		} catch (StorageException e) {
			MessageBox.setError(title, e.getLocalizedMessage());
		}
		return null;
	}
    
	public void storeProperty(String key, String value) {
		try {
			node.put(key, value, true);
			preferences.flush();
		} catch (StorageException e) {
			MessageBox.setError(title, e.getLocalizedMessage());
		} catch (IOException e) {
			MessageBox.setError(title, e.getLocalizedMessage());
		}
    }
	
	public void removeProperty(String key) {
		node.remove(key);
	}
}
