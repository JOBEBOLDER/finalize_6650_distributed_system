import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import server.TCPServer;

import static org.junit.Assert.*;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * Test suite for TCPServer class
 * Tests server initialization, client handling, and request processing
 */
public class TCPServerTest {
  private static final int TEST_PORT = 8888;
  private TCPServer server;
  private ExecutorService serverExecutor;

  /**
   * Set up test environment before each test
   * Initializes server and starts it in a separate thread
   */
  @Before
  public void setUp() {
    server = new TCPServer(TEST_PORT);
    serverExecutor = Executors.newSingleThreadExecutor();

    // Start server in separate thread
    serverExecutor.submit(() -> {
      server.start();
    });

    // Allow server time to start
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * Clean up resources after each test
   */
  @After
  public void tearDown() {
    serverExecutor.shutdownNow();
    try {
      serverExecutor.awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * Helper method to send a request to server and get response
   */
  private String sendRequest(String request) throws IOException {
    try (
            Socket socket = new Socket("localhost", TEST_PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
    ) {
      out.println(request);
      return in.readLine();
    }
  }

  /**
   * Test basic PUT operation
   * Verifies server can store key-value pairs
   */
  @Test
  public void testPutOperation() throws IOException {
    String response = sendRequest("PUT testKey testValue");
    assertEquals("PUT_OK", response);
  }

  /**
   * Test basic GET operation
   * Verifies server can retrieve stored values
   */
  @Test
  public void testGetOperation() throws IOException {
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
  public void testDeleteOperation() throws IOException {
    // First PUT a value
    sendRequest("PUT deleteKey deleteValue");

    // Then DELETE it
    String response = sendRequest("DELETE deleteKey");
    assertEquals("DELETE_OK", response);

    // Verify it's deleted by trying to GET it
    response = sendRequest("GET deleteKey");
    assertEquals("GET_ERROR", response);
  }

  /**
   * Test concurrent client connections
   * Verifies server can handle multiple clients simultaneously
   */
  @Test
  public void testConcurrentClients() throws InterruptedException {
    int numClients = 10;
    CountDownLatch latch = new CountDownLatch(numClients);
    ExecutorService clientExecutor = Executors.newFixedThreadPool(numClients);

    for (int i = 0; i < numClients; i++) {
      final int clientId = i;
      clientExecutor.submit(() -> {
        try {
          String key = "key" + clientId;
          String value = "value" + clientId;

          // Test PUT
          String putResponse = sendRequest("PUT " + key + " " + value);
          assertEquals("PUT_OK", putResponse);

          // Test GET
          String getResponse = sendRequest("GET " + key);
          assertEquals("GET_RESULT " + value, getResponse);

          latch.countDown();
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
    }

    assertTrue("Concurrent operations did not complete in time",
            latch.await(30, TimeUnit.SECONDS));
    clientExecutor.shutdown();
  }

  /**
   * Test invalid requests
   * Verifies server properly handles malformed requests
   */
  @Test
  public void testInvalidRequests() throws IOException {
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
   * Test large data handling
   * Verifies server can handle large key-value pairs
   */
  @Test
  public void testLargeData() throws IOException {
    // Create large value
    StringBuilder largeValue = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      largeValue.append("large-value-test-");
    }

    String response = sendRequest("PUT largeKey " + largeValue.toString());
    assertEquals("PUT_OK", response);

    response = sendRequest("GET largeKey");
    assertTrue(response.startsWith("GET_RESULT"));
    assertTrue(response.contains(largeValue.toString()));
  }

  /**
   * Test request sequence
   * Verifies server maintains consistency across operations
   */
  @Test
  public void testRequestSequence() throws IOException {
    String key = "sequenceKey";
    String value1 = "value1";
    String value2 = "value2";

    // PUT first value
    String response = sendRequest("PUT " + key + " " + value1);
    assertEquals("PUT_OK", response);

    // GET and verify first value
    response = sendRequest("GET " + key);
    assertEquals("GET_RESULT " + value1, response);

    // PUT second value (update)
    response = sendRequest("PUT " + key + " " + value2);
    assertEquals("PUT_OK", response);

    // GET and verify second value
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