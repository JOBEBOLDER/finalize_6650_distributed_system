import common.Protocol;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for Protocol class
 * Verifies request creation, response parsing, and protocol constants
 */
public class ProtocolTest {

  /**
   * Test createRequest method for PUT operations
   * Verifies proper formatting of PUT requests
   */
  @Test
  public void testCreatePutRequest() {
    // Test normal PUT request
    String request = Protocol.createRequest(Protocol.PUT, "testKey", "testValue");
    assertEquals("PUT testKey testValue", request);

    // Test PUT with spaces in value
    request = Protocol.createRequest(Protocol.PUT, "key", "value with spaces");
    assertEquals("PUT key value with spaces", request);

    // Test PUT with special characters
    request = Protocol.createRequest(Protocol.PUT, "key!@#", "value$%^");
    assertEquals("PUT key!@# value$%^", request);
  }

  /**
   * Test createRequest method for GET operations
   * Verifies proper formatting of GET requests
   */
  @Test
  public void testCreateGetRequest() {
    // Test normal GET request
    String request = Protocol.createRequest(Protocol.GET, "testKey", null);
    assertEquals("GET testKey", request);

    // Test GET with special characters in key
    request = Protocol.createRequest(Protocol.GET, "key!@#", null);
    assertEquals("GET key!@#", request);
  }

  /**
   * Test createRequest method for DELETE operations
   * Verifies proper formatting of DELETE requests
   */
  @Test
  public void testCreateDeleteRequest() {
    // Test normal DELETE request
    String request = Protocol.createRequest(Protocol.DELETE, "testKey", null);
    assertEquals("DELETE testKey", request);

    // Test DELETE with special characters in key
    request = Protocol.createRequest(Protocol.DELETE, "key!@#", null);
    assertEquals("DELETE key!@#", request);
  }

  /**
   * Test parseResponse method for GET responses
   * Verifies proper parsing of GET responses
   */
  @Test
  public void testParseGetResponse() {
    // Test successful GET response
    String[] parts = Protocol.parseResponse("GET_RESULT testValue");
    assertEquals(2, parts.length);
    assertEquals("GET_RESULT", parts[0]);
    assertEquals("testValue", parts[1]);

    // Test GET error response
    parts = Protocol.parseResponse("GET_ERROR");
    assertEquals(1, parts.length);
    assertEquals("GET_ERROR", parts[0]);

    // Test GET response with spaces in value
    parts = Protocol.parseResponse("GET_RESULT value with multiple spaces");
    assertEquals(2, parts.length);
    assertEquals("GET_RESULT", parts[0]);
    assertEquals("value with multiple spaces", parts[1]);
  }

  /**
   * Test parseResponse method for PUT responses
   * Verifies proper parsing of PUT responses
   */
  @Test
  public void testParsePutResponse() {
    // Test successful PUT response
    String[] parts = Protocol.parseResponse("PUT_OK");
    assertEquals(1, parts.length);
    assertEquals("PUT_OK", parts[0]);

    // Test PUT error response
    parts = Protocol.parseResponse("PUT_ERROR");
    assertEquals(1, parts.length);
    assertEquals("PUT_ERROR", parts[0]);
  }

  /**
   * Test parseResponse method for DELETE responses
   * Verifies proper parsing of DELETE responses
   */
  @Test
  public void testParseDeleteResponse() {
    // Test successful DELETE response
    String[] parts = Protocol.parseResponse("DELETE_OK");
    assertEquals(1, parts.length);
    assertEquals("DELETE_OK", parts[0]);

    // Test DELETE error response
    parts = Protocol.parseResponse("DELETE_ERROR");
    assertEquals(1, parts.length);
    assertEquals("DELETE_ERROR", parts[0]);
  }

  /**
   * Test protocol constants consistency
   * Verifies that protocol constants match expected values
   */
  @Test
  public void testProtocolConstants() {
    // Test operation constants
    assertEquals("PUT", Protocol.PUT);
    assertEquals("GET", Protocol.GET);
    assertEquals("DELETE", Protocol.DELETE);

    // Test response constants
    assertEquals("PUT_OK", Protocol.PUT_OK);
    assertEquals("PUT_ERROR", Protocol.PUT_ERROR);
    assertEquals("GET_RESULT", Protocol.GET_RESULT);
    assertEquals("GET_ERROR", Protocol.GET_ERROR);
    assertEquals("DELETE_OK", Protocol.DELETE_OK);
    assertEquals("DELETE_ERROR", Protocol.DELETE_ERROR);
  }

  /**
   * Test edge cases in request creation and response parsing
   * Verifies handling of edge cases and potential error conditions
   */
  @Test
  public void testEdgeCases() {
    // Test empty key and value
    String request = Protocol.createRequest(Protocol.PUT, "", "");
    assertEquals("PUT  ", request);

    // Test response with multiple spaces
    String[] parts = Protocol.parseResponse("GET_RESULT     multiple    spaces");
    assertEquals(2, parts.length);
    assertEquals("GET_RESULT", parts[0]);
    assertEquals("    multiple    spaces", parts[1]);

    // Test response with leading/trailing spaces
    parts = Protocol.parseResponse("  GET_RESULT   ");
    assertEquals(2, parts.length);
    assertEquals("GET_RESULT", parts[1].trim());
  }
}