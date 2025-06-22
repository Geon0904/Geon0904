package calendarapp.model;

import calendarapp.exceptions.DuplicateEventException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Concrete implementation of MultiCalendarModel that holds
 * multiple CalendarModel instances by name and delegates
 * all operations to the currently active calendar.
 */
public class CalendarManagerImpl implements MultiCalendarModel {
  private final Map<String, CalendarModel> calendars = new HashMap<>();
  private String activeName;


  @Override
  public void createCalendar(String name, ZoneId tz) throws DuplicateEventException {
    if (name == null || tz == null) {
      throw new IllegalArgumentException("Calendar name and timezone must not be null.");
    }

    if (calendars.containsKey(name)) {
      throw new DuplicateEventException(
              String.format("Calendar '%s' already exists.", name));
    }
    CalendarImpl cal = new CalendarImpl(tz);
    calendars.put(name, cal);
    activeName = name;
  }


  @Override
  public void useCalendar(String name) {
    if (name == null) {
      throw new IllegalArgumentException("name must not be null.");
    }

    if (!calendars.containsKey(name)) {
      throw new NoSuchElementException(
              String.format("No calendar named '%s'.", name));
    }
    activeName = name;
  }


  @Override
  public CalendarModel getActiveCalendar() {
    if (activeName == null) {
      throw new IllegalStateException("No active calendar selected.");
    }
    return calendars.get(activeName);
  }


  @Override
  public void editCalendar(String name, String property, String newValue) {
    if (!calendars.containsKey(name)) {
      throw new NoSuchElementException(
              String.format("No calendar named '%s'.", name));
    }
    CalendarModel cal = calendars.get(name);
    switch (property.toLowerCase()) {
      case "name":
        if (newValue == null) {
          throw new IllegalArgumentException("newValue (calendar name) must not be null.");
        }

        if (calendars.containsKey(newValue)) {
          throw new DuplicateEventException(
                  String.format("Calendar '%s' already exists.", newValue));
        }
        calendars.remove(name);
        calendars.put(newValue, cal);
        if (name.equals(activeName)) {
          activeName = newValue;
        }
        break;
      case "timezone":
        ZoneId tz = ZoneId.of(newValue);
        ((CalendarImpl) cal).setTimezone(tz);
        break;
      default:
        throw new IllegalArgumentException("Unsupported property: " + property);
    }
  }


  @Override
  public void addEvent(Event event) throws DuplicateEventException {
    getActiveCalendar().addEvent(event);
  }


  @Override
  public void addEventSeries(EventSeries series) throws DuplicateEventException {
    getActiveCalendar().addEventSeries(series);
  }


  @Override
  public List<Event> getEventsOn(LocalDate date) {
    return getActiveCalendar().getEventsOn(date);
  }


  @Override
  public List<Event> getEventsBetween(LocalDateTime from, LocalDateTime to) {
    return getActiveCalendar().getEventsBetween(from, to);
  }


  @Override
  public boolean isBusyAt(LocalDateTime dateTime) {
    return getActiveCalendar().isBusyAt(dateTime);
  }


  @Override
  public void editEvent(EventId id, Event newEvent) {
    getActiveCalendar().editEvent(id, newEvent);
  }


  @Override
  public void editEventsFrom(EventId startingId, String property, String newValue) {
    getActiveCalendar().editEventsFrom(startingId, property, newValue);
  }


  @Override
  public void editSeries(String subject, LocalDateTime originalStart,
                         String property, String newValue) {
    getActiveCalendar().editSeries(subject, originalStart, property, newValue);
  }


  @Override
  public Event getEventById(EventId id) {
    return getActiveCalendar().getEventById(id);
  }

  /**
   * Copies a single event from the active calendar into the target calendar.
   * If the event is part of a series, copies the entire series (if not already present).
   * Timezone conversion is applied to event/series start & end.
   * Returns the number of events actually added.
   *
   * @param subject        event subject
   * @param srcStart       event start in source calendar's timezone
   * @param targetCalendar name of the target calendar
   * @param destStart      start time in target calendar's timezone
   * @return number of events actually copied
   */
  @Override
  public int copyEvent(String subject, LocalDateTime srcStart,
                       String targetCalendar, LocalDateTime destStart) {
    CalendarModel src = getActiveCalendar();
    CalendarModel dst = calendars.get(targetCalendar);
    if (dst == null) {
      throw new NoSuchElementException(
              String.format("No calendar named '%s'.", targetCalendar));
    }

    Event orig = null;
    for (Event e : src.getEventsOn(srcStart.toLocalDate())) {
      if (e.getSubject().equals(subject) && e.getStart().equals(srcStart)) {
        orig = e;
        break;
      }
    }
    if (orig == null) {
      throw new NoSuchElementException(
              String.format("No event named '%s' starting at %s.", subject, srcStart));
    }

    EventSeries origSeries = ((CalendarImpl) src).findSeriesForEvent(orig);
    if (origSeries != null) {

      if (((CalendarImpl) dst).findSeriesForEventStart(origSeries.getSubject(),
              destStart) != null) {
        return 0;
      }
      LocalDateTime newSeriesStart = destStart;
      LocalDateTime newSeriesEnd = newSeriesStart.plus(
              Duration.between(origSeries.getStartTime(), origSeries.getEndTime()));
      EventSeries newSeries = new EventSeries(
              origSeries.getSubject(),
              newSeriesStart,
              newSeriesEnd,
              origSeries.getDescription(),
              origSeries.getLocation(),
              origSeries.isPublic(),
              origSeries.getRule()
      );
      try {
        ((CalendarImpl) dst).addEventSeries(newSeries);
        return newSeries.getEvents().size();
      } catch (DuplicateEventException e) {
        return 0;
      }
    } else {
      Duration dur = Duration.between(orig.getStart(), orig.getEnd());
      Event copy = new Event(orig.getSubject(),
              destStart,
              destStart.plus(dur),
              orig.getDescription(),
              orig.getLocation(),
              orig.isPublic());
      try {
        dst.addEvent(copy);
        return 1;
      } catch (DuplicateEventException e) {
        return 0;
      }
    }
  }

  /**
   * Copies all events on a given date from the active calendar into the target calendar,
   * preserving series if found; timezone conversion applied.
   * Avoids redundant series/event copies.
   *
   * @param srcDate        date to copy from
   * @param targetCalendar name of the target calendar
   * @param destDate       date to copy to in target calendar's timezone
   * @return number of events actually copied
   */
  @Override
  public int copyEventsOn(LocalDate srcDate, String targetCalendar, LocalDate destDate) {
    CalendarModel src = getActiveCalendar();
    CalendarModel dst = calendars.get(targetCalendar);
    if (dst == null) {
      throw new NoSuchElementException(
              String.format("No calendar named '%s'.", targetCalendar));
    }
    int total = 0;
    Set<EventSeries> copiedSeries = new HashSet<>();

    for (Event e : src.getEventsOn(srcDate)) {
      EventSeries series = ((CalendarImpl) src).findSeriesForEvent(e);
      if (series != null && copiedSeries.add(series)) {

        LocalDateTime newSeriesStart = destDate.atTime(series.getStartTime().toLocalTime());
        LocalDateTime newSeriesEnd = destDate.atTime(series.getEndTime().toLocalTime());
        if (((CalendarImpl) dst).findSeriesForEventStart(series.getSubject(), newSeriesStart)
                == null) {
          EventSeries newSeries = new EventSeries(
                  series.getSubject(),
                  newSeriesStart,
                  newSeriesEnd,
                  series.getDescription(),
                  series.getLocation(),
                  series.isPublic(),
                  series.getRule()
          );
          try {
            ((CalendarImpl) dst).addEventSeries(newSeries);
            total += newSeries.getEvents().size();
          } catch (DuplicateEventException ignored) {
          }
        }
      } else if (series == null) {
        Duration dur = Duration.between(e.getStart(), e.getEnd());
        LocalDateTime newStart = destDate.atTime(e.getStart().toLocalTime());
        Event copy = new Event(e.getSubject(),
                newStart,
                newStart.plus(dur),
                e.getDescription(),
                e.getLocation(),
                e.isPublic());
        try {
          dst.addEvent(copy);
          total++;
        } catch (DuplicateEventException ignored) {
        }
      }
    }
    return total;
  }

  /**
   * Copies all events in a date-range from the active calendar into the target calendar,
   * preserving series if found; timezone conversion applied.
   * Avoids redundant series/event copies.
   *
   * @param from           start date
   * @param to             end date
   * @param targetCalendar name of the target calendar
   * @param destStart      starting date in target calendar's timezone
   * @return number of events actually copied
   */
  @Override
  public int copyEventsBetween(LocalDate from, LocalDate to,
                               String targetCalendar, LocalDate destStart) {
    CalendarModel src = getActiveCalendar();
    CalendarModel dst = calendars.get(targetCalendar);
    if (dst == null) {
      throw new NoSuchElementException(
              String.format("No calendar named '%s'.", targetCalendar));
    }
    long offsetDays = java.time.temporal.ChronoUnit.DAYS.between(from, destStart);
    int total = 0;
    Set<EventSeries> copiedSeries = new HashSet<>();
    for (Event e : src.getEventsBetween(from.atStartOfDay(), to.atTime(23, 59))) {
      EventSeries series = ((CalendarImpl) src).findSeriesForEvent(e);
      if (series != null
              && copiedSeries.add(series)) {
        LocalDateTime newSeriesStart = series.getStartTime().plusDays(offsetDays);
        LocalDateTime newSeriesEnd = series.getEndTime().plusDays(offsetDays);

        if (((CalendarImpl) dst).findSeriesForEventStart(series.getSubject(), newSeriesStart)
                == null) {
          EventSeries newSeries = new EventSeries(
                  series.getSubject(),
                  newSeriesStart,
                  newSeriesEnd,
                  series.getDescription(),
                  series.getLocation(),
                  series.isPublic(),
                  series.getRule()
          );

          try {
            ((CalendarImpl) dst).addEventSeries(newSeries);
            total += newSeries.getEvents().size();
          } catch (DuplicateEventException ignored) {
          }
        }
      } else if (series == null) {
        Duration dur = Duration.between(e.getStart(), e.getEnd());
        LocalDateTime newStart = e.getStart().plusDays(offsetDays);
        Event copy = new Event(e.getSubject(),
                newStart,
                newStart.plus(dur),
                e.getDescription(),
                e.getLocation(),
                e.isPublic());
        try {
          dst.addEvent(copy);
          total++;
        } catch (DuplicateEventException ignored) {
        }
      }
    }
    return total;
  }

  public CalendarModel getCalendarByName(String name) {
    return calendars.get(name); // Map<String, CalendarModel>
  }

  @Override
  public List<Event> getEventsFrom(LocalDate start, int maxCount) {
    return getActiveCalendar().getEventsFrom(start, maxCount);
  }

  /**
   * Returns all calendar names (sorted, for UI display).
   */
  public java.util.List<String> getCalendarNames() {
    java.util.List<String> names = new java.util.ArrayList<>(calendars.keySet());
    java.util.Collections.sort(names);
    return names;
  }

  /**
   * Returns the currently active calendar name, or null if none selected.
   */
  public String getActiveCalendarName() {
    return activeName;
  }

  @Override
  public void deleteEvent(EventId id) {
    getActiveCalendar().deleteEvent(id);
  }


}
