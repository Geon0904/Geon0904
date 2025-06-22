package calendarapp.model;

import java.time.LocalDateTime;
import java.util.Objects;


/**
 * Unique ID of the identifiable event.
 * If the subject, start, and end are the same, considered the same event.
 */

public class EventId {
  private final String subject;
  private final LocalDateTime start;
  private final LocalDateTime end;

  /**
   * Creates a new event identifier.
   *
   * @param subject event title
   * @param start   start date-time
   * @param end     end date-time
   */

  public EventId(String subject, LocalDateTime start, LocalDateTime end) {

    this.subject = Objects.requireNonNull(subject, "Subject must not be null");
    this.start = Objects.requireNonNull(start, "Start time must not be null");
    this.end = Objects.requireNonNull(end, "End time must not be null");
  }


  public String getSubject() {
    return subject;
  }

  public LocalDateTime getStart() {
    return start;
  }

  public LocalDateTime getEnd() {
    return end;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof EventId)) {
      return false;
    }

    EventId eventId = (EventId) o;
    return subject.equals(eventId.subject)
            && start.equals(eventId.start)
            && end.equals(eventId.end);
  }


  @Override
  public int hashCode() {
    return Objects.hash(subject, start, end);
  }


  @Override
  public String toString() {
    return "\"" + subject + "\" from " + start + " to " + end;
  }
}
