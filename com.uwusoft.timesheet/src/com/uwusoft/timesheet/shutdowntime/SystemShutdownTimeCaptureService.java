package com.uwusoft.timesheet.shutdowntime;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private static File tmp;
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

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        String prefsPath = System.getProperty("user.home");
        if (args.length > 0) prefsPath = args[0];
        String dir;
        if (args.length > 1) {
        	dir = args[0];
        	prefsPath = args[0] + args[1];
        }
        else
        	dir = prefsPath.substring(0, prefsPath.lastIndexOf("/"));
		
        RandomAccessFile lockFile = new RandomAccessFile(new File(dir + "/shutdownTime.lck"), "rw");
        FileChannel channel = lockFile.getChannel();
        FileLock lock = channel.tryLock();
        if (lock == null) {
            System.exit(0);
        }		
        tmp = new File(dir + "/" + tmpFile);
        
		SystemShutdownTimeCaptureService systemTimeCapture = new SystemShutdownTimeCaptureService();
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
