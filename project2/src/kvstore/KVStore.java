package kvstore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe Key-Value Store Implementation
 * Uses ConcurrentHashMap to ensure thread safety for concurrent operations
 */
public class KVStore {
  // Using ConcurrentHashMap instead of HashMap for thread safety
  private static final Map<String, String> store = new ConcurrentHashMap<>();

  /**
   * Stores a key-value pair in the store
   * Thread-safe operation guaranteed by ConcurrentHashMap
   *
   * @param key The key under which to store the value
   * @param value The value to be stored
   * @return "PUT_OK" to indicate successful storage
   */
  public static String put(String key, String value) {
    store.put(key,value);
    return "PUT_OK";
  }

  public static String get(String key) {
    String value = store.get(key);
    return value!= null ? "GET_RESULT " + value : "GET_ERROR";
  }

  /**
   * Removes a key-value pair from the store
   * Thread-safe operation guaranteed by ConcurrentHashMap
   *
   * @param key The key to be removed
   * @return "DELETE_OK" if key was found and removed, "DELETE_ERROR" if key not found
   */
  public static String delete(String key) {
    return store.remove(key) != null ? "DELETE_OK" : "DELETE_ERROR";
  }

  /**
   * Get current store size (for testing)
   * @return current number of keys in the store
   */
  public static int size() {
    return store.size();
  }

  /**
   * Clear all entries (for testing)
   */
  public static void clear() {
    store.clear();
  }

}
