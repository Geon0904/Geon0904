package calendarapp.controller.commands;

import calendarapp.model.CalendarModel;
import calendarapp.model.Event;
import calendarapp.model.EventId;
import calendarapp.model.EventSeries;
import calendarapp.model.RecurrenceRule;
import calendarapp.exceptions.DuplicateEventException;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;

/**
 * Command to create a recurring series of events and add it to the calendar model.
 */
public class CreateSeriesCommand implements CalendarCommand {

  private final String subject;
  private final String startStr;
  private final String endStr;
  private final String pattern;
  private final String untilOrCount;
  private final boolean useUntil;

  /**
   * Creates a command that adds a recurring event series to the calendar.
   *
   * @param subject      the title of the events
   * @param startStr     ISO date-time for when each event starts
   * @param endStr       ISO date-time for when each event ends
   * @param pattern      letters that mark repeat days
   * @param untilOrCount either an ISO date the series stops on or a repeat count
   * @param useUntil     true if untilOrCount is a date, false if it is a count
   */
  public CreateSeriesCommand(
          String subject,
          String startStr,
          String endStr,
          String pattern,
          String untilOrCount,
          boolean useUntil
  ) {
    this.subject = subject;
    this.startStr = startStr;
    this.endStr = endStr;
    this.pattern = pattern;
    this.untilOrCount = untilOrCount;
    this.useUntil = useUntil;
  }


  @Override
  public String execute(CalendarModel model) {
    LocalDateTime startDT;
    LocalDateTime endDT;

    try {
      startDT = LocalDateTime.parse(startStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      endDT = LocalDateTime.parse(endStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    } catch (DateTimeParseException e) {
      return "[ERROR] Invalid date or time format. Use 'YYYY-MM-DDTHH:MM' and 'YYYY-MM-DD'.";
    }

    if (!startDT.toLocalDate().equals(endDT.toLocalDate())) {
      return "[ERROR] Each event in a series must start and end on the same day.";
    }

    Set<DayOfWeek> days = new HashSet<>();
    for (char c : pattern.toUpperCase().toCharArray()) {
      switch (c) {
        case 'M':
          days.add(DayOfWeek.MONDAY);
          break;
        case 'T':
          days.add(DayOfWeek.TUESDAY);
          break;
        case 'W':
          days.add(DayOfWeek.WEDNESDAY);
          break;
        case 'R':
          days.add(DayOfWeek.THURSDAY);
          break;
        case 'F':
          days.add(DayOfWeek.FRIDAY);
          break;
        case 'S':
          days.add(DayOfWeek.SATURDAY);
          break;
        case 'U':
          days.add(DayOfWeek.SUNDAY);
          break;
        default:
          return "[ERROR] Invalid weekday character: " + c;
      }
    }

    RecurrenceRule rule;
    if (useUntil) {
      LocalDate untilDate;
      try {
        untilDate = LocalDate.parse(untilOrCount, DateTimeFormatter.ISO_LOCAL_DATE);
      } catch (DateTimeParseException e) {
        return "[ERROR] Invalid date format. Use 'YYYY-MM-DD'.";
      }
      rule = new RecurrenceRule(days, untilDate);
    } else {
      int count;
      try {
        count = Integer.parseInt(untilOrCount);
      } catch (NumberFormatException e) {
        return "[ERROR] Invalid repeat count. Must be an integer.";
      }
      rule = new RecurrenceRule(days, count);
    }

    EventSeries series;
    try {
      series = new EventSeries(subject, startDT, endDT, "", "", true, rule);
    } catch (IllegalArgumentException e) {
      return "[ERROR] " + e.getMessage();
    }


    for (Event e : series.getEvents()) {


      EventId id = new EventId(e.getSubject(), e.getStart(), e.getEnd());
      try {
        model.getEventById(id);
        return "[ERROR] Event '" + subject + "' already exists.";
      } catch (Exception ignore) {
        //not found? this means it's safe to create
      }
    }


    try {
      model.addEventSeries(series);
    } catch (DuplicateEventException e) {
      return "[ERROR] " + e.getMessage();
    }

    int countEvents = series.getEvents().size();
    return String.format("[OK] Created event series '%s' (%d events)",
            subject, countEvents);
  }
}