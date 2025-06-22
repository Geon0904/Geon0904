package calendarapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import calendarapp.exceptions.DuplicateEventException;

/**
 * Manages multiple named calendars and delegates all CalendarModel operations
 * to the currently active calendar.
 */
public interface MultiCalendarModel extends CalendarModel {

  /**
   * Creates a new calendar with the given unique name and timezone,
   * and makes it the active calendar.
   *
   * @param name the unique name of the new calendar
   * @param tz   the time zone of the new calendar
   * @throws DuplicateEventException if a calendar with the given name already exists
   */
  void createCalendar(String name, ZoneId tz) throws DuplicateEventException;

  /**
   * Switches the active calendar to the one identified by the given name.
   *
   * @param name the name of an existing calendar
   * @throws IllegalArgumentException if no calendar with that name exists
   */
  void useCalendar(String name);

  /**
   * Retrieves the currently active calendar.
   *
   * @return the active CalendarModel
   * @throws IllegalStateException if no calendar has been selected yet
   */
  CalendarModel getActiveCalendar();

  /**
   * Edits a calendar's name or timezone.
   *
   * @param name     the existing calendar name
   * @param property name or timezone
   * @param newValue the new name or timezone string
   * @throws IllegalArgumentException if calendar not found or renaming collides
   */
  void editCalendar(String name, String property, String newValue);

  /**
   * Copy exactly one event into target calendar at a new start time. Returns 1 if successful.
   */
  int copyEvent(String subject,
                LocalDateTime srcStart,
                String targetCalendar,
                LocalDateTime destStart);

  /**
   * Copy all events occurring on srcDate into targetCalendar. Returns number of events copied.
   */
  int copyEventsOn(LocalDate srcDate,
                   String targetCalendar,
                   LocalDate destDate);

  /**
   * Copy every event in [from to] into targetCalendar starting. Returns number of events copied.
   */
  int copyEventsBetween(LocalDate from,
                        LocalDate to,
                        String targetCalendar,
                        LocalDate destStart);

  /**
   * Returns all calendar names (sorted, for UI display).
   */
  List<String> getCalendarNames();

  /**
   * Returns the currently active calendar name, or null if none selected.
   */
  String getActiveCalendarName();


  /**
   * Deletes an event from the currently active calendar.
   *
   * @param id the EventId of the event to delete
   * @throws IllegalArgumentException if no such event exists in the active calendar
   */
  void deleteEvent(EventId id);
}
