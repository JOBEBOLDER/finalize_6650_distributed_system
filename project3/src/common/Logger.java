package common;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple logging utility class that provides formatted logging functionality
 * This logger adds timestamps and class names to log messages for better debugging
 *
 * Usage example:
 * Logger logger = new Logger(MyClass.class);
 * logger.log("Something happened");
 * Output: [2025-01-31 12:34:56.789] [MyClass] Something happened
 */
public class Logger {

  // Stores the name of the class that's doing the logging
  // Using just the simple name (without package) for cleaner output
  private static String className;

  /**
   * Constructor that takes a Class object to identify the source of log messages
   * Uses Java's reflection (Class<?>) to get the class name
   * The <?> is a wildcard generic type, meaning it can accept any class
   *
   * @param clazz The Class object of the class using this logger
   */
  public Logger(Class<?> clazz) {
    this.className = clazz.getSimpleName();
  }

  /**
   * Logs a message with the current timestamp and class name
   * Format: [timestamp] [className] message
   *
   * @param message The message to be logged
   */
  public static void log(String message) {
    // Create date formatter for timestamp
    // Pattern: yyyy-MM-dd HH:mm:ss.SSS
    // Example: 2025-01-31 12:34:56.789

    //get the current timestamp
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    //print the formatted log message
    // format:[timestamp] [className] message
    String timeStamp = sdf.format(new Date());
    System.out.println("[" + timeStamp + "] [" + className + "]" + message );


  }
}
