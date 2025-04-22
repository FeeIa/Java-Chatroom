package chatroom.client;

import chatroom.Main;
import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Represents each Client instance (command-line interface)
public class Client implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private ExecutorService threadPool;

    public Client(int port) {
        try {
            this.clientSocket = new Socket("localhost", port);
        }
        catch (IOException e) {
            System.err.println("Failed to connect to server on port " + port + ". Possibly because no server exists.");
        }
    }

    @Override
    public void run() {
        // Terminates the process if client is null, i.e. due to failure when creating client socket
        if (this.clientSocket == null) {
            return;
        }

        // Initialize important variables
        this.threadPool = Executors.newCachedThreadPool();

        // IO streams
        try {
            this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            this.out = new PrintWriter(new OutputStreamWriter(this.clientSocket.getOutputStream()), true); // Auto flush, so no need to call flush()
        } catch (IOException e) {
            System.err.println("Failed to establish IO stream.");
            this.close();
        }

        // Starts listening from server in a different thread and output accordingly to the client
        this.listenForServerMessage();

        // Starts handling input from the client in the main thread
        this.startInputHandler();
    }

    // Returns whether the client connected successfully or not
    public boolean success() {
        return (this.clientSocket != null);
    }

    // Listens for server message being sent by ClientConnection
    private void listenForServerMessage() {
        this.threadPool.execute(() -> {
            String messageFromServer;

            try {
                while ((messageFromServer = this.in.readLine()) != null) {
                    System.out.println(messageFromServer);
                }
            } catch (IOException e) {
                System.err.println("Failed to read message from server.");
                this.close();
            }
        });
    }

    // Handles input from client and send it to server
    private void startInputHandler() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (!this.clientSocket.isClosed()) {
            try {
                String message = reader.readLine();
                this.out.println(message);
            } catch (IOException e) {
                System.err.println("Failed to read input.");
                this.close();
            }
        }
    }

    // Closes the client
    private void close() {
        try {
            if (this.clientSocket != null && !this.clientSocket.isClosed()) {
                this.clientSocket.close();
                this.threadPool.shutdown();
                this.in.close();
                this.out.close();
                System.out.println("Client closed.");
            }
        } catch (IOException e) {
            System.err.println("An error occurred when attempting to close the client.");
        }
    }

    // Main function to start the client individually
    public static void main(String[] args) {
        Main.promptClientPort();
    }
}