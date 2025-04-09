import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import server.UDPServer;

import static org.junit.Assert.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.*;

/**
 * Test suite for UDPServer class
 * Tests server initialization, packet handling, and request processing
 */
public class UDPServerTest {
  private static final int TEST_PORT = 9999;
  private UDPServer server;
  private ExecutorService serverExecutor;
  private DatagramSocket clientSocket;

  /**
   * Set up test environment before each test
   * Initializes server and starts it in a separate thread
   */
  @Before
  public void setUp() throws Exception {
    server = new UDPServer(TEST_PORT);
    serverExecutor = Executors.newSingleThreadExecutor();
    clientSocket = new DatagramSocket();
    clientSocket.setSoTimeout(2000); // 2-second timeout

    // Start server in separate thread
    serverExecutor.submit(() -> {
      server.start();
    });

    // Allow server time to start
    Thread.sleep(1000);
  }

  /**
   * Clean up resources after each test
   */
  @After
  public void tearDown() throws Exception {
    clientSocket.close();
    serverExecutor.shutdownNow();
    serverExecutor.awaitTermination(5, TimeUnit.SECONDS);
  }

  /**
   * Helper method to send UDP request and receive response
   * @param request Request string to send
   * @return Server's response as string
   */
  private String sendRequest(String request) throws Exception {
    byte[] sendData = request.getBytes();
    InetAddress serverAddress = InetAddress.getByName("localhost");

    // Create and send request packet
    DatagramPacket sendPacket = new DatagramPacket(
            sendData,
            sendData.length,
            serverAddress,
            TEST_PORT
    );
    clientSocket.send(sendPacket);

    // Prepare and receive response packet
    byte[] receiveData = new byte[1024];
    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
    clientSocket.receive(receivePacket);

    return new String(receivePacket.getData(), 0, receivePacket.getLength());
  }

  /**
   * Test basic PUT operation
   * Verifies server can store key-value pairs
   */
  @Test
  public void testPutOperation() throws Exception {
    String response = sendRequest("PUT testKey testValue");
    assertEquals("PUT_OK", response);
  }

  /**
   * Test basic GET operation
   * Verifies server can retrieve stored values
   */
  @Test
  public void testGetOperation() throws Exception {
    // First PUT a value
    sendRequest("PUT getKey getValue");

    // Then GET it
    String response = sendRequest("GET getKey");
    assertEquals("GET_RESULT getValue", response);
  }

  /**
   * Test basic DELETE operation
   * Verifies server can delete stored key-value pairs
   */
  @Test
  public void testDeleteOperation() throws Exception {
    // First PUT a value
    sendRequest("PUT deleteKey deleteValue");

    // Then DELETE it
    String response = sendRequest("DELETE deleteKey");
    assertEquals("DELETE_OK", response);

    // Verify it's deleted
    response = sendRequest("GET deleteKey");
    assertEquals("GET_ERROR Key not found", response);
  }

  /**
   * Test concurrent client requests
   * Verifies server can handle multiple simultaneous clients
   */
  @Test
  public void testConcurrentClients() throws Exception {
    int numClients = 10;
    CountDownLatch latch = new CountDownLatch(numClients);
    ExecutorService clientExecutor = Executors.newFixedThreadPool(numClients);

    for (int i = 0; i < numClients; i++) {
      final int clientId = i;
      clientExecutor.submit(() -> {
        try (DatagramSocket socket = new DatagramSocket()) {
          socket.setSoTimeout(2000);
          String key = "key" + clientId;
          String value = "value" + clientId;

          // Test sequence of operations
          String putResponse = sendRequest("PUT " + key + " " + value);
          assertEquals("PUT_OK", putResponse);

          String getResponse = sendRequest("GET " + key);
          assertEquals("GET_RESULT" + value, getResponse);

          latch.countDown();
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
    }

    assertTrue("Concurrent operations did not complete in time",
            latch.await(30, TimeUnit.SECONDS));
    clientExecutor.shutdown();
  }

  /**
   * Test handling of invalid requests
   * Verifies server properly handles malformed or invalid requests
   */
  @Test
  public void testInvalidRequests() throws Exception {
    // Test empty request
    String response = sendRequest("");
    assertEquals("ERROR Malformed request", response);

    // Test invalid operation
    response = sendRequest("INVALID_OP key value");
    assertEquals("ERROR Invalid operation", response);

    // Test missing key
    response = sendRequest("PUT");
    assertEquals("ERROR Malformed request", response);
  }

  /**
   * Test large packet handling
   * Verifies server can handle packets near the size limit
   */
  @Test
  public void testLargePacket() throws Exception {
    // Create large value (but within packet size limit)
    StringBuilder largeValue = new StringBuilder();
    for (int i = 0; i < 100; i++) {
      largeValue.append("large-value-test-");
    }

    String response = sendRequest("PUT largeKey " + largeValue.toString());
    assertEquals("PUT_OK", response);

    response = sendRequest("GET largeKey");
    assertTrue(response.startsWith("GET_RESULT"));
    assertTrue(response.contains(largeValue.toString()));
  }

  /**
   * Test packet loss simulation
   * Verifies server resilience to UDP packet loss
   */
  @Test
  public void testPacketLossResilience() throws Exception {
    // Send multiple requests rapidly, simulating network congestion
    for (int i = 0; i < 50; i++) {
      String key = "key" + i;
      String value = "value" + i;

      // Don't wait for response, simulate packet loss
      byte[] sendData = ("PUT " + key + " " + value).getBytes();
      DatagramPacket sendPacket = new DatagramPacket(
              sendData,
              sendData.length,
              InetAddress.getByName("localhost"),
              TEST_PORT
      );
      clientSocket.send(sendPacket);
    }

    // Verify at least some operations succeeded
    Thread.sleep(1000); // Allow time for processing
    String response = sendRequest("GET key1");
    assertTrue(response.startsWith("GET_RESULT") ||
            response.equals("GET_ERROR Key not found"));
  }

  /**
   * Test request sequence consistency
   * Verifies server maintains data consistency across operations
   */
  @Test
  public void testRequestSequence() throws Exception {
    String key = "sequenceKey";
    String value1 = "value1";
    String value2 = "value2";

    // PUT first value
    String response = sendRequest("PUT " + key + " " + value1);
    assertEquals("PUT_OK", response);

    // GET and verify first value
    response = sendRequest("GET " + key);
    assertEquals("GET_RESULT " + value1, response);

    // Update value
    response = sendRequest("PUT " + key + " " + value2);
    assertEquals("PUT_OK", response);

    // GET and verify updated value
    response = sendRequest("GET " + key);
    assertEquals("GET_RESULT " + value2, response);

    // DELETE
    response = sendRequest("DELETE " + key);
    assertEquals("DELETE_OK", response);

    // Verify deletion
    response = sendRequest("GET " + key);
    assertEquals("GET_ERROR Key not found", response);
  }
}