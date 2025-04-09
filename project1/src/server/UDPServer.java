package server;

import common.Logger;
import common.Protocol;
import kvstore.KVStore;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;



/**
 * UDP Server Implementation
 * A simple key-value store server using UDP protocol
 *
 * Key Differences from TCP Server:
 * 1. UDP is connectionless - no need to maintain persistent connections
 * 2. Uses DatagramSocket instead of ServerSocket
 * 3. Communicates using packets (DatagramPacket) instead of streams
 * 4. No guarantee of delivery or order of messages
 */
public class UDPServer {
    private final int port;
    private final HashMap<String, String> store = new HashMap<>();
    private final Logger logger;

    private DatagramSocket socket;

    public UDPServer(int port) {
        this.port = port;
        this.logger = new Logger(UDPServer.class);
    }

    /**
     * Starts the UDP server and begins listening for incoming packets
     * 1. Creates a DatagramSocket bound to the specified port
     * 2. Continuously receives and processes UDP packets
     * 3. Uses a buffer to store incoming packet data
     */

    public void start() {
        try {
            socket = new DatagramSocket(port);
            logger.log("Server started on port " + port);
            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet); // Blocks until data received
                handlePacket(packet);
            }
        } catch (IOException e) {
            logger.log("Server error: " + e.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    /**
     * Handles a single UDP packet
     * 1. Extracts client address and port from packet
     * 2. Converts packet data to string
     * 3. Processes request and sends response
     *
     * Note: Unlike TCP, we need to explicitly track client address/port
     * for each packet since UDP is connectionless
     */
     private void handlePacket(DatagramPacket packet) throws IOException {

         // get the client's address and port from the packet
         // these are needed to send response back to the correct client
         InetAddress clientAddress = packet.getAddress();
         int clientPort = packet.getPort();


         // Convert received bytes to string
         // Note: only convert the actual data length, not the entire buffer
         String request = new String(packet.getData(), 0, packet.getLength());

         //process the request(think about how to respond)
         String response = processRequest(request);

         // Convert response string to bytes for sending
         byte[] responseData = response.getBytes();

         try {
             // Create response packet addressed to the client
             DatagramPacket responsePacket = new DatagramPacket(
                     responseData,          // The data to send
                     responseData.length,   // Length of the data
                     clientAddress,         // Client's IP address
                     clientPort            // Client's port number
             );

             // Send the response packet
             socket.send(responsePacket);

             // Log the interaction
             logger.log("Client " + clientAddress + ":" + clientPort +
                     " | Request: " + request + " | Response: " + response);
         } catch (IOException e) {
             logger.log("Failed to send response: " + e.getMessage());
         }
     }

    /**
     * Process client requests - same as TCP server
     * Supports PUT, GET, DELETE operations on key-value store
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
            logger.log("Processing error: " + e.getMessage());
            return "ERROR Internal server error";
        }
    }


    /**
     * Entry point of the program
     * Usage: java UDPServer <port>
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java UDPServer <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        new UDPServer(port).start();
    }


}


