package common;

/**
 * 功能：定义客户端和服务器之间的通信协议（请求和响应的格式）。
 * Protocol class defines the communication protocol between client and server
 *  * It contains all valid commands, response types, and methods for formatting messages
 *  *
 *  * This class acts as a central place for all protocol-related constants and utilities,
 *  * ensuring consistency in client-server communication
 */

public class Protocol {
  // Request operation types
  // Request operation types
  public static final String PUT = "PUT";       // Store a key-value pair
  public static final String GET = "GET";       // Retrieve a value by key
  public static final String DELETE = "DELETE"; // Remove a key-value pair

  // Server response types for PUT operations
  public static final String PUT_OK = "PUT_OK";         // Successful storage
  public static final String PUT_ERROR = "PUT_ERROR";   // Failed to store

  // Server response types for GET operations
  public static final String GET_RESULT = "GET_RESULT"; // Successful retrieval with value
  public static final String GET_ERROR = "GET_ERROR";   // Key not found

  // Server response types for DELETE operations
  public static final String DELETE_OK = "DELETE_OK";       // Successful deletion
  public static final String DELETE_ERROR = "DELETE_ERROR"; // Failed to delete/key not found

  /**
   * Creates a properly formatted request string based on the operation type
   *
   * Format for PUT: "PUT key value"
   * Format for GET/DELETE: "operation key"
   *
   * @param operation The type of operation (PUT, GET, or DELETE)
   * @param key The key to operate on
   * @param value The value to store (only used for PUT operations)
   * @return Formatted request string
   *
   * Example usages:
   * createRequest(PUT, "name", "John") -> "PUT name John"
   * createRequest(GET, "name", null) -> "GET name"
   * createRequest(DELETE, "name", null) -> "DELETE name"
   */
  public static String createRequest(String operation, String key, String value) {
    // For PUT operations, include the value
    if (operation.equals(PUT)) {
      return operation + " " + key + " " + value;
    }
    // For GET and DELETE operations, only include the key
    else {
      return operation + " " + key;
    }
  }

  /**
   * Parses a server response string into its components
   * Splits the response into status and optional value
   *
   * @param response The complete response string from server
   * @return String array where:
   *         - index 0: response type (e.g., PUT_OK, GET_RESULT)
   *         - index 1: value (if present)
   *
   * Example:
   * parseResponse("GET_RESULT John") -> ["GET_RESULT", "John"]
   * parseResponse("PUT_OK") -> ["PUT_OK"]
   *
   * Note: Splits only on first space to keep value intact if it contains spaces
   */
  public static String[] parseResponse(String response) {
    // Split on first space only (limit=2)
    // This ensures values containing spaces stay together
    return response.split(" ", 2);
  }
}


