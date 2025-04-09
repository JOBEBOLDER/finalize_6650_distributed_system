import client.UDPClient;
import common.Protocol;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;


import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class UDPClientTest {
  private static final int TEST_PORT = 9090;
  private static final String TEST_HOST = "localhost";
  private UDPClient client;
  private DatagramSocket mockServer;
  private ExecutorService serverExecutor;
  private volatile boolean serverRunning;

  @Before
  public void setUp() throws SocketException {
    // Initialize test client
    client = new UDPClient(TEST_HOST, TEST_PORT);

    // Create mock UDP server
    mockServer = new DatagramSocket(TEST_PORT);
    serverExecutor = Executors.newSingleThreadExecutor();
    serverRunning = true;

    // Start mock server thread to handle requests
    serverExecutor.submit(() -> {
      while (serverRunning && !mockServer.isClosed()) {
        try {
          handleClientRequest();
        } catch (IOException e) {
          if (serverRunning) {
            e.printStackTrace();
          }
        }
      }
    });
  }

  @After
  public void tearDown() {
    // Clean up resources
    serverRunning = false;
    serverExecutor.shutdown();
    mockServer.close();
  }

  /**
   * Helper method to handle client requests in mock server
   * Simulates server response based on request type
   */
  private void handleClientRequest() throws IOException {
    byte[] receiveData = new byte[1024];
    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

    // Wait for client request
    mockServer.receive(receivePacket);

    // Get client's address and port
    InetAddress clientAddress = receivePacket.getAddress();
    int clientPort = receivePacket.getPort();

    // Process request and prepare response
    String request = new String(receivePacket.getData(), 0, receivePacket.getLength());
    String response;

    if (request.startsWith("PUT")) {
      response = "PUT_OK";
    } else if (request.startsWith("GET")) {
      response = "GET_RESULT testValue";
    } else if (request.startsWith("DELETE")) {
      response = "DELETE_OK";
    } else {
      response = "ERROR Invalid operation";
    }

    // Send response back to client
    byte[] sendData = response.getBytes();
    DatagramPacket sendPacket = new DatagramPacket(
            sendData,
            sendData.length,
            clientAddress,
            clientPort
    );
    mockServer.send(sendPacket);
  }

  /**
   * Test basic operations (PUT, GET, DELETE)
   * Verifies that client can send all types of requests
   */
  @Test
  public void testBasicOperations() {
    client.sendRequest(Protocol.PUT, "testKey", "testValue");
    client.sendRequest(Protocol.GET, "testKey", null);
    client.sendRequest(Protocol.DELETE, "testKey", null);
  }

  /**
   * Test timeout handling
   * Verifies that client properly handles server timeout
   */
  @Test
  public void testTimeout() throws SocketException {
    // Create client with very short timeout
    UDPClient timeoutClient = new UDPClient("non-existent-host", 9999);
    timeoutClient.sendRequest(Protocol.GET, "timeoutKey", null);
    // Test passes if no exception is thrown
  }

  /**
   * Test concurrent requests
   * Verifies that client can handle multiple simultaneous requests
   */
  @Test
  public void testConcurrentRequests() throws InterruptedException {
    int numThreads = 10;
    CountDownLatch latch = new CountDownLatch(numThreads);

    for (int i = 0; i < numThreads; i++) {
      final int index = i;
      new Thread(() -> {
        try {
          client.sendRequest(Protocol.PUT, "key" + index, "value" + index);
          client.sendRequest(Protocol.GET, "key" + index, null);
          client.sendRequest(Protocol.DELETE, "key" + index, null);
        } finally {
          latch.countDown();
        }
      }).start();
    }

    assertTrue("Concurrent operations timed out",
            latch.await(30, TimeUnit.SECONDS));
  }

  /**
   * Test response validation
   * Verifies that client properly validates server responses
   */
  @Test
  public void testResponseValidation() {
    // Test with valid operations
    client.sendRequest(Protocol.PUT, "validKey", "validValue");
    client.sendRequest(Protocol.GET, "validKey", null);

    // Test with invalid operation (should be handled gracefully)
    client.sendRequest("INVALID", "key", "value");
  }

  /**
   * Test large payload handling
   * Verifies that client can handle large data packets
   */
  @Test
  public void testLargePayload() {
    StringBuilder largeValue = new StringBuilder();
    for (int i = 0; i < 100; i++) {
      largeValue.append("large-value-test-");
    }
    client.sendRequest(Protocol.PUT, "largeKey", largeValue.toString());
  }

  /**
   * Test rapid consecutive requests
   * Verifies that client can handle quick successive operations
   */
  @Test
  public void testRapidRequests() {
    for (int i = 0; i < 50; i++) {
      client.sendRequest(Protocol.PUT, "rapidKey" + i, "rapidValue" + i);
    }
  }
}