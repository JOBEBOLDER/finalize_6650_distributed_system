// src/client/ReplicatedRMIClient.java
package client;

import common.KVStoreRMI;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Client for the replicated Key-Value Store
 */
public class ReplicatedRMIClient {
  private static final int BASE_PORT = 1099;
  private static final int NUM_SERVERS = 5;
  private static final List<ServerInfo> servers = new ArrayList<>();

  static {
    // Initialize server information
    for (int i = 0; i < NUM_SERVERS; i++) {
      servers.add(new ServerInfo(i, "localhost", BASE_PORT + i));
    }
  }

  /**
   * Get a value from the Key-Value Store
   */
  public String get(String key) {
    try {
      KVStoreRMI store = connectToAnyServer();
      if (store != null) {
        return store.get(key);
      }
    } catch (Exception e) {
      System.err.println("Client exception: " + e.toString());
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Put a key-value pair into the Key-Value Store
   */
  public String put(String key, String value) {
    try {
      KVStoreRMI store = connectToAnyServer();
      if (store != null) {
        return store.put(key, value);
      }
    } catch (Exception e) {
      System.err.println("Client exception: " + e.toString());
      e.printStackTrace();
    }
    return "ERROR: Operation failed";
  }

  /**
   * Delete a key from the Key-Value Store
   */
  public String delete(String key) {
    try {
      KVStoreRMI store = connectToAnyServer();
      if (store != null) {
        return store.delete(key);
      }
    } catch (Exception e) {
      System.err.println("Client exception: " + e.toString());
      e.printStackTrace();
    }
    return "ERROR: Operation failed";
  }

  /**
   * Connect to any available server
   */
  private KVStoreRMI connectToAnyServer() {
    // Shuffle the server list
    List<ServerInfo> shuffledServers = new ArrayList<>(servers);
    Collections.shuffle(shuffledServers);

    // Try each server until successful
    for (ServerInfo server : shuffledServers) {
      try {
        Registry registry = LocateRegistry.getRegistry(server.getHost(), server.getPort());
        KVStoreRMI store = (KVStoreRMI) registry.lookup("KVStore" + server.getId());
        System.out.println("Connected to server " + server.getId());
        return store;
      } catch (Exception e) {
        System.err.println("Could not connect to server " + server.getId() + ": " + e.toString());
      }
    }

    System.err.println("Could not connect to any server");
    return null;
  }

  /**
   * Test the client
   */
  public static void main(String[] args) {
    ReplicatedRMIClient client = new ReplicatedRMIClient();

    System.out.println("Populating store with test data...");
    // Pre-populate store
    for (int i = 1; i <= 10; i++) {
      String key = "key" + i;
      String value = "value" + i;
      String result = client.put(key, value);
      System.out.println("PUT " + key + "=" + value + ": " + result);
    }

    System.out.println("\nTesting GET operations...");
    // Get values
    for (int i = 1; i <= 5; i++) {
      String key = "key" + i;
      String value = client.get(key);
      System.out.println("GET " + key + ": " + value);
    }

    System.out.println("\nTesting PUT operations...");
    // Update values
    for (int i = 6; i <= 10; i++) {
      String key = "key" + i;
      String value = "updated_value" + i;
      String result = client.put(key, value);
      System.out.println("PUT " + key + "=" + value + ": " + result);
    }

    System.out.println("\nTesting DELETE operations...");
    // Delete values
    for (int i = 1; i <= 5; i++) {
      String key = "key" + i;
      String result = client.delete(key);
      System.out.println("DELETE " + key + ": " + result);
    }

    System.out.println("\nVerifying DELETEs...");
    // Verify deletions
    for (int i = 1; i <= 5; i++) {
      String key = "key" + i;
      String value = client.get(key);
      System.out.println("GET " + key + ": " + value);
    }

    System.out.println("\nVerifying PUTs...");
    // Verify updates
    for (int i = 6; i <= 10; i++) {
      String key = "key" + i;
      String value = client.get(key);
      System.out.println("GET " + key + ": " + value);
    }
  }

  /**
   * Server information class
   */
  static class ServerInfo {
    private final int id;
    private final String host;
    private final int port;

    public ServerInfo(int id, String host, int port) {
      this.id = id;
      this.host = host;
      this.port = port;
    }

    public int getId() {
      return id;
    }

    public String getHost() {
      return host;
    }

    public int getPort() {
      return port;
    }
  }
}