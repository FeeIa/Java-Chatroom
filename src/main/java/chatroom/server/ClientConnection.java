package chatroom.server;

import chatroom.objects.Message;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientConnection implements Runnable {
    private final Socket clientSocket;
    private final Server hostServer;
    private ExecutorService threadPool;
    protected BufferedReader in;
    protected PrintWriter out;
    protected String name;
    protected String id;
    protected ArrayList<Message> messages;

    public ClientConnection(Socket client, Server server) {
        this.clientSocket = client;
        this.hostServer = server;
    }

    @Override
    public void run() {
        // Initialize important variables
        this.threadPool = Executors.newCachedThreadPool();
        this.messages = new ArrayList<>();
        this.id = String.format("%05d", this.hostServer.globalId++); // 5 digits id

        // Client IO streams
        try {
            this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            this.out = new PrintWriter(new OutputStreamWriter(this.clientSocket.getOutputStream()), true); // Auto flush, so no need to call flush()
        } catch (IOException e) {
            System.err.println("Failed to start IO streams on ClientConnection" + this.id + ".");
            this.close();
        }

        // Ask for name
        try {
            do {
                this.out.println("Please enter your name before entering the chat room (CANNOT be empty):");
            } while ((this.name = this.in.readLine().trim()).isEmpty());

            System.out.println(this.name + "#" + this.id + " connected to the server on port " + this.hostServer.serverPort + "."); // Informs the server
        } catch (IOException e) {
            System.err.println("Failed to read name on ClientConnection " + this.id + ".");
            this.close();
        }

        // Add to the list of connected clients
        this.hostServer.clientConnections.add(this);

        // Prints the current server chat history
        this.printChatHistory();
        this.out.println();

        // Welcome message
        this.out.println("Welcome " + this.name + "#" + this.id + " to the chat room! You are currently connected to port " + this.hostServer.serverPort + ".");
        this.out.println("Type /commands to view all commands in the chat room.");
        this.out.println();

        // Print all other clients in the chat room to the new client
        this.printOtherConnectedClients();
        this.out.println();
        this.out.println("You can start chatting by typing a message and pressing enter");

        // Handling the message by picking up outputs from Client class as an input
        this.listenForClientMessage();
    }

    // Listens for client message
    private void listenForClientMessage() {
        this.threadPool.execute(() -> {
            String messageFromClient;

            try {
                while ((messageFromClient = this.in.readLine()) != null) {
                    messageFromClient = messageFromClient.trim(); // Ensures no leading or trailing whitespaces

                    // Command messages
                    if (messageFromClient.startsWith("/")) {
                        String[] messageParts = messageFromClient.substring(1).split(" ", 2);
                        String command = messageParts[0].trim();
                        String args = (messageParts.length > 1 ? messageParts[1] : "").trim();

                        switch (command.toLowerCase()) {
                            case "commands":
                                this.printCommandsList();
                                break;
                            case "current-server":
                                this.out.println("You are currently connected to server on port: " + this.hostServer.serverPort);
                                break;
                            case "users-list":
                                this.printOtherConnectedClients();
                                break;
                            case "msg":
                                this.sendPrivateMessage(args);
                                break;
                            case "search":
                                this.searchForMessage(args);
                                break;
                            case "print-receivers":
                                this.printMessageReceivers(args);
                                break;
                            default:
                                this.out.println("Unknown command. Type /commands for available commands.");
                        }
                    }
                    // Normal messages
                    else if (!messageFromClient.isEmpty()){
                        Message msg = new Message(messageFromClient, this);
                        this.hostServer.broadcastMessage(msg);
                    }
                    else {
                        this.out.println("Please send a non-empty message.");
                    }
                }
            } catch (IOException e) {
                // Client left the server triggered by IO stream throwing errors
                System.out.println(this.name + "#" + this.id + " left the server on port " + this.hostServer.serverPort + "."); // Informs the server

                // Broadcast the client leaving to all other clients
                this.hostServer.broadcastServerMessage(this.name + "#" + this.id + " has left the chat room.");
                this.close();
            }
        });
    }

    // Helper functions for command purposes
    private void printCommandsList() {
        this.out.println("Here is the list of all commands:");

        for (String cmd : this.hostServer.commands) {
            this.out.println("- " + cmd);
        }
    }

    private void printOtherConnectedClients() {
        if (this.hostServer.clientConnections.size() == 1) {
            this.out.println("You are the only user currently connected to the chat room.");
        }
        else {
            this.out.println("You are currently connected with " + (this.hostServer.clientConnections.size() - 1) + " other user(s):");

            for (ClientConnection c : this.hostServer.clientConnections) {
                if (c != this) {
                    this.out.println("- " + c.name + "#" + c.id);
                }
            }
        }
    }

    private void printChatHistory() {
        if (!this.hostServer.messages.isEmpty()) {
            this.out.println("Past messages in the chat room:");

            for (Message msg : this.hostServer.messages) {
                this.messages.add(msg);
                this.out.println(msg.sentTime + " " + msg.sender.name + "#" + msg.sender.id + ": " + msg.content);
            }
        }
    }

    private void sendPrivateMessage(String args) {
        if (args.isEmpty()) {
            this.out.println("Empty user and message to send. Please specify a valid user and message to send.");
            return;
        }

        // Pattern to catch "name#id message"
        Pattern pattern = Pattern.compile("(.+?#\\d++)\\s+(.+)"); // (Any char#digits) whitespaces (message)
        Matcher matcher = pattern.matcher(args);

        if (!matcher.matches()) {
            this.out.println("Invalid format. Please use: /msg name#id message");
            return;
        }

        String userInfo = matcher.group(1).trim();
        String messageToSend = matcher.group(2).trim();

        String[] userInfoParts = userInfo.split("#", 2);
        String username = userInfoParts[0].trim().toLowerCase();
        String userId = userInfoParts[1].trim();

        for (ClientConnection c : this.hostServer.clientConnections) {
            if (c.name.toLowerCase().equals(username) && c.id.equals(userId)) {
                if (c == this) {
                    this.out.println("You cannot send a private message to yourself.");
                }
                else {
                    this.hostServer.sendPrivateMessage(new Message(messageToSend, this), c);
                }

                return;
            }
        }

        this.out.println("User " + userInfo + " not found. Please specify a valid user.");
    }

    private void searchForMessage (String args) {
        String keyword = args.toLowerCase();

        // If empty keyword then it's invalid
        if (keyword.isEmpty()) {
            this.out.println("Empty keyword to search. Please specify a valid keyword.");
            return;
        }

        int foundMessages = 0;

        // Iterates through the client stored message
        for (Message msg : this.messages) {
            // If the message or its sender contains the keyword
            if (msg.content.toLowerCase().contains(keyword) || msg.sender.name.toLowerCase().contains(keyword)) {
                this.out.println("- \"" + msg.content + "\"" + " by " + msg.sender.name + "#" + msg.sender.id + " at " + msg.sentTime);
                foundMessages++;
            }
        }

        // If no found messages from the specified keyword
        if (foundMessages == 0) {
            this.out.println("No message(s) found.");
        }
    }

    private void printMessageReceivers (String args) {
        String targetMessage = args.toLowerCase();

        // If empty then it's invalid
        if (targetMessage.isEmpty()) {
            this.out.println("Empty target message. Please specify a valid target message.");
            return;
        }

        int foundMessages = 0;

        // Iterates through the client stored message
        for (Message msg : this.messages) {
            // Checks if the message was sent by this client and is the target message
            if (msg.sender != null && msg.sender.equals(this) && msg.content.toLowerCase().equals(targetMessage)) {
                if (msg.receivers.isEmpty()) {
                    this.out.println("Your message \"" + msg.content + "\" sent at " + msg.sentTime + " was received by nobody.");
                }
                else {
                    this.out.println("Your message \"" + msg.content + "\" sent at " + msg.sentTime + " was received by:");

                    for (ClientConnection c : msg.receivers) {
                        this.out.println("- " + c.name + "#" + c.id);
                    }
                }

                foundMessages++;
            }
        }

        // If no found messages from the specified target message
        if (foundMessages == 0) {
            this.out.println("No message(s) found.");
        }
    }

    // Closes the ClientConnection
    private void close() {
        try {
            if (this.clientSocket != null && !this.clientSocket.isClosed()) {
                this.hostServer.clientConnections.remove(this);
                this.clientSocket.close();
                this.threadPool.shutdown();
                this.in.close();
                this.out.close();
            }
        } catch (IOException e) {
            System.err.println("An error occurred when attempting to close client " + this.name + "#" + this.id + ".");
        }
    }
}