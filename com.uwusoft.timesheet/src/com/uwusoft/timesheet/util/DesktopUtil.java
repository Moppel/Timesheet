package com.uwusoft.timesheet.util;

import java.awt.Desktop;
import java.net.URI;

public class DesktopUtil {
	private static String title = "Desktop";
	
	public static void openUrl(String url) {
		if(!Desktop.isDesktopSupported()) {
			MessageBox.setError(title, "Desktop is not supported (fatal)");
			return;
	    }				
	    Desktop desktop = Desktop.getDesktop();
	    if(!desktop.isSupported(Desktop.Action.BROWSE)) {
	    	MessageBox.setError(title, "Desktop doesn't support the browse action (fatal)");
	    	return;
	    }
        try {
	        URI uri = new URI(url);
	        desktop.browse(uri);
        }
        catch (Exception e) {
        	MessageBox.setError(title, e.getMessage());
        }		
	}
}
