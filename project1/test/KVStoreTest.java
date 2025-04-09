import kvstore.KVStore;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Test suite for KVStore class
 * Tests basic operations, edge cases, and concurrent access
 */
public class KVStoreTest {

  /**
   * Clear any existing data before each test
   * Ensures tests start with an empty store
   */
  @Before
  public void setUp() {
    // Clear store by putting and deleting a dummy value
    KVStore.put("dummy", "dummy");
    KVStore.delete("dummy");
  }

  /**
   * Test basic PUT operation
   * Verifies successful storage of key-value pairs
   */
  @Test
  public void testBasicPut() {
    // Test single put operation
    assertEquals("PUT_OK", KVStore.put("key1", "value1"));

    // Verify put was successful using get
    assertEquals("GET_RESULT value1", KVStore.get("key1"));
  }

  /**
   * Test PUT operation with value updates
   * Verifies that putting with existing key updates the value
   */
  @Test
  public void testPutUpdate() {
    // Put initial value
    KVStore.put("updateKey", "value1");
    assertEquals("GET_RESULT value1", KVStore.get("updateKey"));

    // Update value
    KVStore.put("updateKey", "value2");
    assertEquals("GET_RESULT value2", KVStore.get("updateKey"));
  }

  /**
   * Test basic GET operation
   * Verifies retrieval of stored values
   */
  @Test
  public void testBasicGet() {
    // Test get on non-existent key
    assertEquals("GET_ERROR", KVStore.get("nonexistentKey"));

    // Test get after putting a value
    KVStore.put("getKey", "getValue");
    assertEquals("GET_RESULT getValue", KVStore.get("getKey"));
  }

  /**
   * Test basic DELETE operation
   * Verifies removal of key-value pairs
   */
  @Test
  public void testBasicDelete() {
    // Test delete on non-existent key
    assertEquals("DELETE_ERROR", KVStore.delete("nonexistentKey"));

    // Test delete after putting a value
    KVStore.put("deleteKey", "deleteValue");
    assertEquals("DELETE_OK", KVStore.delete("deleteKey"));
    assertEquals("GET_ERROR", KVStore.get("deleteKey"));
  }

  /**
   * Test edge cases with null values
   * Verifies proper handling of null inputs
   */
  @Test
  public void testNullHandling() {
    // Test put with null value
    assertEquals("PUT_OK", KVStore.put("nullKey", null));

    // Test get with null key (should return error)
    assertEquals("GET_ERROR", KVStore.get(null));

    // Test delete with null key
    assertEquals("DELETE_ERROR", KVStore.delete(null));
  }

  /**
   * Test empty string handling
   * Verifies proper handling of empty string inputs
   */
  @Test
  public void testEmptyStrings() {
    // Test put with empty strings
    assertEquals("PUT_OK", KVStore.put("", ""));
    assertEquals("GET_RESULT ", KVStore.get(""));
    assertEquals("DELETE_OK", KVStore.delete(""));
  }

  /**
   * Test special character handling
   * Verifies proper handling of special characters in keys and values
   */
  @Test
  public void testSpecialCharacters() {
    String specialKey = "!@#$%^&*()_+";
    String specialValue = "~`-=[]\\{}|;':\",./<>?";

    assertEquals("PUT_OK", KVStore.put(specialKey, specialValue));
    assertEquals("GET_RESULT " + specialValue, KVStore.get(specialKey));
    assertEquals("DELETE_OK", KVStore.delete(specialKey));
  }

  /**
   * Test concurrent operations
   * Verifies thread-safety of operations
   */
  @Test
  public void testConcurrentAccess() throws InterruptedException {
    int numThreads = 100;
    CountDownLatch latch = new CountDownLatch(numThreads);
    ExecutorService executor = Executors.newFixedThreadPool(numThreads);

    for (int i = 0; i < numThreads; i++) {
      final int threadId = i;
      executor.submit(() -> {
        try {
          // Each thread performs a sequence of operations
          String key = "key" + threadId;
          String value = "value" + threadId;

          // Put
          assertEquals("PUT_OK", KVStore.put(key, value));

          // Get
          assertEquals("GET_RESULT " + value, KVStore.get(key));

          // Delete
          assertEquals("DELETE_OK", KVStore.delete(key));

          // Verify deletion
          assertEquals("GET_ERROR", KVStore.get(key));
        } finally {
          latch.countDown();
        }
      });
    }

    assertTrue("Concurrent operations did not complete in time",
            latch.await(30, TimeUnit.SECONDS));
    executor.shutdown();
  }

  /**
   * Test operation sequence
   * Verifies correct behavior across a sequence of operations
   */
  @Test
  public void testOperationSequence() {
    String key = "sequenceKey";
    String value1 = "value1";
    String value2 = "value2";

    // Initial put
    assertEquals("PUT_OK", KVStore.put(key, value1));
    assertEquals("GET_RESULT " + value1, KVStore.get(key));

    // Update existing key
    assertEquals("PUT_OK", KVStore.put(key, value2));
    assertEquals("GET_RESULT " + value2, KVStore.get(key));

    // Delete
    assertEquals("DELETE_OK", KVStore.delete(key));
    assertEquals("GET_ERROR", KVStore.get(key));

    // Put after delete
    assertEquals("PUT_OK", KVStore.put(key, value1));
    assertEquals("GET_RESULT " + value1, KVStore.get(key));
  }

  /**
   * Test large key-value pairs
   * Verifies handling of large strings
   */
  @Test
  public void testLargeValues() {
    StringBuilder largeValue = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      largeValue.append("large-value-test-");
    }

    String key = "largeKey";
    String value = largeValue.toString();

    assertEquals("PUT_OK", KVStore.put(key, value));
    assertEquals("GET_RESULT " + value, KVStore.get(key));
    assertEquals("DELETE_OK", KVStore.delete(key));
  }
}