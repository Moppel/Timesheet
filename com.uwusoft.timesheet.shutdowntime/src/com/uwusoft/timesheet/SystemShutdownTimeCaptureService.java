package com.uwusoft.timesheet;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;

/**
 * the system shutdown time capture service
 *
 * @author Uta Wunderlich
 * @version $Revision: $, $Date: Aug 12, 2011
 * @since Aug 12, 2011
 */
public class SystemShutdownTimeCaptureService implements ActionListener {
    private static TrayIcon trayIcon;
    public static SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    public static String tmpFile = "shutdownTime.tmp";
    public static String lckDir = System.getProperty("user.home") + File.separator + ".timesheet" + File.separator;
    private static File tmp;
    private static final String EXIT = "Exit", SHUTDOWN = "Save & Shutdown";
    private static String comment = "System Shutdown Time Capture Service";

    public SystemShutdownTimeCaptureService() {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("Services.png"));

            PopupMenu popup = new PopupMenu();
            MenuItem menuItem = new MenuItem(SHUTDOWN);
            menuItem.addActionListener(this);
            popup.add(menuItem);
            menuItem = new MenuItem(EXIT);
            menuItem.addActionListener(this);
            popup.add(menuItem);

            trayIcon = new TrayIcon(image, comment, popup);
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println(e);
            }
        }
    }

    public void addShutdownHook() {
        Thread saveThread = new SaveThread();
        Runtime.getRuntime().addShutdownHook(saveThread);
        do {
            try {
                Thread.sleep(1);
            }
            catch (InterruptedException ie) {
                Runtime.getRuntime().halt(1);
            }
        }
        while (true);
    }

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {        
        String tmpDir = lckDir;
        if (args.length > 0) tmpDir = args[0];
        RandomAccessFile lockFile = new RandomAccessFile(new File(tmpDir + File.separator + "shutdownTime.lck"), "rw");
		
        FileChannel channel = lockFile.getChannel();
        FileLock lock = channel.tryLock();
        if (lock == null) {
            System.exit(0);
        }		
        tmp = new File(tmpDir + "shutdownTime.tmp");
        
		SystemShutdownTimeCaptureService systemTimeCapture = new SystemShutdownTimeCaptureService();
        systemTimeCapture.addShutdownHook();
    }

    private static void helpAndTerminate(String message) {
        trayIcon.displayMessage(comment, message, TrayIcon.MessageType.ERROR);
        System.exit(1);
    }

    public void actionPerformed(ActionEvent e) {
        MenuItem source = (MenuItem) e.getSource();
        if (SHUTDOWN.equals(source.getLabel())) {
            try {
            	saveShutdownTime();
    		    // see http://stackoverflow.com/a/2153270
    		    String shutdownCommand;
    		    String osName = System.getProperty("os.name");        
    		    if (osName.startsWith("Win")) {
    		    	shutdownCommand = System.getenv("windir") + File.separator + "system32" + File.separator + "shutdown.exe -i";
    		    } else if (osName.startsWith("Linux") || osName.startsWith("Mac")) {
    		    	shutdownCommand = "shutdown -h now";
    		    } else {
    		        throw new RuntimeException("Unsupported operating system.");
    		    }
    		    Process process = Runtime.getRuntime().exec(shutdownCommand);
	            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
	            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

	            // Read command standard output
	            String s;
	            while ((s = stdInput.readLine()) != null) {
	            	trayIcon.displayMessage(comment, s, TrayIcon.MessageType.ERROR);
	            }

	            // Read command errors
	            while ((s = stdError.readLine()) != null) {
	            	trayIcon.displayMessage(comment, s, TrayIcon.MessageType.ERROR);
	            }
	            System.exit(0);
			} catch (IOException e1) {
				helpAndTerminate(e1.getMessage());
			}
        }
        if (EXIT.equals(source.getLabel())) {
            saveShutdownTime();
        	System.exit(0);
        }
    }

    private void saveShutdownTime() {
		try {
		    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmp)));
		    out.write(formatter.format(System.currentTimeMillis()) + System.getProperty("line.separator"));
		    out.close();
		} catch (IOException e) {
		    helpAndTerminate(e.getMessage());
		}
	}

	class SaveThread extends Thread {

        SaveThread() {
        }

        public void run() {
            saveShutdownTime();
        }
    }
}
