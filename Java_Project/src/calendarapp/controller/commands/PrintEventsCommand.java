package calendarapp.controller.commands;

import calendarapp.model.CalendarModel;
import calendarapp.model.Event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;


/**
 * Command to print events on a specific date.
 */
public class PrintEventsCommand implements CalendarCommand {

  private final String from;
  private final String to; // Can be null if single-date query
  private final boolean isRange;

  /**
   * Print events on date.
   */
  public PrintEventsCommand(String fromDate) {
    this.from = fromDate;
    this.to = null;
    this.isRange = false;
  }

  /**
   * Constructs a date-time range version: print events from start to end.
   */
  public PrintEventsCommand(String fromDateTime, String toDateTime) {
    this.from = fromDateTime;
    this.to = toDateTime;
    this.isRange = true;
  }

  @Override
  public String execute(CalendarModel model) {
    try {
      if (isRange) {
        LocalDateTime startDT = LocalDateTime.parse(from, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime endDT = LocalDateTime.parse(to, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        List<Event> events = model.getEventsBetween(startDT, endDT);
        if (events.isEmpty()) {
          return String.format("[NO EVENTS] from %s to %s", startDT, endDT);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[EVENTS FROM %s TO %s]\n", startDT, endDT));
        for (Event e : events) {
          sb.append("• ").append(e.toString()).append("\n");
        }
        return sb.toString().trim();

      } else {
        LocalDate date = LocalDate.parse(from, DateTimeFormatter.ISO_LOCAL_DATE);
        List<Event> events = model.getEventsOn(date);
        if (events.isEmpty()) {
          return String.format("[NO EVENTS] on %s", date);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[EVENTS ON %s]\n", date));
        for (Event e : events) {
          sb.append("• ").append(e.toString()).append("\n");
        }
        return sb.toString().trim();
      }

    } catch (DateTimeParseException e) {
      return "[ERROR] Invalid date or time format. Use YYYY-MM-DD or YYYY-MM-DDTHH:MM";
    }
  }
}