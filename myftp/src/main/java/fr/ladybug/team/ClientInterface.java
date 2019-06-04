package fr.ladybug.team;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;


public class ClientInterface {
    private static Scanner scanner = new Scanner(System.in);

    private static Socket server;
    private static OutputStream outputStream;
    private static InputStream inputStream;

    public static void main(String[] args) {
        printHelpMessage();

        var serverAddress = askAddress();
        Client client = null;
        try {
            client = new Client(serverAddress);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        while (true) {
            String[] command = askCommand().split("\\s+");
            if (command[0].equals("exit")) {
                try {
                    client.shutdown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }



            byte[] response = new byte[0];
            try {
                response = client.makeQuery(new Query(getIdByName(command[0]), Arrays.copyOfRange(command, 1, command.length)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            switch (command[0]) {
                case "list": {
                    try {
                        ResponseList responseList = ResponseList.fromBytes(response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                case "get": {
                    try {
                        ResponseGet responseGet = ResponseGet.fromBytes(response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                default: {
                    throw new RuntimeException("Internal consistency fail. Should not normally happen");
                }
            }
        }

        System.out.println("Goodbye!");
    }

    private static int getIdByName(String name) {
        switch (name) {
            case "list":
                return 1;
            case "get":
                return 2;
            default:
                return -1;
        }
    }

    private static @NotNull String askCommand() {
        return scanner.nextLine();
    }

    private static String askAddress() {
        return "192.168.1.112";
    }

    private static void printHelpMessage() {
    }


}