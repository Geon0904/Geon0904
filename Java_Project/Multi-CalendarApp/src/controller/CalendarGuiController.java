package calendarapp.controller;

import calendarapp.model.MultiCalendarModel;
import calendarapp.model.Event;
import calendarapp.model.EventId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * GUI controller for calendar app.
 * Supports: calendar switching, event add/edit/delete, querying, calendar creation.
 */
public class CalendarGuiController {
  private final MultiCalendarModel manager;

  public CalendarGuiController(MultiCalendarModel manager) {
    this.manager = manager;
  }

  /**
   * Returns all calendar names.
   */
  public List<String> getCalendarNames() {
    return manager.getCalendarNames();
  }

  /**
   * Returns currently active calendar name.
   */
  public String getActiveCalendarName() {
    return manager.getActiveCalendarName();
  }

  /**
   * Switches active calendar. Returns error message, or null if OK.
   */
  public String useCalendar(String name) {
    try {
      manager.useCalendar(name);
      return null;
    } catch (Exception ex) {
      return ex.getMessage();
    }
  }

  /**
   * Creates a new calendar with the given name and timezone. Returns error message, or null if OK.
   */
  public String createCalendar(String name, ZoneId zone) {
    try {
      manager.createCalendar(name, zone);
      manager.useCalendar(name);
      return null;
    } catch (Exception ex) {
      return ex.getMessage();
    }
  }

  /**
   * Adds a new event to the current calendar. Returns "[OK]" if successful, error otherwise.
   */
  public String addEvent(String subject, LocalDateTime start, LocalDateTime end, String desc) {
    try {
      Event ev = new Event(subject, start, end, desc, "", true);
      manager.addEvent(ev);
      return "[OK] Event added.";
    } catch (Exception e) {
      return "[ERROR] " + (e.getMessage() != null ? e.getMessage() : "Failed to add event.");
    }
  }

  /**
   * Edits an existing event (by replacing with new details).
   * Returns "[OK]" if successful, error otherwise.
   */
  public String editEvent(Event oldEvent, String subject, LocalDateTime start, LocalDateTime end,
                          String desc) {
    try {
      EventId id = new EventId(oldEvent.getSubject(), oldEvent.getStart(), oldEvent.getEnd());
      Event newEvent = new Event(subject, start, end, desc, oldEvent.getLocation(),
              oldEvent.isPublic());
      manager.editEvent(id, newEvent);
      return "[OK] Event updated.";
    } catch (Exception e) {
      return "[ERROR] " + (e.getMessage() != null ? e.getMessage() : "Failed to edit event.");
    }
  }


  /**
   * Deletes an event from the current calendar.
   * Returns "[OK]" if successful, error otherwise.
   */
  public String deleteEvent(Event event) {
    try {
      EventId id = new EventId(event.getSubject(), event.getStart(), event.getEnd());
      manager.deleteEvent(id);
      return "[OK] Event deleted.";
    } catch (Exception e) {
      return "[ERROR] " + (e.getMessage() != null ? e.getMessage() : "Failed to delete event.");
    }
  }

  /**
   * Gets up to max events starting from given date.
   */
  public List<Event> getEventsFrom(LocalDate start, int max) {
    return manager.getEventsFrom(start, max);
  }

  /**
   * Gets all events on given date.
   */
  public List<Event> getEventsOn(LocalDate date) {
    return manager.getEventsOn(date);
  }

  /**
   * Returns the busy/available status at the given date and time.
   * If any event covers the specified time, returns a BUSY message with details.
   * Otherwise, returns an AVAILABLE message.
   *
   * @param dateTime the date and time to check
   * @return status string indicating BUSY (with event) or AVAILABLE
   */
  public String checkBusy(LocalDateTime dateTime) {
    List<Event> events = manager.getEventsOn(dateTime.toLocalDate());
    for (Event e : events) {
      if (!dateTime.isBefore(e.getStart()) && dateTime.isBefore(e.getEnd())) {
        return String.format("[BUSY] %s is covered by event '%s' (%s ~ %s)",
                dateTime, e.getSubject(), e.getStart(), e.getEnd());
      }
    }
    return String.format("[AVAILABLE] %s is not covered by any event.", dateTime);
  }
}
