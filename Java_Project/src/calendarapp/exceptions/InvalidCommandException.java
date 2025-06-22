package calendarapp.exceptions;

/**
 * Thrown when the input command is malformed or not supported.
 */

public class InvalidCommandException extends RuntimeException {
  public InvalidCommandException(String message) {
    super(message);
  }
}
