// server/PaxosServer.java
package server;

import common.Logger;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Server for the fault-tolerant replicated Key-Value Store using Paxos
 */
public class PaxosServer {
  private static final int BASE_PORT = 8090;
  private static final Logger logger = new Logger(PaxosServer.class);

  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("Usage: java server.PaxosServer <serverId>");
      System.exit(1);
    }

    int serverId = Integer.parseInt(args[0]);

    try {
      // Create RMI registry
      Registry registry = LocateRegistry.createRegistry(BASE_PORT + serverId);

      // Create Paxos-based KV store
      PaxosKVStore kvStore = new PaxosKVStore(serverId);

      // Register as both KVStore and Paxos service
      registry.rebind("KVStore" + serverId, kvStore);
      registry.rebind("PaxosService" + serverId, kvStore);

      logger.log("Server " + serverId + " ready with Paxos consensus");

      // Add shutdown hook
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        kvStore.shutdown();
        logger.log("Server " + serverId + " shutting down");
      }));
    } catch (Exception e) {
      logger.log("Server exception: " + e.toString());
      e.printStackTrace();
    }
  }
}