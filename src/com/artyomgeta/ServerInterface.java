package com.artyomgeta;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ServerInterface extends JFrame implements Runnable {
    private JPanel panel1;
    JTextField commandField;
    private JButton enterButton;
    private JTabbedPane tabbedPane1;
    private JEditorPane console;
    private JList<String> usersList;
    private JTabbedPane tabbedPane2;
    private JList list1;
    private JList list2;
    private JEditorPane editorPane2;
    private JEditorPane editorPane1;
    private JButton closeButton;
    private JButton writeButton;
    private JButton deniedButton;
    private JScrollPane consoleScroll;
    private JLabel connectedUsersLabels;
    private JLabel onlineLabel;
    private JList<String> dialogsList;
    private JTextField dielogField;
    private JButton sendButton;
    private JEditorPane dialogPane;
    private JPanel activeRight;
    private final StringBuilder consoleStack = new StringBuilder();
    boolean isOnline = false;
    final JPopupMenu popupMenu = new JPopupMenu();
    final JMenuItem infoMenu = new JMenuItem("Information");
    final JMenuItem messageMenu = new JMenuItem("Message");
    final JMenuItem kickMenu = new JMenuItem("Kick");
    final JMenuItem banMenu = new JMenu("Ban");
    final JMenuItem banForeverItem = new JMenuItem("Forever");
    final JMenuItem banDateItem = new JMenuItem("For...");
    int selectedUser = -1;
    int selectedDialog = 0;


    @Override

    public void run() {
        setTitle("GUI");
        setSize(new Dimension(640, 480));
        setContentPane(panel1);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        this.getRootPane().setDefaultButton(enterButton);
        commandField.grabFocus();
        enterButton.addActionListener(e -> {
            RunnableClass.workingCommand = true;
            //emulateCommand(commandField.getText());
            commandField.setText("");
        });

        final int[] time = {0};
        Timer timer = new Timer(1000, e -> {
            updateUsers(RunnableClass.usersList);
            connectedUsersLabels.setText(RunnableClass.usersList.size() + "");
            onlineLabel.setText(time[0] + " seconds");
            if (isOnline)
                time[0]++;
        });
        timer.start();
        timer.setRepeats(true);

        popupMenu.add(infoMenu);
        popupMenu.add(kickMenu);
        popupMenu.add(banMenu);
        popupMenu.add(messageMenu);
        banMenu.add(banDateItem);
        banMenu.add(banForeverItem);
        usersList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    if (usersList.getSelectedIndex() != -1) {
                        popupMenu.setBorder(BorderFactory.createTitledBorder(parseListElement(0, usersList.getSelectedValue())));
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                } else {
                    usersList.setSelectedIndex(usersList.locationToIndex(e.getPoint()));
                    selectedUser = usersList.getSelectedIndex();
                }
            }
        });

        dialogsList.setSelectedIndex(0);
        dialogsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedDialog = dialogsList.locationToIndex(e.getPoint());
                dialogPane.setText(RunnableClass.returnDialog(selectedDialog, -1));
            }
        });
        messageMenu.addActionListener(e -> {
            String result = JOptionPane.showInputDialog(this, "What do you want to write?", usersList.getSelectedValue(), JOptionPane.QUESTION_MESSAGE);
            if (result != null) {
                if (!result.equals("")) {
                    RunnableClass.command("write " + parseListElement(0, usersList.getSelectedValue()) + " " + result + " Server");
                    JOptionPane.showMessageDialog(this, "Message \"" + result + "\" has arrived to user " + parseListElement(0, usersList.getSelectedValue()));
                } else JOptionPane.showMessageDialog(this, "Enter something", "Error", JOptionPane.ERROR_MESSAGE);
            } else JOptionPane.showMessageDialog(this, "Enter something", "Error", JOptionPane.ERROR_MESSAGE);
        });
        infoMenu.addActionListener(e -> JOptionPane.showMessageDialog(this, "Socket: " + RunnableClass.getClientBy(0, parseListElement(0, usersList.getSelectedValue())).getSocketName() + "\nID: " + RunnableClass.getClientBy(0, parseListElement(0, usersList.getSelectedValue())).getUserId(), "Ifnfo", JOptionPane.PLAIN_MESSAGE));
        kickMenu.addActionListener(e -> {
            String result;
            result = JOptionPane.showInputDialog(this, "Enter the reason to kick: \n\t1 - Violation of the rules\n\t2 - Without reason\n\t10 - Engineering works\n\nOr input \"0\", if you don't want to kick user now.", "Input", JOptionPane.WARNING_MESSAGE);
            try {
                if (Integer.parseInt(result) == 1 || Integer.parseInt(result) == 2 || Integer.parseInt(result) == 10) {
                    RunnableClass.getClientBy(0, parseListElement(0, usersList.getSelectedValue())).kickMe(Integer.parseInt(result));
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid value", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e1) {
                JOptionPane.showMessageDialog(this, "Invalid value", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

    }

    public void updateUsers(List<ServerThread> list) {
        String[] users = new String[list.size()];
        DefaultListModel<String> usersModel = new DefaultListModel<>();
        DefaultListModel<String> dialogsModel = new DefaultListModel<>();
        for (int i = 0; i < users.length; i++) {
            usersModel.addElement(list.get(i).getSocketName() + " : " + list.get(i).getUserId() + " : " + list.get(i).getRole());
        }
        usersList.setModel(usersModel);
        dialogsModel.addAll(Arrays.asList(RunnableClass.returnDialogs()));
        usersList.setSelectedIndex(selectedUser);
        dialogsList.setModel(dialogsModel);
        dialogsList.setSelectedIndex(selectedDialog);
    }

    public void printToConsole(String message) {
        consoleStack.append(message).append("\n");
        console.setText(consoleStack.toString());
        consoleScroll.getVerticalScrollBar().setValue(consoleScroll.getVerticalScrollBar().getMaximum());
        if (message.equals("Listening")) isOnline = true;
        else if (message.equals("Stopped")) isOnline = false;
    }

    private String parseListElement(int todo, String element) {
        String[] parser = element.split(" : ");
        String address = parser[0];
        String id = parser[1];
        switch (todo) {
            case 0:
                return id;
        }
        return "Error";
    }
}
