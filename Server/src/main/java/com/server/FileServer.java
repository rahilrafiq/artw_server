package com.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

public class FileServer {

    private static String DIRECTORY_NAME = "";
    private static int SERVER_PORT;

    public static void main(String[] args) {
        if (args != null && args.length > 0) {
            setProperties(args[0]);
        } else {
            System.out.println("Argument not provided for config file! Please provide " +
                    "config file name in the argument and rerun the Client program.");
            System.exit(1);
        }

        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Server started and waiting for connections...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                // Handle client communication in a separate thread
                Thread clientHandler = new Thread(new ClientHandler(clientSocket, DIRECTORY_NAME));
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setProperties(String configFile) {
        Properties properties = new Properties();
        String fileName = configFile;
        try {
            FileInputStream fileInputStream = new FileInputStream(fileName);
            properties.load(fileInputStream);
            DIRECTORY_NAME = properties.getProperty("app.directory");
            SERVER_PORT = Integer.parseInt(properties.getProperty("app.port"));
            fileInputStream.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            System.exit(1);
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private String DIRECTORY_NAME;

    public ClientHandler(Socket clientSocket, String DIRECTORY_NAME) {
        this.clientSocket = clientSocket;
        this.DIRECTORY_NAME = DIRECTORY_NAME;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
            objectOutputStream.writeObject("Connected with Server...");
            objectOutputStream.flush();

            writeToFile(objectInputStream, DIRECTORY_NAME);
            objectOutputStream.writeObject("File written for " + Thread.currentThread().getName());
            objectOutputStream.flush();

            System.out.println("Map from Client saved to file... ");

            objectInputStream.close();
            objectOutputStream.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeToFile(ObjectInputStream mapInputStream, String DIRECTORY_NAME) throws IOException {
        try {
            Date dateTime = new Date();
            File file = new File(DIRECTORY_NAME + "\\File_" + dateTime.getTime() + ".properties");
            Properties properties = new Properties();

            Map<String, String> dataMap = (Map) mapInputStream.readObject();

            for (Map.Entry<String, String> entry : dataMap.entrySet()) {
                properties.setProperty(entry.getKey(), entry.getValue());
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            properties.store(fileOutputStream, "Properties");
            fileOutputStream.close();
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

}