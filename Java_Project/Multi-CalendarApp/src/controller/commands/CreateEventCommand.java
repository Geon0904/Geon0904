package calendarapp.controller.commands;

import calendarapp.model.CalendarModel;
import calendarapp.model.Event;
import calendarapp.exceptions.DuplicateEventException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


/**
 * Command to create a single event and add it to the calendar model.
 */
public class CreateEventCommand implements CalendarCommand {

  private final String subject;
  private final String start;
  private final String end;
  private final String description;
  private final String location;
  private final boolean isPublic;

  /**
   * Constructs the CreateEventCommand with parsed arguments.
   *
   * @param subject     subject of the event
   * @param start       start datetime string (e.g. 2025-07-04T09:00)
   * @param end         end datetime string (e.g. 2025-07-04T10:00)
   * @param description event description
   * @param location    location of the event
   * @param isPublic    true if public, false if private
   */
  public CreateEventCommand(String subject, String start, String end,
                            String description, String location, boolean isPublic) {
    this.subject = subject;
    this.start = start;
    this.end = end;
    this.description = description;
    this.location = location;
    this.isPublic = isPublic;
  }

  /**
   * Executes the command to add an event to the calendar.
   *
   * @param model the calendar model to operate on
   * @return success or failure message
   */

  @Override
  public String execute(CalendarModel model) {
    try {

      LocalDateTime startDT = LocalDateTime.parse(start, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

      LocalDateTime endDT;
      if (end == null) {

        LocalDate date = startDT.toLocalDate();
        startDT = LocalDateTime.of(date, java.time.LocalTime.of(8, 0));
        endDT = LocalDateTime.of(date, java.time.LocalTime.of(17, 0));
      } else {
        endDT = LocalDateTime.parse(end, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        if (endDT.isBefore(startDT) || endDT.equals(startDT)) {
          return "[ERROR] End time must be after start time.";
        }
      }

      Event event = new Event(subject, startDT, endDT, description, location, isPublic);
      model.addEvent(event);
      return String.format("[OK] Created event '%s' from %s to %s.",
              subject,
              startDT,
              endDT);

    } catch (DateTimeParseException e) {
      return "[ERROR] Invalid date-time format. Use 'YYYY-MM-DDTHH:MM'.";
    } catch (DuplicateEventException e) {
      return "[ERROR] " + e.getMessage();
    }
  }
}