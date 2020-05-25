package com.artyomgeta;

import org.json.JSONArray;
import org.json.JSONException;

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
    public static List<ServerThread> authorizedAdmins = new ArrayList<>();
    public static List<String> authorizedClients = new ArrayList<>();
    public static List<Integer> usedAdminsIDs = new ArrayList<>();
    public static Random random = new Random();
    public static ServerInterface serverInterface = new ServerInterface();
    public static final int[] adminIDs = new int[]{666, 777, 888};
    public static String input = "console";
    public static boolean workingCommand = false;

    private static final FileWriter[] fileWriters = new FileWriter[returnAllUsers()];

    public static final int WAITING_FOR_START_OR_HELP = 0;
    public static final int WAITING_FOR_MAJOR = 1;
    public static final int WAITING_FOR_USER_ID = 2;
    public static final int WAITING_FOR_USER_COMMANDS = 3;

    public static int serverState = WAITING_FOR_START_OR_HELP;

    RunnableClass(Socket csocket) {
        this.csocket = csocket;
    }

    public static void main(String[] args) throws Exception {
        input = args[0];
        System.out.println(input);
        serverInterface.run();
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
                    serverInterface.printToConsole("Client " + csocket.getLocalAddress().getHostName() + " has disconnected");
                    usersList.remove(usersList.size() - 1);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processHelp() {
        System.out.println("Commands: ");
        System.out.println("\thelp");
        System.out.println("\tstart");
        serverInterface.printToConsole("Commands: ");
        serverInterface.printToConsole("\t- help");
        serverInterface.printToConsole("\t- start");
    }

    private static void processUsers() {
        if (usersList.size() > 0) {
            int length = 0;
            for (ServerThread thread : usersList) {
                boolean admin = false;
                for (int j = 0; j < RunnableClass.authorizedAdmins.size(); j++) {
                    if (thread.getSocketName().equals(RunnableClass.authorizedAdmins.get(j).getSocketName()))
                        admin = true;
                }
                System.out.println("\t" + length + ": " + thread.getSocketName() + "(" + thread.getRole() + ")");
                serverInterface.printToConsole("\t" + length + ": " + thread.getSocketName() + "(" + thread.getRole() + ")");
                length++;
            }
            System.out.println("\tChoose user");
            serverInterface.printToConsole("\tChoose user");
            serverState = WAITING_FOR_USER_ID;
//            int selectedUser;
//            try {
//                selectedUser = new Scanner(System.in).nextInt();
//            } catch (InputMismatchException e) {
//                System.out.println("Is not an integer");
//                gui.printToConsole("Is not an integer");
//                return;
//            }
//            if (selectedUser <= usersList.size() && selectedUser >= 0) {
//                System.out.println("\tYou choose user " + selectedUser + ", what do you want to do?");
//                gui.printToConsole("\tYou choose user " + selectedUser + ", what do you want to do?");
//                System.out.println("\tAvailable commands:\n\t\t- info\n\t\t- kick");
//                gui.printToConsole("\tAvailable commands:\n\t\t- info\n\t\t- kick");
//                String todo = new Scanner(System.in).nextLine();
//                //Работа с пользователем
//                switch (todo) {
//                    case "info":
//                        System.out.println("\tUser's info: \n\tAddress: " + usersList.get(selectedUser).getSocketName() + "\n\tId: " + usersList.get(selectedUser).getUserId());
//                        gui.printToConsole("\tUser's info: \n\tAddress: " + usersList.get(selectedUser).getSocketName() + "\n\tId: " + usersList.get(selectedUser).getUserId());
//                        break;
//                    case "kick":
//                        usersList.get(selectedUser).kickMe(0);
//                        break;
//                    case "help":
//                        System.out.println("\tCommands: ");
//                        System.out.println("\t- kick");
//                        System.out.println("\t- info");
//                        System.out.println("\t- write");
//                        gui.printToConsole("Commands: ");
//                        gui.printToConsole("\t- kick");
//                        gui.printToConsole("\t- info");
//                        gui.printToConsole("\t- write");
//                    case "write":
//                        String result1;
//                        System.out.println("Enter the message");
//                        gui.printToConsole("Enter the message");
//                        result1 = new Scanner(System.in).nextLine();
//                        writeToClient(selectedUser, result1);
//                        break;
//                    default:
//                        System.out.println("Unexpected command");
//                        gui.printToConsole("Unexpected command");
//                        break;
//                }
//            } else {
//                System.out.println("Inappropriate number");
//                gui.printToConsole("Invalid value");
//                return;
//            }
        } else {
            System.out.println("No users connected");
            serverInterface.printToConsole("No users connected");
            serverState = WAITING_FOR_MAJOR;
        }
    }

    private static void processStart() throws IOException {
        serverState = WAITING_FOR_MAJOR;
        ServerSocket ssock = new ServerSocket(1234);
        AtomicReference<Robot> robot = null;
        System.out.println("Listening");
        serverInterface.printToConsole("Listening");
        BufferedReader inFromClient;
        DataOutputStream outToClient;
        String clientSentence;
        String capitalizedSentence;

        new Thread(() -> {
            try {
                catchStartingCommands();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        while (true) {
            Socket sock = ssock.accept();
            if (!inBanList(usersList.size())) {
                ServerThread serverThread = new ServerThread(sock, usersList.size(), serverInterface);
                System.out.println("Connected: " + sock.getInetAddress().getHostName());
                serverInterface.printToConsole("Connected: " + sock.getInetAddress().getHostName());
                inFromClient = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                outToClient = new DataOutputStream(sock.getOutputStream());
//            clientSentence = inFromClient.readLine();
//            capitalizedSentence = clientSentence.toUpperCase() + '\n';
//            outToClient.writeBytes(capitalizedSentence);
//            System.out.println(sock.getInetAddress().toString() + ": " + clientSentence);
                usersList.add(serverThread);
                int user = usersList.size() - 1;
                usersList.get(user).start();
                serverInterface.updateUsers(usersList);
            } else {
                new DataOutputStream(sock.getOutputStream()).writeBytes("You are banned.\n");
                sock.close();
            }
        }
    }

    public static void processStop() {
        for (ServerThread serverThread : usersList) {
            serverThread.kickMe(1);
        }
        serverInterface.printToConsole("Stopped");
    }

    private static void processUserId(int userId) {
        serverState = WAITING_FOR_USER_COMMANDS;
        serverInterface.printToConsole("Selected user: " + userId);
    }

    private static void processCommand(String command) throws IOException {
        final String COMMAND_HELP = "HELP";
        final String COMMAND_START = "START";
        if (serverState == WAITING_FOR_START_OR_HELP) {
            switch (command.toUpperCase()) {
                case COMMAND_HELP:
                    processHelp();
                    break;
                case COMMAND_START:
                    processStart();
                    break;
                default:
                    System.out.println("Unexpected command.");
                    serverInterface.printToConsole("Unexpected command");
            }
        } else if (serverState == WAITING_FOR_MAJOR) {
            switch (command.toUpperCase()) {
                case COMMAND_HELP:
                    processHelp();
                    break;
                case "USERS":
                    processUsers();
                    break;
                case "LIST":
                    serverInterface.printToConsole("List");
                    break;
                case "STOP":
                    serverInterface.printToConsole("Stop");
                    processStop();
                    break;
                default:
                    System.out.println("Unexpected command.");
                    serverInterface.printToConsole("Unexpected command");
            }
        } else if (serverState == WAITING_FOR_USER_ID) {
            try {
                processUserId(Integer.parseInt(command));
            } catch (NumberFormatException e) {
                serverInterface.printToConsole("Invalid value");
            }
        }

        switch (command.toUpperCase()) {
            case COMMAND_HELP:
                break;
            case COMMAND_START:
//                new Thread(() -> {
//                    Scanner scanner = new Scanner(System.in);
//                    System.out.println("Enter command: ");
//                    gui.printToConsole("Enter command");
//                    String command;
//                    while (true) {
//                        command = new Scanner(System.in).nextLine();
//                        switch (command) {
//                            case "list":
//                                System.out.println("Connected users: " + usersList.size());
//                                gui.printToConsole("tConnected users:" + usersList.size());
//                                break;
//                            case "users":
//                                break;
//                            case "stop":
//                                if (usersList.size() > 0) {
//                                    for (ServerThread thread : usersList) {
//                                        thread.kickMe(10);
//                                    }
//                                }
//                                System.out.println("Server stopped");
//                                gui.printToConsole("Server stopped");
//                                System.exit(1);
//                                break;
//                            case "help":
//                                System.out.println("\tCommands: ");
//                                System.out.println("\t\tlist - list of connected users");
//                                System.out.println("\t\tstop - kick all users and stop server");
//                                System.out.println("\t\tusers - manipulate the users");
//                                gui.printToConsole("Commands:");
//                                gui.printToConsole("\t- list");
//                                gui.printToConsole("\t- stop");
//                                gui.printToConsole("\t- users");
//                                break;
//                            case "admins":
//                                System.out.println("Authorized admins: ");
//                                gui.printToConsole("Authorized admins: ");
//                                int length = 0;
//                                for (ServerThread authorizedAdmin : authorizedAdmins) {
//                                    System.out.println(length + ": " + authorizedAdmin.getSocketName());
//                                    length++;
//                                }
//                                break;
//                            default:
//                                System.out.println("Unexpected command");
//                                gui.printToConsole("Unexpected command");
//                                break;
//                        }
//                    }
//                }).start();
        }
    }

    public static void catchStartingCommands() throws IOException {
        Scanner startedCommandScanner = new Scanner(System.in);
        String command = "";
        boolean showEnterCommand = true;
        //Начинаем: спроашиваем команду
        do {
            //System.out.println("Enter command: ");
            if (showEnterCommand) {
                serverInterface.printToConsole("Enter command: ");
                showEnterCommand = false;
            }
//            if (!stackToClient())
            //result = startedCommandScanner.nextLine();
            //else result = emulateCommand()

            if (workingCommand) {
                showEnterCommand = true;
                workingCommand = false;
                command = serverInterface.commandField.getText();
                processCommand(command);
            }
        } while (!command.toUpperCase().equals("EXIT"));
        System.exit(0);
    }

    public static String writeToServer(String address, String message) {
        serverInterface.printToConsole(address + ": " + message);
        System.out.println(address + ": " + message);
        return "Your message has arrived to server";
    }


    public static void writeToClient(int id, String message) {
        usersList.get(id).printMessage(message);
    }

    public static String command(String command) {
        String[] parser = command.split(" ");
        String todo = parser[0];
        String client = parser[1];
        String argument = parser[2];
        String author = parser[3];
        switch (todo) {
            case "kick":
                if (!author.equals(client)) {
                    for (ServerThread serverThread : authorizedAdmins) {
                        if (!client.equals(String.valueOf(serverThread.getUserId()))) {
                            new Thread(() -> usersList.get(Integer.parseInt(client)).kickMe(Integer.parseInt(argument))).start();
                            System.out.println("Admin " + client + " has kicked " + client + " with reason: " + argument);
                            serverInterface.printToConsole("Admin has kicked " + client + " with reason: " + argument);
                            return "User " + client + " has been kicked by " + author;
                        } else {
                            return "Error: you can't kick other admin";
                        }
                    }
                } else {
                    return "That's funny, but you can't kick yourself)";
                }
            case "write":
                writeToClient(Integer.parseInt(client), "Message from " + author + ": " + argument.replace("%32", " "));
                System.out.println("Admin " + author + " has write a message: \"" + argument.replace("%32", " ") + "\" to user " + usersList.get(Integer.parseInt(client)).getSocketName());
                serverInterface.printToConsole("Admin " + author + " has write a message: \"" + argument.replace("%32", " ") + "\" to user " + usersList.get(Integer.parseInt(client)).getSocketName());
                return "Your message has arrived";
            default:
                return "An error has occurred";
        }
    }

    public static boolean stackToClient() {
        return input.equals("client");
    }


    private static String emulateCommand(String command) {
        if (stackToClient()) {
            InputStream inputStream = System.in;
            Scanner scanner;
            try {
                System.setIn(new ByteArrayInputStream(command.getBytes()));
                scanner = new Scanner(System.in);
                System.out.println(scanner.nextLine());
            } finally {
                System.setIn(inputStream);
            }
            return scanner.nextLine();
        } else return null;
    }

    protected static ServerThread getClientBy(int by, String argument) {
        for (ServerThread serverThread : usersList) {
            switch (by) {
                case 0:
                    if (Integer.parseInt(argument) == serverThread.getUserId()) return serverThread;
                case 1:
                    if (argument.equals(serverThread.getSocketName())) return serverThread;
                default:
                    return null;
            }
        }
        return null;
    }

    public static String returnDialog(int user, int dialog) {
        StringBuilder returnable = new StringBuilder();
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Scanner myReader = new Scanner(new File("users/" + user + "/dialog.json"));
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                stringBuilder.append(data);
            }
            if (dialog == -1) dialog = 0;
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            JSONArray jsonArray = new JSONArray(stringBuilder.toString()).getJSONArray(dialog);
            for (int i = 0; i < jsonArray.length(); i++) {
                returnable.append(jsonArray.getJSONObject(i).getInt("author")).append(": ").append(jsonArray.getJSONObject(i).getString("text")).append("\n");
            }
        } catch (JSONException e) {
            e.printStackTrace();

        }
        return returnable.toString();
    }

    public static String[] returnDialogs() {
        String[] returnable = new String[returnAllUsers()];
        for (int i = 0; i < returnable.length; i++) {
            returnable[i] = new File("users/" + i).getName();
        }
        return returnable;
    }

    public static int returnAllUsers() {
        return Objects.requireNonNull(new File("users/").listFiles()).length;
    }

    public static boolean inBanList(int id) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean returnable = false;
        try {
            Scanner myReader = new Scanner(new File("users/banlist.json"));
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                stringBuilder.append(data);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            JSONArray jsonArray = new JSONArray(stringBuilder.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                returnable = id == jsonArray.getJSONObject(i).getInt("id");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return returnable;
    }

}