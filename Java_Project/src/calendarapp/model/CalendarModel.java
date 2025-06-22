package calendarapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface for a calendar model that manages events and recurring series.
 */
public interface CalendarModel {

  void addEvent(Event event);

  void addEventSeries(EventSeries series);

  List<Event> getEventsOn(LocalDate date);

  List<Event> getEventsBetween(LocalDateTime from, LocalDateTime to);

  boolean isBusyAt(LocalDateTime dateTime);

  void editEvent(EventId id, Event newEvent);

  void editEventsFrom(EventId startingId, String property, String newValue);

  void editSeries(String subject, LocalDateTime originalStart,
                  String property, String newValue);

  Event getEventById(EventId id);

  List<Event> getEventsFrom(LocalDate start, int maxCount);

  void deleteEvent(EventId id);

}
