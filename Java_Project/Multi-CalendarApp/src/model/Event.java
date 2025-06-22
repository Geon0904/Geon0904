package calendarapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;


/**
 * Represents an immutable calendar event with optional details.
 */

public class Event {
  private final String subject;
  private final LocalDateTime start;
  private final LocalDateTime end;

  private final String description;
  private final String location;
  private final boolean isPublic;

  private static final LocalTime DEFAULT_START_TIME = LocalTime.of(8, 0);
  private static final LocalTime DEFAULT_END_TIME = LocalTime.of(17, 0);


  /**
   * Constructs an all day event.
   * From 8:00 AM to 5:00 PM on the given date.
   */
  public Event(String subject, LocalDateTime start, LocalDateTime end) {
    this(subject, start, end, "", "", true);
  }


  /**
   * Constructs a fully customized event.
   * If end is null, the event is treated as all-day.
   */
  public Event(String subject, LocalDateTime start, LocalDateTime end,
               String description, String location, boolean isPublic) {

    if (subject == null || subject.isBlank()) {
      throw new IllegalArgumentException("subject must not be empty");
    }

    if (start == null) {
      throw new IllegalArgumentException("start time must not be null");
    }

    if (end == null) {
      LocalDate date = start.toLocalDate();

      this.start = LocalDateTime.of(date, DEFAULT_START_TIME);
      this.end = LocalDateTime.of(date, DEFAULT_END_TIME);

    } else {
      if (!end.isAfter(start)) {
        throw new IllegalArgumentException("end time must be after start time");
      }

      this.start = start;
      this.end = end;
    }

    this.subject = subject;
    this.description = description == null ? "" : description;
    this.location = location == null ? "" : location;
    this.isPublic = isPublic;
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


  public String getDescription() {
    return description;
  }


  public String getLocation() {
    return location;
  }


  public boolean isPublic() {
    return isPublic;
  }


  public boolean isAllDay() {
    return start.toLocalTime().equals(DEFAULT_START_TIME)
            && end.toLocalTime().equals(DEFAULT_END_TIME);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(subject)
            .append(": ")
            .append(start)
            .append(" to ")
            .append(end);

    if (!location.isEmpty()) {
      sb.append(" @ ").append(location);
    }

    if (!description.isEmpty()) {
      sb.append(" - ").append(description);
    }

    sb.append(" [").append(isPublic ? "public" : "private").append("]");
    return sb.toString();
  }


  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    Event e = (Event) obj;
    return subject.equals(e.subject)
            && start.equals(e.start)
            && end.equals(e.end)
            && description.equals(e.description)
            && location.equals(e.location)
            && isPublic == e.isPublic;
  }


  @Override
  public int hashCode() {
    return Objects.hash(subject, start, end, description, location, isPublic);
  }


}

