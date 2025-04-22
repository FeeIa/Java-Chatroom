package org.example;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Start a server or a client connection? Please type either Server or Client:");
            String choice = scanner.nextLine().trim().toLowerCase();

            if (choice.equals("server")) {
                Server server = new Server();
                server.run();
                break;
            }
            else if (choice.equals("client")) {
                Client client = new Client();
                client.run();
                break;
            }
            else {
                System.out.println("Invalid choice. Please try again!");
            }
        }
    }
}