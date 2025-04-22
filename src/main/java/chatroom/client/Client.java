package chatroom.scripts;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
The way it communicates:
- Server.ClientConnection output = Client.input
- Client.output = Server.ClientConnection input
*/

public class Client implements Runnable {
    /// VARIABLES & INITIALIZERS
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private ExecutorService threadPool;

    public Client(int port) {
        try {
            this.clientSocket = new Socket("localhost", port);
        }
        catch (IOException e) {
            this.clientSocket = null; // Just to make sure it's NULL
            System.err.println("Failed to connect to server on port " + port + ". Possibly because no server exists.");
        }
    }

    @Override
    public void run() {
        // Terminates the process if client is null, i.e. due to failure when creating client socket
        if (this.clientSocket == null) {
            return;
        }

        // IO streams
        try {
            this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            this.out = new PrintWriter(this.clientSocket.getOutputStream(), true); // Auto flush, so no need to call flush()
        } catch (IOException e) {
            System.err.println("Failed to establish IO stream.");
        }

        // Initialize other important variables
        this.threadPool = Executors.newCachedThreadPool();

        // Starts listening from server in a different thread and output accordingly to the client
        this.listenForServerMessage();

        // Starts handling input from the client in the main thread
        this.startInputHandler();
    }

    /// METHODS
    // Returns whether the client connected successfully or not
    public boolean success() {
        return (this.clientSocket != null);
    }

    // Listens to message from server
    private void listenForServerMessage() {
        // Prints out all the message from the server to client, i.e.
        // When Server.ClientConnection outputs a message, print it inside the Client class
        this.threadPool.execute(() -> {
            String messageFromServer;

            try {
                while ((messageFromServer = this.in.readLine()) != null) {
                    System.out.println(messageFromServer);
                }
            } catch (IOException e) {
                System.err.println("Failed to receive message from server.");
            }
        });
    }

    // Handles input from client and send it to server
    private void startInputHandler() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (!this.clientSocket.isClosed()) {
            try {
                // Sends all Client class inputs as output to Server.ClientConnection as an input, i.e.
                // When the Client sends an input --> output it --> Server.ClientConnection picks it up as an input
                String message = reader.readLine();
                this.out.println(message);
            } catch (IOException e) {
                // The terminal must've been closed
                this.close();
            }
        }
    }

    // Closes the client
    private void close() {
        try {
            if (this.clientSocket != null && !this.clientSocket.isClosed()) {
                this.in.close();
                this.out.close();
                this.clientSocket.close();
                System.out.println("Client closed.");
            }
        } catch (IOException e) {
            System.err.println("An error occurred when attempting to close the client.");
        }
    }
}