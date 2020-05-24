package com.artyomgeta;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.InputMismatchException;

@SuppressWarnings("SwitchStatementWithoutDefaultBranch")
public class ServerThread extends Thread {
    private final Socket socket;
    InputStream input;
    BufferedReader reader;
    OutputStream output;
    PrintWriter writer;
    private int id = 0;
    private static final int RIGHTS_USER = 0;
    private static final int RIGHTS_ADMIN = 1;
    int rights = RIGHTS_USER;
    int userId;
    GUI gui;

    public ServerThread(Socket socket, int id, GUI gui) {
        this.socket = socket;
        this.id = id;
        this.gui = gui;
    }

    @SuppressWarnings("EnhancedSwitchMigration")
    @Override
    public void run() {
        try {
            try {
                input = socket.getInputStream();
                reader = new BufferedReader(new InputStreamReader(input));
                output = socket.getOutputStream();
                writer = new PrintWriter(output, true);
                String text;
                authorisation:
                while (true) {
                    writer.println("Enter you id, if you don't know your id, enter \"0\"");
                    text = reader.readLine();
                    try {
                        userId = Integer.parseInt(text);
                    } catch (NumberFormatException e) {
                        writer.println("Enter an integer");
                        break;
                    }
                    if (userId != 0) {
                        // noinspection ConstantConditions,LoopStatementThatDoesntLoop
                        for (int i = 0; i < RunnableClass.adminIDs.length; //noinspection UnusedAssignment
                             i++) {
                            if (userId == RunnableClass.adminIDs[i]) {
                                rights = RIGHTS_ADMIN;
                                System.out.println(socket.getInetAddress().getHostName() + " registered as admin");
                                gui.printToConsole(socket.getInetAddress().getHostName() + " registered as admin");
                                writer.println("You have authorized as admin.\nYour available commands:\n\t- exit\n\t- users\n\t- report");
                                RunnableClass.authorizedAdmins.add(this);
                                RunnableClass.usedAdminsIDs.add(userId);
                                break authorisation;
                            } else {
                                writer.println("Your id is invalid. Try again.");
                                break;
                            }
                        }
                    } else {
                        writer.println("You have authorized as client.");
                        writer.println("Your available commands:\n\t- exit\n\t- report\n\t- users");
                        break;
                    }
                }
                do {
                    writer.println("Enter command: ");
                    try {
                        String result = reader.readLine();
                        if (socket.getInetAddress().isReachable(100)) {
                            switch (result) {
                                case "users":
                                        if (RunnableClass.usersList.size() > 0) {
                                            int length = 0;
                                            boolean admin = false;
                                            for (ServerThread thread : RunnableClass.usersList) {
                                                for (int j = 0; j < RunnableClass.authorizedAdmins.size(); j++) {
                                                    if (thread.getSocketName().equals(RunnableClass.authorizedAdmins.get(j).getSocketName()))
                                                        admin = true;
                                                }
                                                writer.println("\t" + length + ": " + thread.getSocketName() + (admin ? " (Administrator)" : ""));
                                                length++;
                                            }
                                            writer.println("\tChoose user");
                                            int selectedUser;
                                            try {
                                                selectedUser = Integer.parseInt(reader.readLine());
                                            } catch (InputMismatchException e) {
                                                writer.println("Is not an integer");
                                                break;
                                            }
                                            if (selectedUser <= RunnableClass.usersList.size() && selectedUser >= 0) {
                                                writer.println("\tYou choose user " + selectedUser + ", what do you want to do?");
                                                if (rights == RIGHTS_ADMIN)
                                                writer.println("\tAvailable commands:\n\t\t- info\n\t\t- kick\n\t\t- message");
                                                else writer.println("\tAvailable commands:\n\t\t- message");
                                                String todo = reader.readLine();
                                                //Работа с пользователем
                                                switch (todo) {
                                                    case "info":
                                                        writer.println("\tUser's info: \n\t" + RunnableClass.usersList.get(selectedUser).getSocketName() + "\n\tId: " + getUserId());
                                                        break;
                                                    case "help":
                                                        writer.println("\tCommands: ");
                                                        writer.println("\t- kick");
                                                        writer.println("\t- info");
                                                        writer.println("\t- message");
                                                        break;
                                                    case "kick":
                                                        writer.println("Enter the reason to kick: \n\t- 0 (Without reason)\n\t - 1 (Violation of the rules)");
                                                        int result1 = Integer.parseInt(reader.readLine());
                                                        if (result1 == 0 || result1 == 1) {
                                                            writer.println(RunnableClass.command("kick " + selectedUser + " " + result1 + " " + getSocketName()));
                                                        } else {
                                                            writer.println("Invalid value");
                                                        }
                                                        break;
                                                    case "message":
                                                        writer.println("Enter the message: ");
                                                        String result2 = reader.readLine();
                                                        writer.println(RunnableClass.command("write " + selectedUser + " " + result2.replace(" ", "%32") + " " + getSocketName()));
                                                    default:
                                                        writer.println("Unexpected command");
                                                        break;
                                                }

                                            } else {
                                                writer.println("Inappropriate number");
                                                break;
                                            }
                                        } else {
                                            writer.println("No users connected");
                                            break;
                                        }
                                    break;
                                case "report":
                                    writer.println("Write your message: ");
                                    String result1 = reader.readLine();
                                    writer.println(RunnableClass.writeToServer(socket.getInetAddress().getHostAddress(), result1));
                                    break;
                                case "exit":
                                    kickMe(0);
                                    break;
                                default:
                                    writer.write("Unexpected command");
                                    break;
                            }
                        } else {
                            if (rights == RIGHTS_ADMIN) RunnableClass.authorizedAdmins.remove(this);
                        }
                    } catch (IOException e) {
                        kickMe(0);
                    }
                } while (!text.equals("exit"));
                if (socket.getInetAddress().isReachable(100)) kickMe(0);
                else interrupt();
            /*System.out.println("Disconnected: " + socket.getInetAddress().getHostName());
            MultiThreadServer.list.remove(MultiThreadServer.list.size() - 1);*/
                //sRunnableClass.usersList.remove(id);
            } catch (IOException ex) {
                System.out.println("Server exception: " + ex.getMessage());
                gui.printToConsole("Server exception: " + ex.getMessage());
                ex.printStackTrace();
            }
        } catch (NullPointerException | IndexOutOfBoundsException exception) {
            if (rights == RIGHTS_ADMIN) {
                RunnableClass.usedAdminsIDs.remove(id);
                RunnableClass.authorizedAdmins.remove(this);
            }
            RunnableClass.usersList.remove(this);
            kickMe(0);
        }
    }

    public void printMessage(String message) {
        writer.println(message);
    }

    public String getSocketName() {
        return socket.getInetAddress().getHostName();
    }

    public void kickMe(int cause) {
        try {
            writer.println("You have benn kicked from server. Cause: " + cause);
            gui.printToConsole("Disconnected: " + socket.getInetAddress().getHostName());
            System.out.println("Disconnected: " + socket.getInetAddress().getHostName());
            RunnableClass.usersList.remove(this);
            writer.flush();
            socket.close();
            input.close();
            output.close();
            writer.close();
            reader.close();
            interrupt();
            stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getUserId() {
        return id;
    }

    public boolean isAdmin() {
        return rights == RIGHTS_ADMIN;
    }

}