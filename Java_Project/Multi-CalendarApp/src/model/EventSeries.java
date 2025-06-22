package calendarapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a recurring series of calendar events.
 * All events in the series must start and end on the same day.
 */
public class EventSeries {
  private final String subject;
  private final LocalDateTime startTime;
  private final LocalDateTime endTime;
  private final String description;
  private final String location;
  private final boolean isPublic;
  private final RecurrenceRule rule;
  private final List<Event> events;

  /**
   * Constructs a recurring series of events using the given recurrence rule.
   */
  public EventSeries(String subject,
                     LocalDateTime startTime,
                     LocalDateTime endTime,
                     String description,
                     String location,
                     boolean isPublic,
                     RecurrenceRule rule) {

    if (startTime == null || endTime == null
            || !startTime.toLocalDate().equals(endTime.toLocalDate())) {
      throw new IllegalArgumentException("Events in a series must start and end on the same day.");
    }

    this.subject = subject;
    this.startTime = startTime;
    this.endTime = endTime;
    this.description = description;
    this.location = location;
    this.isPublic = isPublic;
    this.rule = rule;
    this.events = generateEvents();
  }

  /**
   * Additional constructor to support (LocalDate + LocalTime) input.
   */
  public EventSeries(String subject,
                     LocalTime startTime,
                     LocalTime endTime,
                     LocalDate date,
                     RecurrenceRule rule) {
    this(subject,
            LocalDateTime.of(date, startTime),
            LocalDateTime.of(date, endTime),
            "",
            "",
            true,
            rule);
  }


  /**
   * Generates the full list of repeated Event objects.
   */
  private List<Event> generateEvents() {

    List<LocalDateTime> startDates = rule.generateOccurrences(startTime);


    List<Event> list = new ArrayList<>();
    for (LocalDateTime occStart : startDates) {

      LocalDateTime occEnd = occStart.withHour(endTime.getHour())
              .withMinute(endTime.getMinute());
      list.add(new Event(subject, occStart, occEnd, description, location, isPublic));
    }

    return Collections.unmodifiableList(list);
  }

  /**
   * Returns the list of events in this series.
   */
  public List<Event> getEvents() {
    return events;
  }

  public RecurrenceRule getRule() {
    return rule;
  }

  public String getSubject() {
    return subject;
  }

  public LocalDateTime getStartTime() {
    return startTime;
  }

  public LocalDateTime getEndTime() {
    return endTime;
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

  @Override
  public String toString() {
    return String.format("Series: \"%s\" (%d events)", subject, events.size());
  }
}
