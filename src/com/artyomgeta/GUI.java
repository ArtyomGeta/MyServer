package com.artyomgeta;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

public class GUI extends JFrame implements Runnable {
    private JPanel panel1;
    JTextField commandField;
    private JButton enterButton;
    private JTabbedPane tabbedPane1;
    private JEditorPane console;
    private JList<String> usersList;
    private StringBuilder consoleStack = new StringBuilder();

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
    }

    public void updateUsers(List<ServerThread> list) {
        String[] users = new String[list.size()];
        DefaultListModel<String> defaultListModel = new DefaultListModel<>();
        for (int i = 0; i < users.length; i++) {
            defaultListModel.addElement(list.get(i).getSocketName() + " : " + list.get(i).getUserId() + (list.get(i).isAdmin() ? " (Administrator) " : null));
        }
        usersList.setModel(defaultListModel);
    }

    public void printToConsole(String message) {
        consoleStack.append(message).append("\n");
        console.setText(consoleStack.toString());
    }

    public void emulateCommand(String command) {
        InputStream inputStream = System.in;
        try {
            System.setIn(new ByteArrayInputStream(command.getBytes()));
            Scanner scanner = new Scanner(System.in);
            System.out.println(scanner.nextLine());
        } finally {
            System.setIn(inputStream);
        }
    }

}
