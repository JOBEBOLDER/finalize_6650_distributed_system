package test;

import client.RMIClient;
import common.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Concurrent testing class for the Key-Value Store
 * Tests the performance of the server under heavy concurrent load
 */
public class ConcurrentTest {
  private final Logger logger;
  private final String serverHost;
  private final int serverPort;

  /**
   * Constructor initializes test parameters
   * @param serverHost Server hostname or IP
   * @param serverPort Server port
   */
  public ConcurrentTest(String serverHost, int serverPort) {
    this.serverHost = serverHost;
    this.serverPort = serverPort;
    this.logger = new Logger(ConcurrentTest.class);
  }

  /**
   * Run a high-concurrency test with multiple clients
   * @param numClients Number of concurrent clients
   * @param operationsPerClient Number of operations each client performs
   */
  public void runTest(int numClients, int operationsPerClient) {
    logger.log("Starting concurrent test with " + numClients + " clients, "
            + operationsPerClient + " operations per client");

    // Create thread pool for clients
    ExecutorService executor = Executors.newFixedThreadPool(numClients);
    CountDownLatch latch = new CountDownLatch(numClients);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failureCount = new AtomicInteger(0);

    long startTime = System.currentTimeMillis();

    // Start all client threads
    for (int i = 0; i < numClients; i++) {
      final int clientId = i;
      executor.submit(() -> {
        try {
          RMIClient client = new RMIClient(serverHost, serverPort);

          if (client.connect()) {
            // Each client performs operations in sequence
            for (int j = 0; j < operationsPerClient; j++) {
              try {
                String key = "client" + clientId + "-key" + j;
                String value = "client" + clientId + "-value" + j;

                // PUT operation
                String putResult = client.put(key, value);
                if (putResult.equals("PUT_OK")) {
                  successCount.incrementAndGet();
                } else {
                  failureCount.incrementAndGet();
                }

                // GET operation
                String getResult = client.get(key);
                if (getResult.startsWith("GET_RESULT")) {
                  successCount.incrementAndGet();
                } else {
                  failureCount.incrementAndGet();
                }

                // DELETE operation
                String deleteResult = client.delete(key);
                if (deleteResult.equals("DELETE_OK")) {
                  successCount.incrementAndGet();
                } else {
                  failureCount.incrementAndGet();
                }
              } catch (Exception e) {
                failureCount.incrementAndGet();
                logger.log("Client " + clientId + " operation error: " + e.getMessage());
              }
            }
          } else {
            failureCount.addAndGet(operationsPerClient * 3); // 3 operations per iteration
            logger.log("Client " + clientId + " failed to connect");
          }
        } catch (Exception e) {
          failureCount.addAndGet(operationsPerClient * 3); // 3 operations per iteration
          logger.log("Client " + clientId + " error: " + e.getMessage());
        } finally {
          latch.countDown();
        }
      });
    }

    try {
      // Wait for all clients to finish, with a timeout
      boolean completed = latch.await(5, TimeUnit.MINUTES);

      long endTime = System.currentTimeMillis();
      long duration = endTime - startTime;

      // Report results
      logger.log("Concurrent test " + (completed ? "completed" : "timed out"));
      logger.log("Total duration: " + duration + "ms");
      logger.log("Successful operations: " + successCount.get());
      logger.log("Failed operations: " + failureCount.get());

      // Calculate operations per second
      double opsPerSecond = (successCount.get() + failureCount.get()) / (duration / 1000.0);
      logger.log(String.format("Performance: %.2f operations per second", opsPerSecond));
    } catch (InterruptedException e) {
      logger.log("Test interrupted: " + e.getMessage());
    }

    executor.shutdown();
  }

  public static void main(String[] args) {
    if (args.length < 2 || args.length > 4) {
      System.out.println("Usage: java ConcurrentTest <host> <port> [numClients] [opsPerClient]");
      return;
    }

    String host = args[0];
    int port = Integer.parseInt(args[1]);

    // Default: 10 clients, 20 operations per client
    int numClients = (args.length > 2) ? Integer.parseInt(args[2]) : 10;
    int opsPerClient = (args.length > 3) ? Integer.parseInt(args[3]) : 20;

    ConcurrentTest test = new ConcurrentTest(host, port);
    test.runTest(numClients, opsPerClient);
  }
}
