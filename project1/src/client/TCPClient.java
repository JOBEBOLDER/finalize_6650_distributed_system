package client;


import common.Logger;
import common.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


/**
 * TCP Client Implementation
 * This client connects to a TCP server and sends requests for key-value operations
 *
 * Key Concepts:
 * 1. Creates a Socket to connect to server (unlike ServerSocket in server)
 * 2. Uses PrintWriter for sending data and BufferedReader for receiving responses
 * 3. Each request creates a new connection (in this implementation)
 */
public class TCPClient {

    // Server's IP address or hostname(机器人构造）
    private final String serverAddress;
    private final int port;

    private final Logger logger;


    //(机器人出生了）构造方法
    /**
     * Constructor to initialize client with server details
     * @param serverAddress The IP address or hostname of the server
     * @param port The port number the server is listening on
     */
    public TCPClient(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.logger = new Logger(TCPClient.class);
    }

    /**
     * Sends a single request to the server and receives the response
     * 1. Creates a new socket connection
     * 2. Sets up input/output streams
     * 3. Sends request and reads response
     * 4. Closes connection when done
     *
     * Note: This implementation creates a new connection for each request.
     * In a production environment, you might want to maintain a persistent connection
     * for better performance.
     *
     */
    public void sendRequest(String operation, String key, String value) {
        String request = Protocol.createRequest(operation, key, value);
        try (
                Socket socket = new Socket(serverAddress, port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            out.println(request);
            String response = in.readLine();
            logger.log("Request: " + request + " | Response: " + response);
        } catch (IOException e) {
            logger.log("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java TCPClient <host> <port>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        TCPClient client = new TCPClient(host, port);

        // Test 5 PUT, GET, DELETE operations
        for (int i = 1; i <= 5; i++) {
            client.sendRequest(Protocol.PUT, "key" + i, "value" + i);//key" + i 代表字符串拼接操作
            client.sendRequest(Protocol.GET, "key" + i, null);
            client.sendRequest(Protocol.DELETE, "key" + i, null);
        }
    }




}
