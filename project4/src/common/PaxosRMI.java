// common/PaxosRMI.java
package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface for Paxos consensus protocol operations
 */
public interface PaxosRMI extends Remote {
  /**
   * Phase 1a: Proposer sends a prepare request to acceptors
   * @param proposalId The proposal ID (must be unique and higher than any previous proposal)
   * @return Promise response from the acceptor
   * @throws RemoteException if a remote error occurs
   */
  PaxosResponse prepare(long proposalId) throws RemoteException;

  /**
   * Phase 2a: Proposer sends an accept request to acceptors
   * @param proposalId The proposal ID
   * @param operation The operation type (PUT, GET, DELETE)
   * @param key The key involved in the operation
   * @param value The value (for PUT operations)
   * @return Accept response from the acceptor
   * @throws RemoteException if a remote error occurs
   */
  PaxosResponse accept(long proposalId, String operation, String key, String value) throws RemoteException;

  /**
   * Phase 3: Learner learns the accepted value
   * @param proposalId The proposal ID
   * @param operation The operation type
   * @param key The key involved in the operation
   * @param value The value (for PUT operations)
   * @throws RemoteException if a remote error occurs
   */
  void learn(long proposalId, String operation, String key, String value) throws RemoteException;

  /**
   * Check if this node is alive
   * @return True if the node is alive
   * @throws RemoteException if a remote error occurs
   */
  boolean isAlive() throws RemoteException;
}