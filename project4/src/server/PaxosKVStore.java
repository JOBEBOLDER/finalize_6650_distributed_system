// server/PaxosKVStore.java
package server;

import common.*;
import kvstore.KVStore;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A replicated key-value store implementing the Paxos consensus algorithm.
 * This class serves as both KVStore and implements the Paxos protocol roles.
 */
public class PaxosKVStore extends UnicastRemoteObject implements KVStoreRMI, PaxosRMI {
  private static final int NUM_REPLICAS = 5; // Total number of replicas
  private static final int MAJORITY = (NUM_REPLICAS / 2) + 1; // Required majority for Paxos
  private static final int TIMEOUT = 5000; // Timeout in milliseconds
  private static final int ACCEPTOR_FAILURE_MIN_TIME = 5000; // Min time before acceptor failure (5 seconds)
  private static final int ACCEPTOR_FAILURE_MAX_TIME = 15000; // Max time before acceptor failure (15 seconds)
  private static final int ACCEPTOR_RESTART_TIME = 3000; // Time before acceptor restarts (3 seconds)

  private final int serverId; // Unique identifier for this server instance
  private final KVStore store; // Local key-value store
  private final List<ReplicaInfo> replicas = new ArrayList<>(); // List of replica servers

  // Paxos Acceptor state
  private final AtomicLong promisedId = new AtomicLong(0); // Highest proposal ID promised
  private final AtomicLong acceptedId = new AtomicLong(0); // Highest proposal ID accepted
  private String acceptedOperation = null; // Operation type of accepted proposal
  private String acceptedKey = null; // Key of accepted proposal
  private String acceptedValue = null; // Value of accepted proposal

  // Track learned proposals
  private final Set<Long> learnedProposals = Collections.synchronizedSet(new HashSet<>());

  // Thread for simulating acceptor failures
  private Thread acceptorFailureSimulator;
  private final AtomicBoolean acceptorActive = new AtomicBoolean(true);

  // Thread for generating unique proposal IDs
  private AtomicLong nextProposalId;

  private Logger logger;

  public PaxosKVStore(int serverId) throws RemoteException {
    this.serverId = serverId;
    this.store = new KVStore();
    this.logger = new Logger(PaxosKVStore.class);
    this.nextProposalId = new AtomicLong(serverId);

    // Initialize replica information (excluding self)
    for (int i = 0; i < NUM_REPLICAS; i++) {
      if (i != serverId) {
        replicas.add(new ReplicaInfo(i, "localhost", 1099 + i));
      }
    }

    // Start the acceptor failure simulator
    startAcceptorFailureSimulator();
  }

  /**
   * Generate a new unique proposal ID.
   * The proposal ID is a combination of the current timestamp and server ID
   * to ensure uniqueness across all servers.
   */
  private long generateProposalId() {
    // Increment by NUM_REPLICAS to ensure uniqueness across servers
    return nextProposalId.getAndAdd(NUM_REPLICAS);
  }

  /**
   * Start a thread that simulates acceptor failures at random intervals
   */
  private void startAcceptorFailureSimulator() {
    acceptorFailureSimulator = new Thread(() -> {
      while (true) {
        try {
          // Random time before failure
          int failureTime = ACCEPTOR_FAILURE_MIN_TIME +
                  (int)(Math.random() * (ACCEPTOR_FAILURE_MAX_TIME - ACCEPTOR_FAILURE_MIN_TIME));
          Thread.sleep(failureTime);

          // Simulate acceptor failure
          acceptorActive.set(false);
          logger.log("Server " + serverId + " acceptor FAILED");

          // Wait before restarting
          Thread.sleep(ACCEPTOR_RESTART_TIME);

          // Restart acceptor
          acceptorActive.set(true);
          logger.log("Server " + serverId + " acceptor RESTARTED");
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }
    });
    acceptorFailureSimulator.setDaemon(true);
    acceptorFailureSimulator.start();
  }

  //==============================
  // KVStoreRMI Interface Methods
  //==============================

  @Override
  public String get(String key) throws RemoteException {
    return store.get(key);
  }

  @Override
  public String put(String key, String value) throws RemoteException {
    logger.log("Server " + serverId + " initiating Paxos for PUT: " + key + "=" + value);

    // Use Paxos to reach consensus
    boolean success = runPaxos(Protocol.PUT, key, value);

    return success ? "OK" : "ERROR: Failed to reach consensus";
  }

  @Override
  public String delete(String key) throws RemoteException {
    logger.log("Server " + serverId + " initiating Paxos for DELETE: " + key);

    // Use Paxos to reach consensus
    boolean success = runPaxos(Protocol.DELETE, key, null);

    return success ? "OK" : "ERROR: Failed to reach consensus";
  }

  //==============================
  // Paxos Algorithm Implementation
  //==============================

  /**
   * Run the Paxos algorithm to reach consensus on an operation
   */
  private boolean runPaxos(String operation, String key, String value) {
    int maxRetries = 3;
    for (int attempt = 0; attempt < maxRetries; attempt++) {
      try {
        // Generate a unique proposal ID
        long proposalId = generateProposalId();
        logger.log("Server " + serverId + " running Paxos with proposal ID " + proposalId);

        // Phase 1: Prepare
        Map<ReplicaInfo, PaxosResponse> prepareResponses = sendPrepare(proposalId);

        // Check if we got majority of promises
        if (prepareResponses.size() < MAJORITY) {
          logger.log("Failed to get majority promises for proposal " + proposalId + ", retrying...");
          Thread.sleep(100 * (attempt + 1)); // Backoff before retry
          continue;
        }

        // Check if any acceptor has already accepted a value
        PaxosResponse highestAcceptedResponse = null;
        for (PaxosResponse response : prepareResponses.values()) {
          if (response.hasAcceptedValue()) {
            if (highestAcceptedResponse == null ||
                    response.getAcceptedId() > highestAcceptedResponse.getAcceptedId()) {
              highestAcceptedResponse = response;
            }
          }
        }

        // Use the highest accepted value if exists, otherwise use our value
        String proposedOperation = operation;
        String proposedKey = key;
        String proposedValue = value;

        if (highestAcceptedResponse != null) {
          proposedOperation = highestAcceptedResponse.getAcceptedOperation();
          proposedKey = highestAcceptedResponse.getAcceptedKey();
          proposedValue = highestAcceptedResponse.getAcceptedValue();
          logger.log("Using previously accepted value for proposal " + proposalId);
        }

        // Phase 2: Accept
        Map<ReplicaInfo, PaxosResponse> acceptResponses =
                sendAccept(proposalId, proposedOperation, proposedKey, proposedValue);

        // Check if we got majority of accepts
        if (acceptResponses.size() < MAJORITY) {
          logger.log("Failed to get majority accepts for proposal " + proposalId + ", retrying...");
          Thread.sleep(100 * (attempt + 1)); // Backoff before retry
          continue;
        }

        // Phase 3: Learn
        sendLearn(proposalId, proposedOperation, proposedKey, proposedValue);

        // Apply the operation locally
        applyOperation(proposedOperation, proposedKey, proposedValue);

        logger.log("Paxos consensus reached for proposal " + proposalId);
        return true;

      } catch (Exception e) {
        logger.log("Error in Paxos round: " + e.getMessage());
      }
    }

    logger.log("Failed to reach consensus after " + maxRetries + " attempts");
    return false;
  }

  /**
   * Send prepare requests to all acceptors (Phase 1a)
   */
  private Map<ReplicaInfo, PaxosResponse> sendPrepare(long proposalId) {
    Map<ReplicaInfo, PaxosResponse> responses = new ConcurrentHashMap<>();
    CountDownLatch latch = new CountDownLatch(replicas.size());

    // Send prepare to self first
    try {
      PaxosResponse selfResponse = prepare(proposalId);
      if (selfResponse.isSuccess()) {
        responses.put(new ReplicaInfo(serverId, "localhost", 1099 + serverId), selfResponse);
      }
    } catch (Exception e) {
      logger.log("Error preparing self: " + e.getMessage());
    }

    // Send prepare to other replicas
    for (ReplicaInfo replica : replicas) {
      new Thread(() -> {
        try {
          Registry registry = LocateRegistry.getRegistry(replica.getHost(), replica.getPort());
          PaxosRMI remotePaxos = (PaxosRMI) registry.lookup("PaxosService" + replica.getId());

          // Check if replica is alive
          if (remotePaxos.isAlive()) {
            PaxosResponse response = remotePaxos.prepare(proposalId);
            if (response.isSuccess()) {
              responses.put(replica, response);
            }
          }
        } catch (Exception e) {
          logger.log("Error preparing replica " + replica.getId() + ": " + e.getMessage());
        } finally {
          latch.countDown();
        }
      }).start();
    }

    try {
      latch.await(TIMEOUT, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    logger.log("Received " + responses.size() + " promises for proposal " + proposalId);
    return responses;
  }

  /**
   * Send accept requests to all acceptors (Phase 2a)
   */
  private Map<ReplicaInfo, PaxosResponse> sendAccept(long proposalId, String operation, String key, String value) {
    Map<ReplicaInfo, PaxosResponse> responses = new ConcurrentHashMap<>();
    CountDownLatch latch = new CountDownLatch(replicas.size());

    // Send accept to self first
    try {
      PaxosResponse selfResponse = accept(proposalId, operation, key, value);
      if (selfResponse.isSuccess()) {
        responses.put(new ReplicaInfo(serverId, "localhost", 1099 + serverId), selfResponse);
      }
    } catch (Exception e) {
      logger.log("Error accepting self: " + e.getMessage());
    }

    // Send accept to other replicas
    for (ReplicaInfo replica : replicas) {
      new Thread(() -> {
        try {
          Registry registry = LocateRegistry.getRegistry(replica.getHost(), replica.getPort());
          PaxosRMI remotePaxos = (PaxosRMI) registry.lookup("PaxosService" + replica.getId());

          // Check if replica is alive
          if (remotePaxos.isAlive()) {
            PaxosResponse response = remotePaxos.accept(proposalId, operation, key, value);
            if (response.isSuccess()) {
              responses.put(replica, response);
            }
          }
        } catch (Exception e) {
          logger.log("Error accepting at replica " + replica.getId() + ": " + e.getMessage());
        } finally {
          latch.countDown();
        }
      }).start();
    }

    try {
      latch.await(TIMEOUT, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    logger.log("Received " + responses.size() + " accepts for proposal " + proposalId);
    return responses;
  }

  /**
   * Send learn notifications to all replicas (Phase 3)
   */
  private void sendLearn(long proposalId, String operation, String key, String value) {
    // Learn self first
    try {
      learn(proposalId, operation, key, value);
    } catch (Exception e) {
      logger.log("Error learning self: " + e.getMessage());
    }

    // Send learn to other replicas
    for (ReplicaInfo replica : replicas) {
      new Thread(() -> {
        try {
          Registry registry = LocateRegistry.getRegistry(replica.getHost(), replica.getPort());
          PaxosRMI remotePaxos = (PaxosRMI) registry.lookup("PaxosService" + replica.getId());

          // Check if replica is alive
          if (remotePaxos.isAlive()) {
            remotePaxos.learn(proposalId, operation, key, value);
          }
        } catch (Exception e) {
          logger.log("Error learning at replica " + replica.getId() + ": " + e.getMessage());
        }
      }).start();
    }
  }

  /**
   * Apply an operation to the local store
   */
  private synchronized void applyOperation(String operation, String key, String value) {
    if (operation.equals(Protocol.PUT)) {
      store.put(key, value);
      logger.log("Applied PUT operation: " + key + "=" + value);
    } else if (operation.equals(Protocol.DELETE)) {
      store.delete(key);
      logger.log("Applied DELETE operation: " + key);
    }
  }

  //==============================
  // PaxosRMI Interface Methods
  //==============================

  @Override
  public PaxosResponse prepare(long proposalId) throws RemoteException {
    // Simulate acceptor failure
    if (!acceptorActive.get()) {
      throw new RemoteException("Acceptor is currently down");
    }

    synchronized (this) {
      logger.log("Server " + serverId + " received PREPARE for proposal " + proposalId);

      // If we've already promised a higher proposal ID, reject
      if (proposalId < promisedId.get()) {
        logger.log("Rejecting PREPARE " + proposalId + " (promised: " + promisedId.get() + ")");
        return new PaxosResponse(false);
      }

      // Update the promised ID
      promisedId.set(proposalId);

      // Return a promise with the highest accepted proposal (if any)
      return new PaxosResponse(
              true,
              promisedId.get(),
              acceptedId.get(),
              acceptedOperation,
              acceptedKey,
              acceptedValue
      );
    }
  }

  @Override
  public PaxosResponse accept(long proposalId, String operation, String key, String value) throws RemoteException {
    // Simulate acceptor failure
    if (!acceptorActive.get()) {
      throw new RemoteException("Acceptor is currently down");
    }

    synchronized (this) {
      logger.log("Server " + serverId + " received ACCEPT for proposal " + proposalId);

      // If we've already promised a higher proposal ID, reject
      if (proposalId < promisedId.get()) {
        logger.log("Rejecting ACCEPT " + proposalId + " (promised: " + promisedId.get() + ")");
        return new PaxosResponse(false);
      }

      // Accept the proposal
      promisedId.set(proposalId);
      acceptedId.set(proposalId);
      acceptedOperation = operation;
      acceptedKey = key;
      acceptedValue = value;

      logger.log("Accepted proposal " + proposalId + ": " + operation + " " + key +
              (value != null ? "=" + value : ""));

      return new PaxosResponse(true);
    }
  }

  @Override
  public void learn(long proposalId, String operation, String key, String value) throws RemoteException {
    // No need to check if acceptor is active, as learning should always succeed

    synchronized (this) {
      // Check if we've already learned this proposal
      if (learnedProposals.contains(proposalId)) {
        return;
      }

      logger.log("Server " + serverId + " LEARNED proposal " + proposalId);

      // Mark as learned
      learnedProposals.add(proposalId);

      // Apply the operation to the local store
      applyOperation(operation, key, value);
    }
  }

  @Override
  public boolean isAlive() throws RemoteException {
    return true; // This method is always reachable
  }

  //==============================
  // Two-Phase Commit (Legacy Support)
  //==============================

  @Override
  public boolean prepare(String transactionId, String operation, String key, String value) throws RemoteException {
    logger.log("Legacy 2PC prepare received - not supported");
    return false;
  }

  @Override
  public boolean commit(String transactionId) throws RemoteException {
    logger.log("Legacy 2PC commit received - not supported");
    return false;
  }

  @Override
  public boolean abort(String transactionId) throws RemoteException {
    logger.log("Legacy 2PC abort received - not supported");
    return false;
  }

  //==============================
  // Server Shutdown
  //==============================

  /**
   * Shutdown the server and clean up resources
   */
  public void shutdown() {
    if (acceptorFailureSimulator != null) {
      acceptorFailureSimulator.interrupt();
    }
  }
}