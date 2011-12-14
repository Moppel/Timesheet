package com.uwusoft.timesheet;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * the system shutdown time capture service
 *
 * @author Uta Wunderlich
 * @version $Revision: $, $Date: Aug 12, 2011
 * @since Aug 12, 2011
 */
public class SystemShutdownTimeCaptureService implements ActionListener {
    private static TrayIcon trayIcon;
    private static SimpleDateFormat formatter;
    private static File props, tmp;
    private static Properties transferProps;
    private static String comment = "System Shutdown Time Capture Service";

    public SystemShutdownTimeCaptureService() {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("Services.png"));

            PopupMenu popup = new PopupMenu();
            MenuItem menuItem = new MenuItem("Exit");
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

    public static void main(String[] args) {
		RuntimeMXBean mx = ManagementFactory.getRuntimeMXBean();
		Date startDate = new Date(mx.getStartTime());

		SystemShutdownTimeCaptureService systemTimeCapture = new SystemShutdownTimeCaptureService();

        formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");

        transferProps = new Properties();
        String prefsPath = "/.eclipse/com.uwusoft.timesheet/.metadata/.plugins/org.eclipse.core.runtime/.settings/com.uwusoft.timesheet.prefs";
        if (args.length > 0) prefsPath = args[0];
        props = new File(System.getProperty("user.home") + prefsPath);
        tmp = new File(System.getProperty("user.home") + "/shutdownTime.tmp");
		try {
			if (tmp.exists()) {
				BufferedReader time = new BufferedReader(new InputStreamReader(new FileInputStream(tmp)));				
				String line = time.readLine();
				time.close();
				if (line != null) {
					InputStream in = new FileInputStream(props);
					transferProps.load(in);
					in.close();

					OutputStream out = new FileOutputStream(props);
					transferProps.setProperty("system.shutdown", formatter.format(formatter.parse(line)));
					transferProps.setProperty("system.start", formatter.format(startDate));
					transferProps.store(out, comment);
					out.close();
				}
			} else
				tmp.createNewFile();
		} catch (IOException e) {
			helpAndTerminate(e.getMessage());
		} catch (ParseException e) {
			helpAndTerminate(e.getMessage());
		}
        
        systemTimeCapture.addShutdownHook();
    }

    private static void helpAndTerminate(String message) {
        trayIcon.displayMessage(comment, message, TrayIcon.MessageType.ERROR);
        System.exit(1);
    }

    public void actionPerformed(ActionEvent e) {
        MenuItem source = (MenuItem) e.getSource();
        if ("Exit".equals(source.getLabel())) {
            System.exit(0);
        }
    }

    class SaveThread extends Thread {

        SaveThread() {
        }

        public void run() {
            try {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmp)));
                out.write(formatter.format(System.currentTimeMillis()) + System.getProperty("line.separator"));
                out.close();
            } catch (IOException e) {
                helpAndTerminate(e.getMessage());
            }
        }
    }
}
