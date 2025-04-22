package org.example;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;

/*
WHAT I LEARNED (SOMETHING TO NOTE):
- When using socket connection between server and client, the client receives server output as its input and VICE VERSA
*/

public class Server implements Runnable {
    // Server Variables
    private ServerSocket server;
    private ArrayList<ClientConnection> connections;
    private ExecutorService threadPool;
    private int globalId = 0;

    private final String[] commands = {
            "[print-receiver]:text-to-find",
            "[search]:keyword-to-find",
    };

    // Broadcast message to ALL online clients
    public void broadcast(Message msg) {
        for (ClientConnection c : this.connections) {
            // For each connected client in connections
            if (msg.sender != null) {
                ClientConnection sender = msg.sender;
                if (c != sender) {
                    msg.receivers.add(c);
                }

                c.out.println(msg.sentTime + " " + sender.name + " (" + sender.id + "): " + msg.content);
            }
            else {
                // From server
                c.out.println(msg.content);
            }
        }

        // Add the broadcast message to self + each connection receiver messages
        if (msg.sender != null) {
            msg.sender.messages.add(msg);
            for (ClientConnection c : msg.receivers) {
                c.messages.add(msg);
            }
        }
    }

    // Shutdown server upon request
    public void shutdown() {
        try {
            if (!this.server.isClosed()) {
                this.server.close();
                System.out.println("Server closed.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred when attempting to close server.");
        }
    }

    // Thread initializer
    @Override
    public void run() {
        try {
            // Initializing server
            this.server = new ServerSocket(9090);
            this.connections = new ArrayList<ClientConnection>();
            this.threadPool = Executors.newCachedThreadPool();
            System.out.println("Server started. Waiting for connections...");

            // Listening for new clients
            while (!this.server.isClosed()) {
                Socket client = this.server.accept();
                ClientConnection clientConnection = new ClientConnection(client);
                this.connections.add(clientConnection);
                this.threadPool.execute(clientConnection);
            }
        } catch (IOException e) {
            System.out.println("An error occurred when attempting to run server.");
            this.shutdown();
        }
    }

    // Client connection subclass
    private class ClientConnection implements Runnable {
        private final Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String name;
        private String id;
        private ArrayList<Message> messages; // Messages received/sent by the client

        public ClientConnection(Socket client) {
            this.client = client;
            this.messages = new ArrayList<Message>();
        }

        @Override
        public void run() {
            try {
                // IO streams
                this.in = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
                this.out = new PrintWriter(this.client.getOutputStream(), true);

                // Initializing name and ids
                this.id = String.format("%05d", globalId++);
                this.out.println("Please enter your name before entering the chat:");
                this.name = in.readLine();
                System.out.println("Client " + this.name + " (" + this.id + ")" + " connected to the server.");
                this.out.println("Welcome " + this.name + " to the chat room! Type [cmds] to view all commands.");

                // Printing the existence of other users in the chat room
                if (connections.size() == 1) {
                    this.out.println("You are the only user connected to the chat room.");
                } else {
                    this.out.println("You are currently connected with " + (connections.size() - 1) + " other user(s):");
                    for (ClientConnection c : connections) {
                        if (c != this) {
                            this.out.println("- " + c.name + " (" + c.id + ")");
                        }
                    }
                }

                // Handling the message
                String messageFromClient;
                while ((messageFromClient = this.in.readLine()) != null) {
                    if (messageFromClient.startsWith("[print-receiver]:")) {
                        String targetMessage = messageFromClient.split(":", 2)[1];
                        int foundMessages = 0;

                        if (targetMessage.isEmpty()) {
                            this.out.println("Empty message.");
                            continue;
                        }

                        for (Message msg : this.messages) {
                            if (msg.sender != null && msg.sender.equals(this) && msg.content.equals(targetMessage)) {
                                this.out.println("Message \"" + targetMessage + "\" was sent at " + msg.sentTime);

                                if (msg.receivers.isEmpty()) {
                                    this.out.println("Nobody received your message.");
                                }
                                else {
                                    this.out.println("Your message was received by:");
                                    for (ClientConnection c : msg.receivers) {
                                        this.out.println("- " + c.name + " (" + c.id + ")");
                                    }
                                }

                                foundMessages++;
                            }
                        }

                        if (foundMessages == 0) {
                            this.out.println("No message(s) found.");
                        }
                    }
                    else if (messageFromClient.startsWith("[search]:")) {
                        String targetKeyword = messageFromClient.split(":", 2)[1];
                        int foundMessages = 0;

                        if (targetKeyword.isEmpty()) {
                            this.out.println("Empty keyword.");
                            continue;
                        }

                        for (Message msg : this.messages) {
                            if (msg.sender != null) {
                                if (msg.content.contains(targetKeyword) || msg.sender.name.contains(targetKeyword)) {
                                    this.out.println("\"" + msg.content + "\"" + " by " + msg.sender.name + " (" + msg.sender.id + ") at " + msg.sentTime);
                                    foundMessages++;
                                }
                            }
                        }

                        if (foundMessages == 0) {
                            this.out.println("No message(s) found.");
                        }
                    }
                    else if (messageFromClient.equals("[cmds]")) {
                        this.out.println("Here is a list of all commands:");
                        for (String cmd : commands) {
                            this.out.println("- " + cmd);
                        }
                    }
                    else {
                        Message msg = new Message(messageFromClient);
                        msg.sender = this;
                        broadcast(msg);
                    }
                }
            } catch (IOException e) {
                // Error occurred or the client has left/closed their command bar
                System.out.println("Client " + this.name + " (" + this.id + ")" + " left the server.");
                Message msg = new Message(this.name + " (" + this.id + ")" + " has left the chat room.");
                broadcast(msg);
                connections.remove(this);
                this.quit();
            }
        }

        public void quit() {
            try {
                if (!this.client.isClosed()) {
                    this.in.close();
                    this.out.close();
                    this.client.close();
                    System.out.println("Client closed.");
                }
            } catch (IOException e) {
                System.out.println("An error occurred when attempting to close client socket.");
            }
        }
    }

    // Message class
    public class Message {
        private final String content;
        private final String sentTime;
        private ClientConnection sender;
        private ArrayList<ClientConnection> receivers;

        public Message(String content) {
            this.content = content;
            this.sentTime = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
            this.receivers = new ArrayList<ClientConnection>();
        }
    }
}