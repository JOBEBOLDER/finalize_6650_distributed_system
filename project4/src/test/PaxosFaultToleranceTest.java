package test;

import client.ReplicatedRMIClient;
import common.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Test class for verifying fault tolerance of the Paxos-based KV store
 */
public class PaxosFaultToleranceTest {
  private static final Logger logger = new Logger(PaxosFaultToleranceTest.class);

  public static void main(String[] args) {
    logger.log("Starting Paxos fault tolerance test");

    // Create client
    ReplicatedRMIClient client = new ReplicatedRMIClient();

    // Test 1: Initial state test - all servers running
    logger.log("\n=== TEST 1: Initial State - All Servers Running ===");
    testOperations(client, "test1");

    // Wait for some servers to fail randomly (due to the simulator)
    logger.log("\n=== Waiting for random server failures... ===");
    try {
      TimeUnit.SECONDS.sleep(20);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    // Test 2: After failures - Paxos should still work with majority of servers
    logger.log("\n=== TEST 2: After Random Failures - Paxos Should Continue Working ===");
    testOperations(client, "test2");

    // Wait for servers to recover
    logger.log("\n=== Waiting for server recovery... ===");
    try {
      TimeUnit.SECONDS.sleep(10);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    // Test 3: After recovery - all operations should complete
    logger.log("\n=== TEST 3: After Recovery - All Operations Should Complete ===");
    testOperations(client, "test3");

    // Additional stress test with rapid operations
    logger.log("\n=== TEST 4: Stress Test - Rapid Operations During Failures ===");
    stressTest(client);

    logger.log("\nPaxos fault tolerance test completed");
  }

  /**
   * Test basic PUT, GET, DELETE operations with the given prefix
   */
  private static void testOperations(ReplicatedRMIClient client, String prefix) {
    // Test PUT operations
    logger.log("Testing PUT operations...");
    for (int i = 1; i <= 5; i++) {
      String key = prefix + "-key" + i;
      String value = prefix + "-value" + i;
      String result = client.put(key, value);
      logger.log("PUT " + key + "=" + value + ": " + result);
    }

    // Test GET operations
    logger.log("Testing GET operations...");
    for (int i = 1; i <= 5; i++) {
      String key = prefix + "-key" + i;
      String value = client.get(key);
      logger.log("GET " + key + ": " + value);
    }

    // Test DELETE operations
    logger.log("Testing DELETE operations...");
    for (int i = 1; i <= 5; i++) {
      String key = prefix + "-key" + i;
      String result = client.delete(key);
      logger.log("DELETE " + key + ": " + result);
    }

    // Verify DELETEs
    logger.log("Verifying DELETEs...");
    for (int i = 1; i <= 5; i++) {
      String key = prefix + "-key" + i;
      String value = client.get(key);
      logger.log("GET " + key + " (after DELETE): " + value);
    }
  }

  /**
   * Perform a stress test with rapid operations during potential failures
   */
  private static void stressTest(ReplicatedRMIClient client) {
    final int NUM_OPERATIONS = 50;

    // Perform rapid PUT operations
    logger.log("Performing " + NUM_OPERATIONS + " rapid PUT operations...");
    for (int i = 0; i < NUM_OPERATIONS; i++) {
      String key = "stress-key" + i;
      String value = "stress-value" + i;
      try {
        client.put(key, value);
        // Don't log every operation to reduce overhead
        if (i % 10 == 0) {
          logger.log("Completed " + i + " PUT operations");
        }
      } catch (Exception e) {
        logger.log("Error in PUT operation: " + e.getMessage());
      }
    }

    // Verify some keys
    logger.log("Verifying random keys...");
    for (int i = 0; i < 10; i++) {
      int randomIndex = (int) (Math.random() * NUM_OPERATIONS);
      String key = "stress-key" + randomIndex;
      String expected = "stress-value" + randomIndex;
      String actual = client.get(key);
      logger.log("GET " + key + ": " + actual + " (Expected: " + expected + ")");
    }
  }
}