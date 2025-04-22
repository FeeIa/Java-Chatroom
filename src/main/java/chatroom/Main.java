package chatroom;

import chatroom.client.Client;
import chatroom.server.Server;
import java.util.*;

// The "Main" class to run either Server or Client by choice.
public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    // Prompt the user to input server port until valid
    public static void promptServerPort() {
        while (true) {
            System.out.println("Enter a port (1 - 65535) to connect to:");

            try {
                int port = Integer.parseInt(scanner.nextLine().trim());
                if (port < 1 || port > 65535) {
                    throw new NumberFormatException();
                }

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
    }

    // Prompt the user to input client port until valid
    public static void promptClientPort() {
        while (true) {
            System.out.println("Enter a port (1 - 65535) to connect to:");

            try {
                int port = Integer.parseInt(scanner.nextLine().trim());
                if (port < 1 || port > 65535) {
                    throw new NumberFormatException();
                }

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
    }

    public static void main(String[] args) {
        // Loops until input is either Server or Client
        while (true) {
            System.out.println("Start a server or a client instance? Please type either Server or Client:");
            String choice = scanner.nextLine().trim().toLowerCase();

            // Starts server or client class instance, if successful --> break the loop
            if (choice.equals("server")) {
                promptServerPort();
                break;
            }

            else if (choice.equals("client")) {
                promptClientPort();
                break;
            }

            // If invalid then retain the loop
            else {
                System.out.println("Invalid input. Please try again.");
            }
        }

        scanner.close();
    }
}