package chatroom.server;

import chatroom.Main;
import chatroom.objects.Message;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

// The Server class representing each room's server
public class Server implements Runnable {
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    protected int serverPort;
    protected int globalId = 0;
    protected ArrayList<ClientConnection> clientConnections;
    protected ArrayList<Message> messages; // Stores public chat history in the server. Private ones not included
    protected final String[] commands = {
            "/current-server || Shows the current server port you are connected to.",
            "/users-list || Shows all other connected users.",
            "/msg name#id message || Sends a private message to user with given id.",
            "/search keyword || Searches for all messages or users by specified keyword.",
            "/print-receivers message || Prints all receiver of specified message.",
    };

    public Server(int port) {
        try {
            this.serverSocket = new ServerSocket(port);
            this.serverPort = port;
        }
        catch (IOException e) {
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
        this.messages = new ArrayList<>();
        this.threadPool = Executors.newCachedThreadPool();

        // Listen for new clients
        this.listenForClientConnection();

        System.out.println("Server started on port " + this.serverPort + ". Waiting for connections...");
    }

    // Returns whether the server was started successfully or not
    public boolean success() {
        return (this.serverSocket != null);
    }

    // Listens for new client connection in a different thread
    private void listenForClientConnection() {
        this.threadPool.execute(() -> {
            while (!this.serverSocket.isClosed()) {
                try {
                    // Creates new ClientConnection from received client socket and executes it in a different thread
                    Socket client = this.serverSocket.accept();
                    this.threadPool.execute(new ClientConnection(client, this));
                } catch (IOException e) {
                    System.err.println("Failed to accept and initialize client connection.");
                    this.shutdown();
                }
            }
        });
    }

    // Broadcasts message from one client to all connected clients
    protected void broadcastMessage(Message msg) {
        for (ClientConnection c : this.clientConnections) {
            // Add the ClientConnection c to the message's receivers list
            if (c != msg.sender) {
                msg.receivers.add(c);
            }

            // Add the message to each client's messages list and print it
            c.messages.add(msg);
            c.out.println(msg.sentTime + " " + msg.sender.name + "#" + msg.sender.id + ": " + msg.content);
        }

        // Add to the server messages list
        this.messages.add(msg);
    }

    // Broadcasts server message to all connected clients
    protected void broadcastServerMessage(String msg) {
        for (ClientConnection c : this.clientConnections) {
            c.out.println(msg);
        }
    }

    // Sends private message to specified client
    protected void sendPrivateMessage(Message msg, ClientConnection c) {
        // Add the target client to the message's receivers list
        msg.receivers.add(c);

        // Add the message to the sender and target client messages list
        msg.sender.messages.add(msg);
        c.messages.add(msg);

        // Print the message to both the sender and target client
        msg.sender.out.println("[To " + c.name + "#" + c.id + "] " +  msg.sentTime + ": " + msg.content);
        c.out.println("[From " + msg.sender.name + "#" + msg.sender.id + "] " + msg.sentTime + ": " + msg.content);
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

    // Main function to start the server individually
    public static void main(String[] args) {
        Main.promptServerPort();
    }
}