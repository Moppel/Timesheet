package com.uwusoft.timesheet.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

public class PropertiesUtil {

    private String fileName;
	private File props;
    private Properties transferProps;
    private String comment;

    /**
	 * @param name
	 */
	public PropertiesUtil(Class<?> clas, String name) {
		fileName = name + ".properties";
		comment = name + " Properties";

		transferProps = new Properties();

		String dir = System.getProperty("user.home") + "/.timesheet/";
		File userDir = new File(dir);
		
		props = new File(dir + fileName);
		try {
			if (!props.exists()) {
				userDir.mkdir();
				transferProps.load(clas.getResourceAsStream(fileName));
				transferProps.store(new FileOutputStream(props), comment);
			} else
				transferProps.load(new FileInputStream(props));
		} catch (IOException e) {
			MessageBox.setError("Configuration file", e.getLocalizedMessage());
		}
	}

	public String getProperty(String key) {
		return transferProps.getProperty(key);
	}
    
	public void storeProperty(String key, String value) {
        OutputStream out;
		try {
			out = new FileOutputStream(props);
	        transferProps.setProperty(key, value);
	        transferProps.store(out, comment);
	        out.close();
		} catch (FileNotFoundException e) {
			MessageBox.setError("Configuration file not found", e.getLocalizedMessage());
		} catch (IOException e) {
			MessageBox.setError("Configuration file", e.getLocalizedMessage());
		}
    }
	
	public void removeProperty(String key) {
        OutputStream out;
		try {
			out = new FileOutputStream(props);
	        transferProps.remove(key);
	        transferProps.store(out, comment);
	        out.close();
		} catch (FileNotFoundException e) {
			MessageBox.setError("Configuration file not found", e.getLocalizedMessage());
		} catch (IOException e) {
			MessageBox.setError("Configuration file", e.getLocalizedMessage());
		}
	}
}
