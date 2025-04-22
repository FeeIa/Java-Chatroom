package chatroom.scripts;

import chatroom.Object;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/*
The way server and client communicate:
- Server.ClientConnection output = Client.input
- Client.output = Server.ClientConnection input
*/

public class Server implements Runnable {
    /// VARIABLES & INITIALIZERS
    private ServerSocket serverSocket;
    private ArrayList<ClientConnection> clientConnections;
    private ExecutorService threadPool;
    private int globalId = 0;
    private final String[] commands = {
            "[print-receiver]:message | Prints all receiver of specified message.",
            "[search]:key-message-to-search | Searches for all messages or users by specified keyword.",
            "[user-list] | Shows all other connected users.",
            "[current-server] | Shows the current server port you are connected to."
    };

    public Server(int port) {
        try {
            this.serverSocket = new ServerSocket(port);
        }
        catch (IOException e) {
            this.serverSocket = null; // Just ensuring that it's NULL
            System.err.println("Failed to open server on port " + port + ". Possibly because the port is already in use.");
        }
    }

    @Override
    public void run() {
        // Terminates process if server is null, i.e. failed to create a server socket
        if (this.serverSocket == null) {
            return;
        }

        // Initialize important variables
        this.clientConnections = new ArrayList<>();
        this.threadPool = Executors.newCachedThreadPool();
        System.out.println("Server started on port " + this.serverSocket.getLocalPort() + ". Waiting for connections...");

        // Listen for new clients
        this.listenForClientConnection();
    }

    /// METHODS
    // Returns whether the server was started successfully or not
    public boolean success() {
        return (this.serverSocket != null);
    }

    // Listens for new client connection in a different thread
    private void listenForClientConnection() {
        this.threadPool.execute(() -> {
            while (!this.serverSocket.isClosed()) {
                try {
                    // Creates new ClientConnection from received client socket and adds to list of clients and threads
                    Socket client = this.serverSocket.accept();
                    ClientConnection clientConnection = new ClientConnection(client);
                    this.clientConnections.add(clientConnection);
                    this.threadPool.execute(clientConnection);
                } catch (IOException e) {
                    System.err.println("Failed to accept and initialize client connection.");
                    this.shutdown();
                }
            }
        });
    }

    // Broadcasts message to all connected clients
    private void broadcastMessageToConnectedClients(chatroom.Object.Message msg) {
        // For each connected client in connections
        for (ClientConnection c : this.clientConnections) {
            // If the target message to broadcast sender is not NULL
            if (msg.sender != null) {
                ClientConnection sender = msg.sender;

                // If iterated connection (c) is NOT equal to the message's sender connection
                // Add it as receivers
                if (c != sender) {
                    msg.receivers.add(c);
                }

                // Print the message out
                c.out.println(msg.sentTime + " " + sender.name + " (" + sender.id + "): " + msg.content);
            }

            // Must be from server
            else {
                c.out.println(msg.content);
            }
        }

        // If the message wasn't from the server
        // Add the broadcast message to sender and receivers messages list
        if (msg.sender != null) {
            msg.sender.messages.add(msg);

            for (ClientConnection c : msg.receivers) {
                c.messages.add(msg);
            }
        }
    }

    // Shutdowns (closes) the Server class instance
    private void shutdown() {
        try {
            if (this.serverSocket != null && !this.serverSocket.isClosed()) {
                this.serverSocket.close();

                System.out.println("Server on port " + this.serverSocket.getLocalPort() + " has been closed.");
            }
        } catch (IOException e) {
            System.err.println("An error occurred when attempting to close server on port " + this.serverSocket.getLocalPort() + ".");
        }
    }

    /// INNER CLASSES
    // The interactable client class that is used to communicate
    public class ClientConnection implements Runnable {
        /// VARIABLES & INITIALIZERS
        private final Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;
        protected String name;
        protected String id;
        protected ArrayList<chatroom.Object.Message> messages;

        public ClientConnection(Socket client) {
            this.clientSocket = client;
        }

        @Override
        public void run() {
            // Initialize important variables
            this.messages = new ArrayList<>();
            this.id = String.format("%05d", globalId++); // 5 digits id

            // Client IO streams
            try {
                this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
                this.out = new PrintWriter(this.clientSocket.getOutputStream(), true); // Auto flush, so no need to call flush()
            } catch (IOException e) {
                System.err.println("Failed to start IO stream for client " + this.id);
                this.close();
            }

            // Ask for name
            try {
                do {
                    this.out.println("Please enter your name before entering (CANNOT be empty):");
                } while ((this.name = this.in.readLine().trim()).isEmpty());

                System.out.println("Client " + this.name + " (" + this.id + ")" + " connected to the server."); // Informs the server
            } catch (IOException e) {
                System.err.println("Failed to read name for client " + this.id);
                this.close();
            }

            // Welcome message
            this.out.println("Welcome " + this.name + " to the chat room! You are currently connected to port " + serverSocket.getLocalPort() + ".");
            this.out.println("Type [commands] to view all commands in the chat room.");

            // Print all other clients in the chat room to the new client
            this.printOtherConnectedClients();

            // Handling the message by picking up outputs from Client class as an input
            this.listenForClientMessage();
        }

        /// METHODS
        // Listen to message from client in a new thread
        private void listenForClientMessage() {
            threadPool.execute(() -> {
                String messageFromClient;

                try {
                    while ((messageFromClient = this.in.readLine()) != null) {
                        messageFromClient = messageFromClient.trim(); // Ensures no leading or trailing whitespaces

                        // [print-receiver] command
                        if (messageFromClient.startsWith("[print-receiver]:")) {
                            String targetMessage = messageFromClient.split(":", 2)[1].trim(); // Takes the String after ":" as the target message
                            int foundMessages = 0;

                            // If empty then it's invalid
                            if (targetMessage.isEmpty()) {
                                this.out.println("Empty target message. Please enter a valid target message.");
                                continue;
                            }

                            // Iterates through the client stored message
                            for (chatroom.Object.Message msg : this.messages) {
                                // Checks if the message was sent by this client and is the target message
                                if (msg.sender != null && msg.sender.equals(this) && msg.content.equals(targetMessage)) {
                                    this.out.println("Your message \"" + targetMessage + "\" sent at " + msg.sentTime + " was received by:");

                                    // If receivers list is empty
                                    if (msg.receivers.isEmpty()) {
                                        this.out.println("Nobody received your message.");
                                    } else {
                                        for (ClientConnection c : msg.receivers) {
                                            this.out.println("- " + c.name + " (" + c.id + ")");
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

                        // [search] command
                        else if (messageFromClient.startsWith("[search]:")) {
                            String keyword = messageFromClient.split(":", 2)[1].trim(); // Takes the String after ":" as the keyword
                            int foundMessages = 0;

                            // If empty keyword then it's invalid
                            if (keyword.isEmpty()) {
                                this.out.println("Empty keyword to search. Please enter a valid keyword.");
                                continue;
                            }

                            // Iterates through the client stored message
                            for (chatroom.Object.Message msg : this.messages) {
                                if (msg.sender != null) {
                                    // If the message or its sender contains the keyword
                                    if (msg.content.contains(keyword) || msg.sender.name.contains(keyword)) {
                                        this.out.println("- \"" + msg.content + "\"" + " by " + msg.sender.name + " (" + msg.sender.id + ") at " + msg.sentTime);
                                        foundMessages++;
                                    }
                                }
                            }

                            // If no found messages from the specified target message
                            if (foundMessages == 0) {
                                this.out.println("No message(s) found.");
                            }
                        }

                        // [commands] command
                        else if (messageFromClient.equals("[commands]")) {
                            this.out.println("Here is the list of all commands:");
                            for (String cmd : commands) {
                                this.out.println("- " + cmd);
                            }
                        }

                        // [user-list] command
                        else if (messageFromClient.equals("[user-list]")) {
                            this.printOtherConnectedClients();
                        }

                        // [current-server] command
                        else if (messageFromClient.equals("[current-server]")) {
                            this.out.println("Current server port: " + serverSocket.getLocalPort());
                        }

                        // Normal messages
                        else {
                            chatroom.Object.Message msg = new chatroom.Object.Message(messageFromClient);
                            msg.sender = this;
                            broadcastMessageToConnectedClients(msg);
                        }
                    }
                } catch (IOException e) {
                    // Client left the server triggered by IO stream throwing errors
                    System.out.println("Client " + this.name + " (" + this.id + ")" + " left the server."); // Informs the server

                    // Broadcast the client leaving to all other clients
                    chatroom.Object.Message msg = new Object.Message(this.name + " (" + this.id + ")" + " has left the chat room.");
                    broadcastMessageToConnectedClients(msg);
                    this.close();
                }
            });
        }

        // Prints all other connected clients in the server
        private void printOtherConnectedClients() {
            if (clientConnections.size() == 1) {
                this.out.println("You are the only user currently connected to the chat room.");
            }
            else {
                this.out.println("You are currently connected with " + (clientConnections.size() - 1) + " other user(s):");
                for (ClientConnection c : clientConnections) {
                    if (c != this) {
                        this.out.println("- " + c.name + " (" + c.id + ")");
                    }
                }
            }
        }

        // Closes the client connection
        private void close() {
            try {
                if (this.clientSocket != null && !this.clientSocket.isClosed()) {
                    clientConnections.remove(this);
                    this.in.close();
                    this.out.close();
                    this.clientSocket.close();
                    System.out.println("Client " + this.name + " (" + this.id + ")" + " has been closed.");
                }
            } catch (IOException e) {
                System.err.println("An error occurred when attempting to close client " + this.name + " (" + this.id + ").");
            }
        }
    }
}