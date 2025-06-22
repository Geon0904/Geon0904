package calendarapp.model;

import calendarapp.exceptions.DuplicateEventException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;


/**
 * Concrete implementation of the CalendarModel interface.
 * Each instance represents a single calendar with its own timezone
 * and set of events.
 */
public class CalendarImpl implements CalendarModel {

  private ZoneId timezone;
  private final Map<EventId, Event> eventsById;
  private final Map<LocalDate, List<Event>> eventsByDate;
  private final List<EventSeries> seriesList;

  /**
   * Constructs a CalendarImpl using the default timezone (EST).
   */
  public CalendarImpl() {
    this(ZoneId.of("America/New_York"));
  }

  /**
   * Constructs a CalendarImpl with the specified timezone.
   *
   * @param timezone the IANA time zone for this calendar
   */
  public CalendarImpl(ZoneId timezone) {
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone must not be null");
    }
    this.timezone = timezone;
    this.eventsById = new HashMap<>();
    this.eventsByDate = new HashMap<>();
    this.seriesList = new ArrayList<>();
  }

  /**
   * Changes the timezone of this calendar.
   *
   * @param newTz the new IANA timezone
   */
  public void setTimezone(ZoneId newTz) {
    if (newTz == null) {
      throw new IllegalArgumentException("Timezone must not be null");
    }
    this.timezone = newTz;
  }

  /**
   * Adds a single event to the calendar.
   *
   * @param event the Event to add
   * @throws DuplicateEventException if an event with the same EventId already exists
   */
  @Override
  public void addEvent(Event event) {
    EventId id = new EventId(event.getSubject(), event.getStart(), event.getEnd());
    if (eventsById.containsKey(id)) {
      throw new DuplicateEventException(
              String.format("Event '%s' already exists.", event.getSubject())
      );
    }

    eventsById.put(id, event);
    LocalDate startDate = event.getStart().toLocalDate();
    LocalDate endDate = event.getEnd().toLocalDate();
    for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
      eventsByDate.computeIfAbsent(d, k -> new ArrayList<>()).add(event);
    }
    for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
      List<Event> dayList = eventsByDate.get(d);
      dayList.sort(Comparator.comparing(Event::getStart));
    }
  }


  /**
   * Adds a recurring series of events to the calendar.
   *
   * @param series the EventSeries to add
   * @throws DuplicateEventException if any occurrence duplicates an existing event
   */
  @Override
  public void addEventSeries(EventSeries series) {
    List<Event> added = new ArrayList<>();
    try {
      for (Event e : series.getEvents()) {
        addEvent(e);
        added.add(e);
      }
      seriesList.add(series);
    } catch (DuplicateEventException ex) {

      for (Event e : added) {
        //
      }
      throw ex;
    }
  }


  /**
   * Retrieves all events occurring on the specified date, sorted by start time.
   *
   * @param date the LocalDate to query
   * @return a new list of Events on that date
   */
  @Override
  public List<Event> getEventsOn(LocalDate date) {
    List<Event> list = eventsByDate.getOrDefault(date, Collections.emptyList());
    return new ArrayList<>(list);
  }

  /**
   * Retrieves all events that overlap with the given datetime interval.
   *
   * @param start the start of the interval
   * @param end   the end of the interval
   * @return a list of overlapping Events, sorted by start time
   */
  @Override
  public List<Event> getEventsBetween(LocalDateTime start, LocalDateTime end) {
    List<Event> result = new ArrayList<>();
    LocalDate currDate = start.toLocalDate();
    LocalDate endDate = end.toLocalDate();
    while (!currDate.isAfter(endDate)) {
      List<Event> dayList = eventsByDate.getOrDefault(currDate, Collections.emptyList());
      for (Event e : dayList) {
        if (!(e.getEnd().isBefore(start) || e.getStart().isAfter(end))) {
          result.add(e);
        }
      }
      currDate = currDate.plusDays(1);
    }
    result.sort(Comparator.comparing(Event::getStart));
    return result;
  }

  /**
   * Retrieves the Event identified by the given EventId.
   *
   * @param id the EventId to look up
   * @return the matching Event
   * @throws NoSuchElementException if no event with that id exists
   */
  @Override
  public Event getEventById(EventId id) {
    if (!eventsById.containsKey(id)) {
      throw new NoSuchElementException(
              String.format("No event found with id: %s", id)
      );
    }
    return eventsById.get(id);
  }

  /**
   * Edits a single event, replacing it with a new Event object.
   *
   * @param id       the EventId of the existing event to replace
   * @param newEvent the new Event data
   * @throws NoSuchElementException  if no event with that id exists
   * @throws DuplicateEventException if the new event duplicates another existing event
   */
  @Override
  public void editEvent(EventId id, Event newEvent) {
    if (!eventsById.containsKey(id)) {
      throw new NoSuchElementException(
              String.format("No event found to edit: %s", id)
      );
    }
    Event old = eventsById.remove(id);
    LocalDate oldStart = old.getStart().toLocalDate();
    LocalDate oldEnd = old.getEnd().toLocalDate();
    for (LocalDate d = oldStart; !d.isAfter(oldEnd); d = d.plusDays(1)) {
      List<Event> dl = eventsByDate.get(d);
      if (dl != null) {
        dl.remove(old);
      }
    }

    EventId newId = new EventId(newEvent.getSubject(), newEvent.getStart(), newEvent.getEnd());
    if (eventsById.containsKey(newId)) {
      // rollback
      eventsById.put(id, old);
      for (LocalDate d = oldStart; !d.isAfter(oldEnd); d = d.plusDays(1)) {
        eventsByDate.computeIfAbsent(d, k -> new ArrayList<>()).add(old);
      }
      throw new DuplicateEventException(
              String.format("Event '%s' already exists.", newEvent.getSubject())
      );
    }

    //commit new event
    LocalDate newStart = newEvent.getStart().toLocalDate();
    LocalDate newEnd = newEvent.getEnd().toLocalDate();
    eventsById.put(newId, newEvent);
    for (LocalDate d = newStart; !d.isAfter(newEnd); d = d.plusDays(1)) {
      eventsByDate.computeIfAbsent(d, k -> new ArrayList<>()).add(newEvent);
      eventsByDate.get(d).sort(Comparator.comparing(Event::getStart));
    }
  }

  /**
   * Edits all events in a series starting from a specific instance.
   *
   * @param startingId the EventId of the first event to edit
   * @param property   the property to change
   * @param newValue   the new value for that property
   * @throws NoSuchElementException if the starting event does not exist
   */
  @Override
  public void editEventsFrom(EventId startingId, String property, String newValue) {
    if (!eventsById.containsKey(startingId)) {
      throw new NoSuchElementException(
              String.format("No event found to edit from: %s", startingId)
      );
    }
    Event startEvent = eventsById.get(startingId);

    EventSeries series = null;
    for (EventSeries s : seriesList) {
      for (Event e : s.getEvents()) {
        if (new EventId(e.getSubject(), e.getStart(), e.getEnd()).equals(startingId)) {
          series = s;
          break;
        }
      }
      if (series != null) {
        break;
      }
    }

    if (series == null) {
      editEvent(startingId, createModifiedEvent(startEvent, property, newValue));
      return;
    }

    boolean apply = false;
    for (Event e : series.getEvents()) {
      EventId eid = new EventId(e.getSubject(), e.getStart(), e.getEnd());
      if (eid.equals(startingId)) {
        apply = true;
      }
      if (apply) {
        editEvent(eid, createModifiedEvent(e, property, newValue));
      }
    }
  }

  /**
   * Edits every event in a series.
   *
   * @param subject       the series subject
   * @param originalStart the start time of the first occurrence
   * @param property      the property to change
   * @param newValue      the new value
   * @throws NoSuchElementException if the series cannot be found
   */
  @Override
  public void editSeries(String subject, LocalDateTime originalStart,
                         String property, String newValue) {
    EventSeries target = null;
    for (EventSeries s : seriesList) {
      if (s.getSubject().equals(subject) && s.getStartTime().equals(originalStart)) {
        target = s;
        break;
      }
    }
    if (target == null) {
      throw new NoSuchElementException(
              String.format("No series '%s' starting at %s", subject, originalStart)
      );
    }

    for (Event e : target.getEvents()) {
      EventId eid = new EventId(e.getSubject(), e.getStart(), e.getEnd());
      editEvent(eid, createModifiedEvent(e, property, newValue));
    }
  }

  /**
   * Checks if the calendar is busy at the given date-time.
   *
   * @param dateTime the LocalDateTime to check
   * @return true if an event covers that moment, false otherwise
   */
  @Override
  public boolean isBusyAt(LocalDateTime dateTime) {
    LocalDate date = dateTime.toLocalDate();
    List<Event> list = eventsByDate.getOrDefault(date, Collections.emptyList());
    for (Event e : list) {
      if (!dateTime.isBefore(e.getStart()) && dateTime.isBefore(e.getEnd())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Helper to modify one property of an existing event.
   */
  private Event createModifiedEvent(Event e, String property, String newValue) {
    switch (property.toLowerCase()) {
      case "subject":
        return new Event(newValue, e.getStart(), e.getEnd(),
                e.getDescription(), e.getLocation(), e.isPublic());
      case "start":
        return new Event(e.getSubject(),
                LocalDateTime.parse(newValue, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                e.getEnd(),
                e.getDescription(), e.getLocation(), e.isPublic());
      case "end":
        return new Event(e.getSubject(), e.getStart(),
                LocalDateTime.parse(newValue, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                e.getDescription(), e.getLocation(), e.isPublic());
      case "description":
        return new Event(e.getSubject(), e.getStart(), e.getEnd(),
                newValue, e.getLocation(), e.isPublic());
      case "location":
        return new Event(e.getSubject(), e.getStart(), e.getEnd(),
                e.getDescription(), newValue, e.isPublic());
      case "status":
        boolean pub = newValue.equalsIgnoreCase("public");
        return new Event(e.getSubject(), e.getStart(), e.getEnd(),
                e.getDescription(), e.getLocation(), pub);
      default:
        throw new IllegalArgumentException("Unsupported property: " + property);
    }
  }

  /**
   * Returns the timezone associated with this calendar.
   *
   * @return the ZoneId of the calendar
   */
  public ZoneId getTimezone() {
    return this.timezone;
  }

  /**
   * Returns the EventSeries this event belongs to, or null if not part of a series.
   *
   * @param e the Event to check
   * @return the EventSeries containing this event, or null if not found
   */
  public EventSeries findSeriesForEvent(Event e) {
    if (e == null) {
      return null;
    }
    for (EventSeries s : seriesList) {
      for (Event se : s.getEvents()) {
        if (se.getSubject().equals(e.getSubject())
                && se.getStart().equals(e.getStart())
                && se.getEnd().equals(e.getEnd())) {
          return s;
        }
      }
    }
    return null;
  }

  /**
   * Returns the EventSeries with given subject and start time, or null if not found.
   *
   * @param subject   subject of the series
   * @param startTime start time of the first event in the series
   * @return the EventSeries, or null if not found
   */
  public EventSeries findSeriesForEventStart(String subject, LocalDateTime startTime) {
    for (EventSeries s : seriesList) {
      if (s.getSubject().equals(subject) && s.getStartTime().equals(startTime)) {
        return s;
      }
    }
    return null;
  }

  public List<Event> getAllEvents() {
    return new ArrayList<>(this.eventsById.values());

  }


  @Override
  public List<Event> getEventsFrom(LocalDate start, int maxCount) {

    return this.getAllEvents().stream()
            .filter(e -> !e.getStart().toLocalDate().isBefore(start))
            .sorted(Comparator.comparing(Event::getStart))
            .limit(maxCount)
            .collect(Collectors.toList());
  }

  /**
   * Deletes an event from the calendar.
   *
   * @param id the EventId of the event to delete
   * @throws NoSuchElementException if no such event exists
   */
  @Override
  public void deleteEvent(EventId id) {
    if (!eventsById.containsKey(id)) {
      throw new NoSuchElementException(
              String.format("No event found to delete: %s", id)
      );
    }
    Event ev = eventsById.remove(id);

    LocalDate start = ev.getStart().toLocalDate();
    LocalDate end = ev.getEnd().toLocalDate();
    for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
      List<Event> dayList = eventsByDate.get(d);
      if (dayList != null) {
        dayList.remove(ev);
        if (dayList.isEmpty()) {
          eventsByDate.remove(d);
        }
      }
    }
  }


}
