package chatroom;

import java.util.*;

// The Main class (file) to start an instance (server or client) by choice
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Loops until input is valid. Goal: Server or Client input
        while (true) {
            System.out.println("Start a server or a client instance? Please type either Server or Client.");
            String choice = scanner.nextLine().trim().toLowerCase(); // Lower case the choice string

            // Starts server or client class instance, if successful --> break the loop
            if (choice.equals("server")) {
                // Loops until input is valid. Goal: get port to connect to
                while (true) {
                    System.out.println("Enter the port number to connect to:");

                    try {
                        int port = Integer.parseInt(scanner.nextLine().trim());
                        Server server = new Server(port);
                        server.run();

                        // Checks if the server was successfully ran or not
                        if (server.success()) {
                            break;
                        }
                        System.out.println("Server failed to connect. Please try again.");
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid port format. Please try again.");
                    }
                }

                break;
            }

            else if (choice.equals("client")) {
                // Loops until valid input. Goal: get port to connect to
                while (true) {
                    System.out.println("Enter the port number to connect to:");

                    try {
                        int port = Integer.parseInt(scanner.nextLine().trim());
                        Client client = new Client(port);
                        client.run();

                        // Checks if the client was successfully ran or not
                        if (client.success()) {
                            break;
                        }

                        System.out.println("Client failed to connect. Please try again.");
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid port format. Please try again.");
                    }
                }

                break;
            }

            // If invalid then retain the loop
            else {
                System.out.println("Invalid input. Please try again.");
            }
        }
    }
}