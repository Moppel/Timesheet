package com.uwusoft.timesheet.util;

import java.io.File;
import java.io.FileInputStream;
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
	public PropertiesUtil(String name) {
		fileName = name + ".properties";
		comment = name + " Properties";

		transferProps = new Properties();

		String dir = System.getProperty("user.home") + "/.timesheet/";
		File userDir = new File(dir);
		
		props = new File(dir + fileName);
		try {
			if (!props.exists()) {
				userDir.mkdir();
				transferProps.load(this.getClass().getResourceAsStream(fileName));
				transferProps.store(new FileOutputStream(props), comment);
			} else
				transferProps.load(new FileInputStream(props));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getProperty(String key) {
		return transferProps.getProperty(key);
	}
    
	public void storeProperty(String key, String value) throws IOException {
        OutputStream out = new FileOutputStream(props);
        transferProps.setProperty(key, value);
        transferProps.store(out, comment);
        out.close();
    }
}
