package com.artyomgeta;

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class RunnableClass implements java.lang.Runnable {
    Socket csocket;
    public static List<ServerThread> usersList = new ArrayList<>();
    public static Random random = new Random();
    public static final int[] adminIDs = new int[] { 666, 777, 888 };

    RunnableClass(Socket csocket) {
        this.csocket = csocket;
        for (int i = 0; i < adminIDs.length; i++) {

        }
    }

    public static void main(String[] args) throws Exception {
        catchStartingCommands();
    }

    @Override
    public void run() {
        try {
            PrintStream pstream = new PrintStream(csocket.getOutputStream());
//            InputStream inputStream = csocket.getInputStream();
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(csocket.getInputStream()));
            InputStream input;
            //InputStreamReader reader = null;
            while (true) {
                try {
                    input = csocket.getInputStream();
                    //reader = new InputStreamReader(input);
                    //int character = reader.read();  // reads a single character
                    //if (character == -1) throw new SocketException("Socket is closed");
                    inFromClient = new BufferedReader(new InputStreamReader(csocket.getInputStream()));
                    if (inFromClient.readLine().equals("null")) throw new SocketException("Socket is closed");
                    System.out.println(inFromClient.readLine());
                } catch (SocketException | NullPointerException exception) {
                    csocket.close();
                    //inputStream.close();
                    pstream.close();
                    //assert reader != null;
                    //reader.close();
                    System.out.println("Client " + csocket.getLocalAddress().getHostName() + " has disconnected");
                    usersList.remove(usersList.size() - 1);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("EnhancedSwitchMigration")
    public static void catchStartingCommands() throws IOException {
        Scanner startedCommandScanner = new Scanner(System.in);
        final String COMMAND_HELP = "HELP";
        final String COMMAND_START = "START";
        String result = null;
        //Начинаем: спроашиваем команду
        do {
            System.out.println("Enter command: ");
            result = startedCommandScanner.nextLine();
            switch (result.toUpperCase()) {
                case COMMAND_HELP -> {
                    System.out.println("Commands: ");
                    System.out.println("\thelp");
                    System.out.println("\tstart");
                }
                case COMMAND_START -> {
                    ServerSocket ssock = new ServerSocket(1234);
                    AtomicReference<Robot> robot = null;
                    System.out.println("Listening");
                    new Thread(() -> {
                        System.out.println("Enter command: ");
                        String command;
                        while (true) {
                            command = new Scanner(System.in).nextLine();
                            switch (command) {
                                case "list":
                                    System.out.println("\tConnected users: " + usersList.size());
                                    break;
                                case "users":
                                    if (usersList.size() > 0) {
                                        int length = 0;
                                        for (ServerThread thread : usersList) {
                                            System.out.println("\t" + length + ": " + thread.getSocketName());
                                            length++;
                                        }
                                        System.out.println("\tChoose user");
                                        int selectedUser;
                                        try {
                                            selectedUser = new Scanner(System.in).nextInt();
                                        } catch (InputMismatchException e) {
                                            System.out.println("Is not an integer");
                                            break;
                                        }
                                        if (selectedUser <= usersList.size() && selectedUser >= 0) {
                                            System.out.println("\tYou choose user " + selectedUser + ", what do you want to do?");
                                            System.out.println("\tAvailable commands:\n\t\t- info\n\t\t- kick");
                                            String todo = new Scanner(System.in).nextLine();
                                            //Работа с пользователем
                                            switch (todo) {
                                                case "info":
                                                    System.out.println("\tUser's info: " + usersList.get(selectedUser).getSocketName());
                                                    break;
                                                case "kick":
                                                    usersList.get(selectedUser).kickMe(0);
                                                    break;
                                                case "help":
                                                    System.out.println("\tCommands: ");
                                                    System.out.println("\tkick");
                                                    System.out.println("\tinfo");
                                            }
                                        } else {
                                            System.out.println("Inappropriate number");
                                            break;
                                        }
                                        break;
                                    } else {
                                        System.out.println("No users connected");
                                        break;
                                    }
                                case "stop":
                                    if (usersList.size() > 0) {
                                        for (ServerThread thread : usersList) {
                                            thread.kickMe(10);
                                        }
                                    }
                                    System.out.println("Server stopped");
                                    System.exit(1);
                                    break;
                                case "help":
                                    System.out.println("\tCommands: ");
                                    System.out.println("\t\tlist - list of connected users");
                                    System.out.println("\t\tstop - kick all users and stop server");
                                    System.out.println("\t\tusers - manipulate the users");
                                    break;
                                default:
                                    System.out.println("Unexpected command");
                                    break;
                            }
                        }
                    }).start();
                    BufferedReader inFromClient;
                    DataOutputStream outToClient;
                    String clientSentence;
                    String capitalizedSentence;
                    while (true) {
                        Socket sock = ssock.accept();
                        ServerThread serverThread = new ServerThread(sock, usersList.size());
                        System.out.println("Connected: " + sock.getInetAddress().getHostName());
                        inFromClient = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                        outToClient = new DataOutputStream(sock.getOutputStream());
//            clientSentence = inFromClient.readLine();
//            capitalizedSentence = clientSentence.toUpperCase() + '\n';
//            outToClient.writeBytes(capitalizedSentence);
//            System.out.println(sock.getInetAddress().toString() + ": " + clientSentence);
                        usersList.add(serverThread);
                        int user = usersList.size() - 1;
                        usersList.get(user).start();

                    }
                }
                default -> System.out.println("Unexpected command.");
            }
        } while (!result.equals("stop"));
    }

}