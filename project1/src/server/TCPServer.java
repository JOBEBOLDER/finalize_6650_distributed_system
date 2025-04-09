package server; //

import common.Logger;
import common.Protocol;
import kvstore.KVStore;

import java.io.*;
import java.net.*;
import java.util.HashMap;

/**
 * TCP Server Implementation
 * A simple key-value store server that supports PUT, GET, and DELETE operations
 * This server demonstrates basic socket programming concepts for beginners
 */
public class TCPServer {
    // The port number that the server will listen on
    private final int port;
    // HashMap to store our key-value pairs in memory
    private final HashMap<String, String> store = new HashMap<>();
    private final Logger logger;

    public TCPServer(int port) {
        this.port = port;
        this.logger = new Logger(TCPServer.class);
    }

    /**
     * Main server method that starts the TCP server
     * 1. Creates a ServerSocket to listen on the specified port
     * 2. Continuously accepts client connections in an infinite loop
     * 3. Handles each client connection in a separate method
     */
    public void start() {
        // try-with-resources ensures ServerSocket is properly closed after use
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.log("Server started on port " + port);
            // Main server loop: continuously accept new client connections
            while (true) {
                // accept() blocks until a client connects
                Socket clientSocket = serverSocket.accept();
                // Handle the connected client
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            logger.log("Server error: " + e.getMessage());
        }
    }

    /**
     * Handles an individual client connection
     * 1. Sets up input/output streams for communication
     * 2. Reads requests and sends responses in a loop
     * 3. Continues until client disconnects or an error occurs
     */
    private void handleClient(Socket clientSocket) {
        try (
                // Create a BufferedReader to read data from the client
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                // Create a PrintWriter to send data to the client (autoFlush=true)
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String request;
            // Keep reading client requests until the connection is closed
            while ((request = in.readLine()) != null) {
                // Process the request and get the response
                String response = processRequest(request);
                // Send the response back to the client
                out.println(response);
                // Log the interaction for debugging/monitoring
                logger.log("Client " + clientSocket.getInetAddress() + ":" + clientSocket.getPort() +
                        " | Request: " + request + " | Response: " + response);
            }
        } catch (IOException e) {
            logger.log("Client handling error: " + e.getMessage());
        }
    }

    /**
     * Processes client requests and returns appropriate responses
     * Supported operations:
     * - PUT key value: Store a key-value pair
     * - GET key: Retrieve the value for a given key
     * - DELETE key: Remove a key-value pair
     *
     * Request format: <OPERATION> <KEY> [VALUE]
     * Response format varies by operation (see switch cases below)
     */
    private String processRequest(String request) {
        // Split the request string into parts by whitespace
        String[] parts = request.split(" ");
        // Check if request has at least operation and key
        if (parts.length < 2) return "ERROR Malformed request";

        String operation = parts[0];
        String key = parts[1];
        String value = (parts.length > 2) ? parts[2] : null;


        // Handle different operations
        try {
            switch (operation.toUpperCase()) {
                case Protocol.PUT:
                    return KVStore.put(key, value);
                case Protocol.GET:
                    return KVStore.get(key);
                case Protocol.DELETE:
                    return KVStore.delete(key);
                default:
                    return "ERROR Invalid operation";
            }
        } catch (Exception e) {
            logger.log("Error processing request: " + e.getMessage());
            return "ERROR Internal server error";
        }
    }


    /**
     * Entry point of the program
     * Usage: java TCPServer <port>
     */
    public static void main(String[] args) {
        // Verify command line arguments
        if (args.length != 1) {
            System.out.println("Usage: java TCPServer <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        new TCPServer(port).start();
    }
}