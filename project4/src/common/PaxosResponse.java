// common/PaxosResponse.java
package common;

import java.io.Serializable;

/**
 * Response object for Paxos protocol operations
 * Contains the result of prepare and accept phases
 */
public class PaxosResponse implements Serializable {
  private static final long serialVersionUID = 1L;

  private final boolean success;
  private final long promisedId;
  private final long acceptedId;
  private final String acceptedOperation;
  private final String acceptedKey;
  private final String acceptedValue;

  /**
   * Constructor for a promise response
   * @param success Whether the operation succeeded
   * @param promisedId The ID this acceptor has promised not to accept proposals below
   * @param acceptedId The ID of the proposal this acceptor has already accepted (if any)
   * @param acceptedOperation The operation of the accepted proposal (if any)
   * @param acceptedKey The key of the accepted proposal (if any)
   * @param acceptedValue The value of the accepted proposal (if any)
   */
  public PaxosResponse(boolean success, long promisedId, long acceptedId,
                       String acceptedOperation, String acceptedKey, String acceptedValue) {
    this.success = success;
    this.promisedId = promisedId;
    this.acceptedId = acceptedId;
    this.acceptedOperation = acceptedOperation;
    this.acceptedKey = acceptedKey;
    this.acceptedValue = acceptedValue;
  }

  /**
   * Constructor for a simple success/failure response
   * @param success Whether the operation succeeded
   */
  public PaxosResponse(boolean success) {
    this(success, -1, -1, null, null, null);
  }

  public boolean isSuccess() {
    return success;
  }

  public long getPromisedId() {
    return promisedId;
  }

  public long getAcceptedId() {
    return acceptedId;
  }

  public String getAcceptedOperation() {
    return acceptedOperation;
  }

  public String getAcceptedKey() {
    return acceptedKey;
  }

  public String getAcceptedValue() {
    return acceptedValue;
  }

  public boolean hasAcceptedValue() {
    return acceptedId != -1 && acceptedOperation != null && acceptedKey != null;
  }
}