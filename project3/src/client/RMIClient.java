package client;

import common.KVStoreRMI;
import common.Logger;
import common.Protocol;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * RMI Client Implementation
 * Uses Java RMI to communicate with the server instead of sockets
 */
public class RMIClient {
  private final String serverAddress;
  private final int port;
  private final Logger logger;
  private KVStoreRMI remoteService;

  /**
   * Constructor initializes client with server details
   * @param serverAddress The IP address or hostname of the server
   * @param port The port number the server is listening on
   */
  public RMIClient(String serverAddress, int port) {
    this.serverAddress = serverAddress;
    this.port = port;
    this.logger = new Logger(RMIClient.class);
  }

  /**
   * Connects to the RMI server and gets the remote service reference
   * @return true if connection successful, false otherwise
   */
  public boolean connect() {
    try {
      // Get the registry
      Registry registry = LocateRegistry.getRegistry(serverAddress, port);

      // Look up the remote object
      remoteService = (KVStoreRMI) registry.lookup("KVStoreService");

      logger.log("Connected to server: " + serverAddress + ":" + port);
      return true;
    } catch (Exception e) {
      logger.log("Connection error: " + e.getMessage());
      return false;
    }
  }

  /**
   * Performs a PUT operation
   * @param key The key to store
   * @param value The value to associate with the key
   * @return Response from the server
   */
  public String put(String key, String value) {
    try {
      String response = remoteService.put(key, value);
      logger.log("PUT " + key + " " + value + " -> " + response);
      return response;
    } catch (Exception e) {
      logger.log("PUT error: " + e.getMessage());
      return "ERROR: " + e.getMessage();
    }
  }

  /**
   * Performs a GET operation
   * @param key The key to look up
   * @return Response from the server
   */
  public String get(String key) {
    try {
      String response = remoteService.get(key);
      logger.log("GET " + key + " -> " + response);
      return response;
    } catch (Exception e) {
      logger.log("GET error: " + e.getMessage());
      return "ERROR: " + e.getMessage();
    }
  }

  /**
   * Performs a DELETE operation
   * @param key The key to delete
   * @return Response from the server
   */
  public String delete(String key) {
    try {
      String response = remoteService.delete(key);
      logger.log("DELETE " + key + " -> " + response);
      return response;
    } catch (Exception e) {
      logger.log("DELETE error: " + e.getMessage());
      return "ERROR: " + e.getMessage();
    }
  }

  /**
   * Populates the store with test data
   * @param count Number of test entries to create
   */
  public void populateStore(int count) {
    logger.log("Populating store with " + count + " entries...");
    for (int i = 0; i < count; i++) {
      String key = "testKey" + i;
      String value = "testValue" + i + "-" + UUID.randomUUID().toString().substring(0, 8);
      put(key, value);
    }
    logger.log("Store populated with " + count + " entries");
  }

  /**
   * Runs basic tests with PUT, GET, and DELETE operations
   */
  public void runBasicTests() {
    logger.log("Running basic tests...");

    // Perform 5 PUT operations
    for (int i = 0; i < 5; i++) {
      String key = "basicKey" + i;
      String value = "basicValue" + i;
      put(key, value);
    }

    // Perform 5 GET operations
    for (int i = 0; i < 5; i++) {
      get("basicKey" + i);
    }

    // Perform 5 DELETE operations
    for (int i = 0; i < 5; i++) {
      delete("basicKey" + i);
    }

    logger.log("Basic tests completed");
  }

  /**
   * Runs concurrent client tests with multiple threads
   * @param numThreads Number of concurrent clients to simulate
   * @param operationsPerThread Number of operations per client
   */
  public void runConcurrentTests(int numThreads, int operationsPerThread) {
    logger.log("Running concurrent tests with " + numThreads + " threads, "
            + operationsPerThread + " operations per thread...");

    ExecutorService executor = Executors.newFixedThreadPool(numThreads);
    CountDownLatch latch = new CountDownLatch(numThreads);

    for (int i = 0; i < numThreads; i++) {
      final int clientId = i;
      executor.submit(() -> {
        try {
          logger.log("Client " + clientId + " started");

          // Each client performs PUT, GET, DELETE operations
          for (int j = 0; j < operationsPerThread; j++) {
            String key = "client" + clientId + "-key" + j;
            String value = "client" + clientId + "-value" + j;

            // PUT operation
            put(key, value);

            // GET operation
            get(key);

            // DELETE operation
            delete(key);
          }

          logger.log("Client " + clientId + " finished");
        } catch (Exception e) {
          logger.log("Client " + clientId + " error: " + e.getMessage());
        } finally {
          latch.countDown();
        }
      });
    }

    try {
      latch.await(); // Wait for all clients to finish
      logger.log("All concurrent tests completed");
    } catch (InterruptedException e) {
      logger.log("Concurrent test interrupted: " + e.getMessage());
    }

    executor.shutdown();
  }

  public static void main(String[] args) {
    if (args.length != 2) {
      System.out.println("Usage: java RMIClient <host> <port>");
      return;
    }

    String host = args[0];
    int port = Integer.parseInt(args[1]);

    RMIClient client = new RMIClient(host, port);

    if (client.connect()) {
      // Pre-populate the store
      client.populateStore(10);

      // Run basic PUT, GET, DELETE tests
      client.runBasicTests();

      // Run concurrent client tests (5 clients, 5 operations each)
      client.runConcurrentTests(5, 5);
    }
  }
}
