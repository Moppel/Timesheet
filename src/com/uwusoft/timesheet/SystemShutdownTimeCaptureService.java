package com.uwusoft.timesheet;

import java.io.*;
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
    private static File props;
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
        SystemShutdownTimeCaptureService systemTimeCapture = new SystemShutdownTimeCaptureService();

        formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");

        transferProps = new Properties();
        String prefsPath = "/.eclipse/com.uwusoft.timesheet/.metadata/.plugins/org.eclipse.core.runtime/.settings/com.uwusoft.timesheet.prefs";
        if (args.length > 0) prefsPath = args[0];
        props = new File(System.getProperty("user.home") + prefsPath);
        
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
                InputStream in = new FileInputStream(props);
                transferProps.load(in);
                in.close();

                OutputStream out = new FileOutputStream(props);
                transferProps.setProperty("system.shutdown", formatter.format(System.currentTimeMillis()));
                transferProps.store(out, comment);
                out.close();           
            } catch (IOException e) {
                helpAndTerminate(e.getMessage());
            }
        }
    }
}
