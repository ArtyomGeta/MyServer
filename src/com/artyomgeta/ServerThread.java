package com.artyomgeta;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

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

    public ServerThread(Socket socket, int id) {
        this.socket = socket;
        this.id = id;
    }

    @Override
    public void run() {
        try {
            input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
            output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
            String text = null;
            while (true) {
                writer.println("Enter you id, if you don't know your id, enter \"0\"");
                text = reader.readLine();
                int id;
                try {
                    id = Integer.parseInt(text);
                } catch (NumberFormatException e) {
                    writer.println("Enter an integer");
                    break;
                }
                if (id != 0) {
                    for (int i = 0; i < RunnableClass.adminIDs.length; i++) {
                        if (id == RunnableClass.adminIDs[i]) {
                            rights = RIGHTS_ADMIN;
                            System.out.println(socket.getInetAddress().getHostName() + " registered as admin");
                        } else {
                            writer.println("Your id is invalid. Try again.");
                        }
                        break;
                    }
                }
            }
            do {
                try {
                    writer.println("Прочитано");
                } catch (NullPointerException exception) {
                    socket.close();
                    System.out.println("Disconnected: " + socket.getInetAddress().getHostName());
                    RunnableClass.usersList.remove(RunnableClass.usersList.size() - 1);
                    break;
                }

            } while (!text.equals("kick"));
            System.out.println(socket.isConnected());
            if (socket.getInetAddress().isReachable(100)) kickMe(0);
            else interrupt();
            /*System.out.println("Disconnected: " + socket.getInetAddress().getHostName());
            MultiThreadServer.list.remove(MultiThreadServer.list.size() - 1);*/
            //sRunnableClass.usersList.remove(id);
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void printMessage(String message) {
        //System.out.println("Works");
        writer.println(message);
        writer.close();
    }

    public String getSocketName() {
        return socket.getInetAddress().getHostName();
    }

    public void kickMe(int cause) {
        try {
            writer.println("You have benn kicked from server. Cause: " + cause);
            System.out.println("Disconnected: " + socket.getInetAddress().getHostName());
            RunnableClass.usersList.remove(id);
            writer.flush();
            socket.close();
            input.close();
            output.close();
            writer.close();
            reader.close();
            stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}