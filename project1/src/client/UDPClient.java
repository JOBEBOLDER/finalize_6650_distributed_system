package client;

import common.Logger;
import common.Protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


/**
 * UDP Client Implementation
 * Demonstrates how to send and receive datagrams using UDP protocol
 *
 * Key Differences from TCP Client:
 * 1. Uses DatagramSocket instead of Socket
 * 2. Sends/receives packets instead of using streams
 * 3. Includes timeout handling
 * 4. No connection establishment needed
 */
public class UDPClient {

    //构造变量
    private final String serverAddress;
    private final int port;
    private final DatagramSocket socket;
    private final Logger logger;

    //创建对象
    public UDPClient(String serverAddress, int port) throws SocketException {
        this.serverAddress = serverAddress;
        this.port = port;
        this.socket = new DatagramSocket();
        //time out handling
        this.socket.setSoTimeout(2000);  // 2-second timeout
        this.logger = new Logger(UDPClient.class);
    }

    //构造方法:机器人如何发送消息（sendRequest 方法）
    public void sendRequest(String operation, String key, String value) {
        String request = Protocol.createRequest(operation, key, value);

        if (request == null) {
            logger.log("Invalid request: operation=" + operation + ", key=" + key);
            return;
        }

        try {
            // Resolve server address
            InetAddress address = InetAddress.getByName(serverAddress);

            // Prepare data to send
            byte[] sendData = request.getBytes();

            // Create packet with data, server address, and port
            DatagramPacket sendPacket = new DatagramPacket(
                    sendData,       // Data to send
                    sendData.length, // Length of data
                    address,        // Server's address
                    port           // Server's port
            );

            // Send the packet
            socket.send(sendPacket);

            //prepare buffer for response
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);

            try {
                // Wait for response (will timeout after 2 seconds)
                socket.receive(receivePacket);

                //convert received data to string (only for actual data length)
                String response = new String(receivePacket.getData(), 0 ,receivePacket.getLength());
                validateResponse(request, response);
                logger.log("Request: " + request + " | Response: " + response);

            } catch (SocketException e) {
                // Handle timeout case
                logger.log("Timeout for request: " + request);
            }

        }catch (IOException e) {
            logger.log("Error: " + e.getMessage());
        }
    }

    private void validateResponse(String request, String response) {
        String[] requestParts = request.split(" ");
        if (requestParts.length < 1) {
            logger.log("Invalid request format: " + request);
            return;
        }

        String expectedPrefix = requestParts[0].toUpperCase() + "_";
        if (!response.startsWith(expectedPrefix)) {
            logger.log("Unsolicited response: " + response + " for request: " + request);
        }

        // Additional validation for GET responses
        if (requestParts[0].equalsIgnoreCase(Protocol.GET) && response.startsWith(Protocol.GET_RESULT)) {
            String[] responseParts = response.split(" ", 2);
            if (responseParts.length < 2) {
                logger.log("Malformed GET response: " + response);
            }
        }
    }

    /**
     * Main method to demonstrate UDP client usage
     * Tests PUT, GET, DELETE operations in sequence
     */
    public static void main(String[] args) throws SocketException {
        if (args.length != 2) {
            System.out.println("Usage: java UDPClient <host> <port>");
            return;
        }

        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            UDPClient client = new UDPClient(host, port);

            for (int i = 1; i <= 5; i++) {
                client.sendRequest(Protocol.PUT, "key" + i, "value" + i);
                client.sendRequest(Protocol.GET, "key" + i, null);
                client.sendRequest(Protocol.DELETE, "key" + i, null);
            }
        } catch (SocketException e) {
            System.err.println("Failed to initialize UDP client: " + e.getMessage());
        }
    }
}
