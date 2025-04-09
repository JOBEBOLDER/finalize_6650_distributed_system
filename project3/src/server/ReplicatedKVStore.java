// ReplicatedKVStore.java
package server;

import common.KVStoreRMI;
import kvstore.KVStore;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A replicated key-value store implementing a two-phase commit (2PC) protocol.
 */
public class ReplicatedKVStore extends UnicastRemoteObject implements KVStoreRMI {
  private static final int TIMEOUT = 10000; // Timeout in milliseconds
  private static final int NUM_REPLICAS = 5; // Total number of replicas

  private final int serverId; // Unique identifier for this server instance
  private final KVStore store; // Local key-value store
  private final Map<String, String> tempStore = new ConcurrentHashMap<>(); // Temporary storage for 2PC transactions
  private final List<ReplicaInfo> replicas = new ArrayList<>(); // List of replica servers
  private final Map<String, Boolean> transactions = new ConcurrentHashMap<>(); // Tracks ongoing transactions

  public ReplicatedKVStore(int serverId) throws RemoteException {
    this.serverId = serverId;
    this.store = new KVStore();

    // Initialize replica information (excluding self)
    for (int i = 0; i < NUM_REPLICAS; i++) {
      if (i != serverId) {
        replicas.add(new ReplicaInfo(i, "localhost", 1099 + i));
      }
    }
  }

  /**
   * Retrieves a value associated with the given key.
   */
  @Override
  public String get(String key) throws RemoteException {
    return store.get(key);
  }

  /**
   * Stores a key-value pair using a two-phase commit (2PC) protocol.
   */
  @Override
  public String put(String key, String value) throws RemoteException {
    String transactionId = UUID.randomUUID().toString();
    System.out.println("Server " + serverId + " initiating 2PC for PUT: " + key + "=" + value);

    // Stage 1: PREPARE - Ask all replicas if they can perform the operation.
    boolean allPrepared = sendPrepareToReplicas(transactionId, "PUT", key, value);

    // Local preparation (store in temporary storage)
    tempStore.put(key, value);

    if (allPrepared) {
      // Stage 2: COMMIT or ABORT - If all replicas agree, commit; otherwise, abort.
      boolean allCommitted = sendCommitToReplicas(transactionId);

      // Local commit
      store.put(key, value);
      tempStore.remove(key);

      return "OK";
    } else {
      // Abort transaction
      sendAbortToReplicas(transactionId);
      tempStore.remove(key);
      return "ERROR: Failed to prepare all replicas";
    }
  }

  /**
   * Deletes a key-value pair using the two-phase commit (2PC) protocol.
   */
  @Override
  public String delete(String key) throws RemoteException {
    String transactionId = UUID.randomUUID().toString();
    System.out.println("Server " + serverId + " initiating 2PC for DELETE: " + key);

    boolean allPrepared = sendPrepareToReplicas(transactionId, "DELETE", key, null);

    // Local preparation: check if the key exists
    if (store.get(key) != null) {
      tempStore.put(key, "DELETE_MARKER"); // Mark as deleted
    } else {
      return "ERROR: Key not found"; // Key does not exist
    }

    if (allPrepared) {
      boolean allCommitted = sendCommitToReplicas(transactionId);

      // Local commit
      store.delete(key);
      tempStore.remove(key);

      return allCommitted ? "OK" : "ERROR: Failed to commit to all replicas";
    } else {
      // Abort transaction
      sendAbortToReplicas(transactionId);
      tempStore.remove(key);
      return "ERROR: Failed to prepare all replicas";
    }
  }

  /**
   * Sends a PREPARE message to all replicas for transaction approval.
   */
  private boolean sendPrepareToReplicas(String transactionId, String operation, String key, String value) {
    CountDownLatch latch = new CountDownLatch(replicas.size());
    AtomicBoolean allPrepared = new AtomicBoolean(true);

    for (ReplicaInfo replica : replicas) {
      new Thread(() -> {
        try {
          Registry registry = LocateRegistry.getRegistry(replica.getHost(), replica.getPort());
          KVStoreRMI remoteStore = (KVStoreRMI) registry.lookup("KVStore" + replica.getId());

          boolean prepared = operation.equals("PUT")
                  ? remoteStore.prepare(transactionId, "PUT", key, value)
                  : remoteStore.prepare(transactionId, "DELETE", key, null);

          if (!prepared) {
            allPrepared.set(false);
          }
        } catch (Exception e) {
          System.err.println("Error contacting replica " + replica.getId() + ": " + e.getMessage());
          allPrepared.set(false);
        } finally {
          latch.countDown();
        }
      }).start();
    }

    try {
      latch.await(TIMEOUT, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      System.err.println("Interrupted while waiting for PREPARE responses: " + e.getMessage());
      return false;
    }

    return allPrepared.get();
  }

  /**
   * Sends a COMMIT message to all replicas to finalize the transaction.
   */
  private boolean sendCommitToReplicas(String transactionId) {
    CountDownLatch latch = new CountDownLatch(replicas.size());
    AtomicBoolean allCommitted = new AtomicBoolean(true);

    for (ReplicaInfo replica : replicas) {
      new Thread(() -> {
        try {
          Registry registry = LocateRegistry.getRegistry(replica.getHost(), replica.getPort());
          KVStoreRMI remoteStore = (KVStoreRMI) registry.lookup("KVStore" + replica.getId());

          boolean committed = remoteStore.commit(transactionId);

          if (!committed) {
            allCommitted.set(false);
          }
        } catch (Exception e) {
          System.err.println("Error contacting replica " + replica.getId() + ": " + e.getMessage());
          allCommitted.set(false);
        } finally {
          latch.countDown();
        }
      }).start();
    }

    try {
      latch.await(TIMEOUT, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      System.err.println("Interrupted while waiting for COMMIT responses: " + e.getMessage());
      return false;
    }

    return allCommitted.get();
  }

  /**
   * Sends an ABORT message to all replicas to cancel the transaction.
   */
  private void sendAbortToReplicas(String transactionId) {
    for (ReplicaInfo replica : replicas) {
      new Thread(() -> {
        try {
          Registry registry = LocateRegistry.getRegistry(replica.getHost(), replica.getPort());
          KVStoreRMI remoteStore = (KVStoreRMI) registry.lookup("KVStore" + replica.getId());

          remoteStore.abort(transactionId);
        } catch (Exception e) {
          System.err.println("Error contacting replica " + replica.getId() + ": " + e.getMessage());
        }
      }).start();
    }
  }

  /**
   * Handles the PREPARE request (as a participant).
   */
  @Override
  public boolean prepare(String transactionId, String operation, String key, String value) throws RemoteException {
    System.out.println("Server " + serverId + " received PREPARE for transaction " + transactionId);

    boolean canPrepare = true;

    if (operation.equals("PUT")) {
      tempStore.put(key, value);
    } else if (operation.equals("DELETE")) {
      if (store.get(key) == null) {
        canPrepare = false;
      } else {
        tempStore.put(key, "DELETE_MARKER"); // Mark for deletion
      }
    }

    transactions.put(transactionId, canPrepare);
    return canPrepare;
  }

  /**
   * Handles the COMMIT request (as a participant).
   */
  @Override
  public boolean commit(String transactionId) throws RemoteException {
    System.out.println("Server " + serverId + " received COMMIT for transaction " + transactionId);

    Boolean prepared = transactions.get(transactionId);
    if (prepared == null || !prepared) {
      return false;
    }

    // Commit all temporary changes
    for (Map.Entry<String, String> entry : tempStore.entrySet()) {
      if ("DELETE".equals(entry.getValue())) {
        store.delete(entry.getKey());
      } else {
        store.put(entry.getKey(), entry.getValue());
      }
    }

    // Cleanup
    tempStore.clear();
    transactions.remove(transactionId);

    return true;
  }

  /**
   * Handles the ABORT request (as a participant).
   */
  @Override
  public boolean abort(String transactionId) throws RemoteException {
    System.out.println("Server " + serverId + " received ABORT for transaction " + transactionId);

    // Cleanup
    tempStore.clear();
    transactions.remove(transactionId);

    return true;
  }
}