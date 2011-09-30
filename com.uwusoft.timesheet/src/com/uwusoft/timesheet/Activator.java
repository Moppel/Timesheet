package com.uwusoft.timesheet;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.uwusoft.timesheet.util.MessageBox;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.uwusoft.timesheet"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
    /**
     * Storage for preferences.
     */
    private PreferenceStore preferenceStore;

    /**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#getPreferenceStore()
	 */
	@Override
	public IPreferenceStore getPreferenceStore() {
        if (preferenceStore == null) {
        	String dir = System.getProperty("user.home") + "/.eclipse/" + PLUGIN_ID;
        	File prefs = new File(dir);
        	if (!prefs.exists()) prefs.mkdirs();
        	String path = dir + "/" + PLUGIN_ID.substring(PLUGIN_ID.lastIndexOf('.') + 1, PLUGIN_ID.length()) + ".prefs";
        	prefs = new File(path);
            try {
            	if (!prefs.exists()) prefs.createNewFile();
                preferenceStore = new PreferenceStore(path);
				preferenceStore.load();
			} catch (IOException e) {
				MessageBox.setError("Activator", e.getLocalizedMessage());
			}
        }
        return preferenceStore;
	}
}
