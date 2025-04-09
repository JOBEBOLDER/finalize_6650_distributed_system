// ReplicaInfo.java
package server;

import java.io.Serializable;

/**
 * store the server info
 */
public class ReplicaInfo implements Serializable {
  private final int id;
  private final String host;
  private final int port;

  public ReplicaInfo(int id, String host, int port) {
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