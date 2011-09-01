package com.uwusoft.timesheet.extensionpoint.model;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

import com.uwusoft.timesheet.extensionpoint.StorageService;

/**
 * the time management app
 *
 * @author Uta Wunderlich
 * @version $Revision: $, $Date: Aug 12, 2011
 * @since Aug 12, 2011
 */
public class TimeSheet implements ActionListener {
    private static StorageService storageService;
    private static SimpleDateFormat formatter;
    private static TrayIcon trayIcon = null;
    private static File props;
    private static Properties transferProps;
    private static String comment = "Timesheet Properties";

    public TimeSheet() {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage(TimeSheet.class.getResource("clock.png") == null ? null : TimeSheet.class.getResource("clock.png").getFile());

            PopupMenu popup = new PopupMenu();
            addToPopupMenu(popup, new MenuItem("Change Task"));
            addToPopupMenu(popup, new MenuItem("Submit"));
            addToPopupMenu(popup, new MenuItem("Exit"));

            trayIcon = new TrayIcon(image, "TimeSheet", popup);
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

    private void addToPopupMenu(PopupMenu popup, MenuItem menuItem) {
        menuItem.addActionListener(this);
        popup.add(menuItem);
    }

    public static void main(String[] args) {
        TimeSheet timeSheet = new TimeSheet();

        formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        RuntimeMXBean mx = ManagementFactory.getRuntimeMXBean();
        String startTime = formatter.format(mx.getStartTime());

        transferProps = new Properties();
        try {
            props = new File("TimeSheet.properties");
            if (!props.exists()) props.createNewFile();

            loadProperties();

            String shutdownTime = formatter.format(formatter.parse(transferProps.getProperty("system.shutdown")));

            storeProperty("system.start", startTime);

            //storageService = new StorageServiceImpl(transferProps.getProperty("spreadsheet.key")); // todo get properties of special storage service

            Calendar calDay = Calendar.getInstance();
            Calendar calWeek = new GregorianCalendar();
            int shutdownDay = 0;
            int shutdownWeek = 0;
            // automatic check out
            if (transferProps.getProperty("task.last") != null) { // last task will be set to null if a manual check out has occurred
                Date shutdownDate = formatter.parse(shutdownTime);
                calDay.setTime(shutdownDate);
                shutdownDay = calDay.get(Calendar.DAY_OF_YEAR);
                calWeek.setTime(shutdownDate);
                shutdownWeek = calWeek.get(Calendar.WEEK_OF_YEAR);
                storageService.storeTimeEntry(shutdownDate, transferProps.getProperty("task.last"));
                storageService.storeLastDailyTotal();
                if (transferProps.getProperty("task.daily") != null)
                    storageService.storeTimeEntry(shutdownDate, transferProps.getProperty("task.daily"), transferProps.getProperty("task.daily.total"));
            }
            // automatic check in
            Date startDate = formatter.parse(startTime);
            calDay.setTime(startDate);
            calWeek.setTime(startDate);
            int startDay = calDay.get(Calendar.DAY_OF_YEAR);
            int startWeek = calWeek.get(Calendar.WEEK_OF_YEAR);
            if (startDay != shutdownDay) { // don't automatically check in/out if computer is rebootet
                if (startWeek != shutdownWeek) storageService.storeLastWeekTotal(); //  store Week and Overtime
                storageService.storeTimeEntry(startDate, StorageService.CHECK_IN);
                storeProperty("task.last", transferProps.getProperty("task.default"));
            }
            displayMessage("System Shutdown/Start", shutdownTime + System.getProperty("line.separator") + startTime);

            timeSheet.addShutdownHook();
        }
        catch (java.io.IOException e) {
            helpAndTerminate(e.getMessage());
        } catch (ParseException e) {
            helpAndTerminate(e.getMessage());
		}
    }

    private static void loadProperties() throws IOException {
        InputStream in = new FileInputStream(props);
        transferProps.load(in);
        in.close();
    }

    private static void storeProperty(String key, String value) throws IOException {
        OutputStream out = new FileOutputStream(props);
        transferProps.setProperty(key, value);
        transferProps.store(out, comment);
        out.close();
    }

    private static void helpAndTerminate(String message) {
        displayError(message);
        System.exit(1);
    }

    private static void displayMessage(String caption, String message) {
        if (trayIcon != null)
            trayIcon.displayMessage(caption, message, TrayIcon.MessageType.INFO);
    }

    private static void displayError(String message) {
        if (trayIcon != null)
            trayIcon.displayMessage("Error", message, TrayIcon.MessageType.ERROR);
        System.err.println(message);
    }

    public void actionPerformed(ActionEvent e) {
        MenuItem source = (MenuItem) e.getSource();
        try {
            if ("Exit".equals(source.getLabel())) System.exit(0);
            else if ("Change Task".equals(source.getLabel())) {
                String selectedTask = TaskListDialog.showDialog(
                        "Tasks for Primavera:",
                        "Task Chooser",
                        storageService.getTasks().get("Primavera").toArray(new String[storageService.getTasks().get("Primavera").size()]), // todo
                        transferProps.getProperty("task.default"));
                Date now = new Date();
                storageService.storeTimeEntry(now, transferProps.getProperty("task.last"));
                storeProperty("task.last", selectedTask);
            } else if ("Submit".equals(source.getLabel())) {
                storageService.submitEntries();
            }
        } catch (IOException e1) {
            helpAndTerminate(e1.getMessage());
        }
    }

    class SaveThread extends Thread {

        SaveThread() {
        }

        public void run() {
            try {
                loadProperties();
                storeProperty("system.shutdown", formatter.format(System.currentTimeMillis()));
            } catch (FileNotFoundException e) {
                helpAndTerminate(e.getMessage());
            } catch (IOException e) {
                helpAndTerminate(e.getMessage());
            }
        }
    }
}
