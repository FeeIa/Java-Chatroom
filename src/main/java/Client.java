package org.example;
import java.io.*;
import java.net.*;

public class Client implements Runnable {
    public Socket client;
    public BufferedReader in;
    public PrintWriter out;

    // Quit function
    public void quit() {
        try {
            if (!this.client.isClosed()) {
                this.in.close();
                this.out.close();
                this.client.close();
                System.out.println("Client closed.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred when attempting to close the client.");
        }
    }

    @Override
    public void run() {
        try {
            this.client = new Socket("localhost", 9090);
            this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            this.out = new PrintWriter(client.getOutputStream(), true);

            new Thread(new InputThread()).start(); // Starting new input thread

            // Prints out all the message from the server to client
            String messageFromServer;
            while ((messageFromServer = this.in.readLine()) != null) {
                System.out.println(messageFromServer);
            }
        } catch (IOException e) {
            this.quit();
        }
    }

    // Handling the inputs with separate thread
    private class InputThread implements Runnable {
        @Override
        public void run() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while (!client.isClosed()) {
                try {
                    String message = reader.readLine();
                    out.println(message);
                } catch (IOException e) {
                    quit();
                }
            }
        }
    }
}