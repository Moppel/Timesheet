/* Copyright (c) 2005 by net-linx; All rights reserved */
package com.uwusoft.timesheet;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.*;

/**
 * todo: add class doc
 *
 * @author Uta Wunderlich
 * @version $Revision: $, $Date: Aug 17, 2011
 * @since Aug 17, 2011
 */
public class TaskListDialog extends JDialog implements ActionListener {
    private static TaskListDialog dialog;
    private static String value = "";
    private JList list;

    public static String showDialog(String labelText,
                                    String title,
                                    String[] possibleValues,
                                    String initialValue) {
        Frame frame = JOptionPane.getRootFrame();
        dialog = new TaskListDialog(frame,
                                labelText,
                                title,
                                possibleValues,
                                initialValue);
        dialog.setVisible(true);
        return value;
    }

    private void setValue(String newValue) {
        value = newValue;
        list.setSelectedValue(value, true);
    }

    private TaskListDialog(Frame frame,
                       String labelText,
                       String title,
                       Object[] data,
                       String initialValue) {
        super(frame, title, true);

        //Create and initialize the buttons.
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        final JButton setButton = new JButton("Set");
        setButton.setActionCommand("Set");
        setButton.addActionListener(this);
        getRootPane().setDefaultButton(setButton);

        list = new JList(data);
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        list.setVisibleRowCount(-1);
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    setButton.doClick(); //emulate button click
                }
            }
        });
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(400, 200));
        listScroller.setAlignmentX(LEFT_ALIGNMENT);
        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        JLabel label = new JLabel(labelText);
        label.setLabelFor(list);
        listPane.add(label);
        listPane.add(Box.createRigidArea(new Dimension(0,5)));
        listPane.add(listScroller);
        listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        //Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(cancelButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(setButton);

        //Put everything together, using the content pane's BorderLayout.
        Container contentPane = getContentPane();
        contentPane.add(listPane, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.PAGE_END);

        //Initialize values.
        setValue(initialValue);
        pack();
    }

    public void actionPerformed(ActionEvent e) {
        if ("Set".equals(e.getActionCommand())) {
            TaskListDialog.value = (String)(list.getSelectedValue());
        }
        TaskListDialog.dialog.setVisible(false);
    }
}
