package calendarapp.exceptions;

/**
 * Thrown when a command fails during execution.
 */

public class CommandExecutionException extends RuntimeException {
  public CommandExecutionException(String message) {
    super(message);
  }
}
