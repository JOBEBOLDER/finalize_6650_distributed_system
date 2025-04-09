// src/server/ReplicatedRMIServer.java
package server;

import common.KVStoreRMI;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * RMI Server for the replicated Key-Value Store
 */
public class ReplicatedRMIServer {
  private static final int BASE_PORT = 1099;

  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("Usage: java server.ReplicatedRMIServer <serverId>");
      System.exit(1);
    }

    int serverId = Integer.parseInt(args[0]);

    try {
      // Create RMI registry
      Registry registry = LocateRegistry.createRegistry(BASE_PORT + serverId);

      // Create replicated KV store
      KVStoreRMI kvStore = new ReplicatedKVStore(serverId);

      // Register remote object
      registry.rebind("KVStore" + serverId, kvStore);

      System.out.println("Server " + serverId + " ready");
    } catch (Exception e) {
      System.err.println("Server exception: " + e.toString());
      e.printStackTrace();
    }
  }
}