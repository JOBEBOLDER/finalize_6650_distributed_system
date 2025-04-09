package kvstore;

import java.util.HashMap;
import java.util.Map;

/**
 * KVStore (Key-Value Store) Implementation
 * A simple in-memory key-value storage system that provides basic CRUD operations
 * (without the Update operation, which is handled as a Put)
 *
 * This class serves as the core data storage component for the client-server application,
 * providing thread-safe operations through HashMap's implementation
 */
public class KVStore {
    // The actual storage container using Java's HashMap
    // Key and Value are both Strings for simplicity
    // HashMap provides O(1) average case complexity for all operations
    private static final Map<String, String> store = new HashMap<>();

    /**
     * Stores a key-value pair in the store
     * If the key already exists, its value will be updated
     *
     * @param key The key under which to store the value
     * @param value The value to be stored
     * @return "PUT_OK" to indicate successful storage
     *
     * Time Complexity: O(1) average case
     * Thread Safety: put() is thread-safe in HashMap
     */
    public static String put(String key, String value) {
        store.put(key, value);
        return "PUT_OK";
    }

    /**
     * Retrieves a value by its key
     *
     * @param key The key whose value should be retrieved
     * @return "GET_RESULT value" if key exists, "GET_ERROR" if key not found
     *
     * Time Complexity: O(1) average case
     * Thread Safety: get() is thread-safe in HashMap
     *
     * Example returns:
     * - Key exists: "GET_RESULT someValue"
     * - Key doesn't exist: "GET_ERROR"
     */
    public static String get(String key) {
        String value = store.get(key);
        return value != null ? "GET_RESULT " + value : "GET_ERROR";
    }

    /**
     * Removes a key-value pair from the store
     *
     * @param key The key to be removed
     * @return "DELETE_OK" if key was found and removed, "DELETE_ERROR" if key not found
     *
     * Time Complexity: O(1) average case
     * Thread Safety: remove() is thread-safe in HashMap
     */
    public static String delete(String key) {
        return store.remove(key) != null ? "DELETE_OK" : "DELETE_ERROR";
    }
}