import client.TCPClient;
import common.Protocol;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TCPClientTest {
  private static final int TEST_PORT = 8090;
  private static final String TEST_HOST = "localhost";
  private TCPClient client;
  private ServerSocket mockServer;
  private ExecutorService serverExecutor;

  @Before
  public void setUp() throws IOException {
    //create a new client server
    client = new TCPClient(TEST_HOST, TEST_PORT);
    // create a mock server
    mockServer = new ServerSocket(TEST_PORT);
    serverExecutor = Executors.newSingleThreadExecutor();

    // start the mock server
    serverExecutor.submit(() -> {
      while (!mockServer.isClosed()) {
        try {
          Socket clientSocket = mockServer.accept();
          handleClientRequest(clientSocket);
        } catch (IOException e) {
          if (!mockServer.isClosed()) {
            e.printStackTrace();
          }
        }
      }
    });
  }

  @After
  public void tearDown() throws IOException {
    serverExecutor.shutdown();
    mockServer.close();
  }

  // Helper methods for handling client requests
  private void handleClientRequest(Socket clientSocket) {
    try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
    ) {
      String request = in.readLine();
      // Simulate server response
      if (request.startsWith("PUT")) {
        out.println("OK");
      } else if (request.startsWith("GET")) {
        out.println("VALUE:testValue");
      } else if (request.startsWith("DELETE")) {
        out.println("DELETED");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testBasicOperations() {
    //  Testing basic PUT operations
    client.sendRequest(Protocol.PUT, "testKey", "testValue");

    // testing GET operations
    client.sendRequest(Protocol.GET, "testKey", null);

    // testing DELETE operations
    client.sendRequest(Protocol.DELETE, "testKey", null);
  }

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

    assertTrue("Concurrent operations did not complete in time",
            latch.await(30, TimeUnit.SECONDS));
  }

  @Test
  public void testInvalidServerAddress() {
    //Test connection to a non-existent server
    TCPClient invalidClient = new TCPClient("invalid-host", 9999);
    invalidClient.sendRequest(Protocol.GET, "someKey", null);
    // This test verifies that the client does not crash due to a failed connection.
  }

  @Test
  public void testLargeRequests() {
    // Testing requests with large data volumes
    StringBuilder largeValue = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      largeValue.append("large-value-test-");
    }
    client.sendRequest(Protocol.PUT, "largeKey", largeValue.toString());
  }

  @Test
  public void testRapidRequests() {
    // Testing fast-continuous requests
    for (int i = 0; i < 100; i++) {
      client.sendRequest(Protocol.PUT, "rapidKey" + i, "rapidValue" + i);
    }
  }
}