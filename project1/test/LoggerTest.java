import common.Logger;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Test suite for the Logger class
 * Tests logging functionality, timestamp formatting, and message output
 */
public class LoggerTest {
  // Capture system output for testing
  private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private Logger logger;

  /**
   * Set up test environment before each test
   * Redirects System.out to capture logger output
   */
  @Before
  public void setUp() {
    System.setOut(new PrintStream(outputStream));
    logger = new Logger(LoggerTest.class);
  }

  /**
   * Clean up after each test
   * Restores original System.out
   */
  @After
  public void tearDown() {
    System.setOut(originalOut);
    outputStream.reset();
  }

  /**
   * Test basic logging functionality
   * Verifies that log messages contain all required components
   */
  @Test
  public void testBasicLogging() {
    String testMessage = "Test message";
    logger.log(testMessage);
    String output = outputStream.toString().trim();

    // Verify output format: [timestamp] [className] message
    assertTrue("Log output should start with timestamp bracket",
            output.startsWith("["));
    assertTrue("Log output should contain class name",
            output.contains("[LoggerTest]"));
    assertTrue("Log output should contain test message",
            output.contains(testMessage));
  }

  /**
   * Test timestamp format
   * Verifies that timestamp follows the required pattern
   */
  @Test
  public void testTimestampFormat() {
    logger.log("Timestamp test");
    String output = outputStream.toString().trim();

    // Extract timestamp from log output
    String timestamp = output.substring(1, output.indexOf("]"));

    // Define expected timestamp pattern
    String timestampPattern = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}";

    assertTrue("Timestamp should match expected format",
            Pattern.matches(timestampPattern, timestamp));
  }

  /**
   * Test logging with empty message
   * Verifies that logger handles empty messages correctly
   */
  @Test
  public void testEmptyMessage() {
    logger.log("");
    String output = outputStream.toString().trim();

    // Verify basic format is maintained even with empty message
    assertTrue("Log format should be maintained with empty message",
            output.matches("\\[.*\\] \\[LoggerTest\\]"));
  }

  /**
   * Test logging with null message
   * Verifies that logger handles null messages gracefully
   */
  @Test
  public void testNullMessage() {
    logger.log(null);
    String output = outputStream.toString().trim();

    // Verify logger doesn't throw exception and maintains format
    assertTrue("Log should contain class name even with null message",
            output.contains("[LoggerTest]"));
  }

  /**
   * Test logging from different classes
   * Verifies that class names are correctly displayed
   */
  @Test
  public void testDifferentClasses() {
    // Create loggers for different classes
    Logger stringLogger = new Logger(String.class);
    Logger integerLogger = new Logger(Integer.class);

    stringLogger.log("String log");
    String stringOutput = outputStream.toString().trim();
    outputStream.reset();

    integerLogger.log("Integer log");
    String integerOutput = outputStream.toString().trim();

    assertTrue("Should contain String class name",
            stringOutput.contains("[String]"));
    assertTrue("Should contain Integer class name",
            integerOutput.contains("[Integer]"));
  }

  /**
   * Test logging with special characters
   * Verifies that logger handles special characters correctly
   */
  @Test
  public void testSpecialCharacters() {
    String specialChars = "!@#$%^&*()_+-=[]{}|;:'\",.<>?/\\";
    logger.log(specialChars);
    String output = outputStream.toString().trim();

    assertTrue("Log should contain all special characters",
            output.contains(specialChars));
  }

  /**
   * Test timestamp accuracy
   * Verifies that logger timestamp is close to current time
   */
  @Test
  public void testTimestampAccuracy() {
    long beforeLog = System.currentTimeMillis();
    logger.log("Timestamp accuracy test");
    long afterLog = System.currentTimeMillis();

    String output = outputStream.toString().trim();
    String timestamp = output.substring(1, output.indexOf("]"));

    try {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
      long logTime = sdf.parse(timestamp).getTime();

      assertTrue("Log timestamp should be between before and after times",
              logTime >= beforeLog && logTime <= afterLog);
    } catch (Exception e) {
      fail("Failed to parse timestamp: " + e.getMessage());
    }
  }

  /**
   * Test long message handling
   * Verifies that logger handles long messages correctly
   */
  @Test
  public void testLongMessage() {
    StringBuilder longMessage = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longMessage.append("very long message ");
    }

    logger.log(longMessage.toString());
    String output = outputStream.toString().trim();

    assertTrue("Log should contain entire long message",
            output.contains(longMessage));
  }
}