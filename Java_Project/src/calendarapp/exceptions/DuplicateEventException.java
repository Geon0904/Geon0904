package calendarapp.exceptions;

/**
 * Thrown when an event with the same subject, start, and end already exists.
 */
public class DuplicateEventException extends RuntimeException {
  public DuplicateEventException(String message) {
    super(message);
  }
}
