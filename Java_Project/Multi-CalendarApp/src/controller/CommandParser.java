package calendarapp.controller;


import calendarapp.controller.commands.CalendarCommand;
import calendarapp.controller.commands.CopyEventCommand;
import calendarapp.controller.commands.CopyEventsCommand;
import calendarapp.controller.commands.CopyEventsRangeCommand;
import calendarapp.controller.commands.CreateCalendarCommand;
import calendarapp.controller.commands.CreateEventCommand;
import calendarapp.controller.commands.CreateSeriesCommand;
import calendarapp.controller.commands.EditCalendarCommand;
import calendarapp.controller.commands.EditEventCommand;
import calendarapp.controller.commands.ExitCommand;
import calendarapp.controller.commands.PrintEventsCommand;
import calendarapp.controller.commands.ShowStatusCommand;
import calendarapp.controller.commands.UseCalendarCommand;
import calendarapp.exceptions.InvalidCommandException;
import calendarapp.model.Event;
import calendarapp.model.EventId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;


/**
 * Parses user input strings into CalendarCommand objects.
 * Assignment5: Handles all commands and error formats as specified.
 */
public class CommandParser {

  private static final Set<String> VALID_CALENDAR_PROPERTIES = new
          HashSet<>(Arrays.asList("name", "timezone"));

  /**
   * Parses the raw user input into a CalendarCommand.
   *
   * @param input the raw input
   * @return a CalendarCommand to execute
   * @throws InvalidCommandException if the input is malformed or unknown
   */
  public static CalendarCommand parse(String input) throws InvalidCommandException {
    if (input == null || input.trim().isEmpty()) {
      throw new InvalidCommandException("[ERROR] Command cannot be empty.");
    }
    String[] tokens = input.trim().split("\\s+");
    String main = tokens[0].toLowerCase();

    switch (main) {
      case "create":
        return handleCreate(tokens);
      case "use":
        return handleUse(tokens);
      case "edit":
        return handleEdit(tokens);
      case "print":
        return handlePrint(tokens);
      case "show":
        return handleShow(tokens);
      case "copy":
        return handleCopy(tokens);
      case "exit":
      case "quit":
        return new ExitCommand();
      default:
        throw new InvalidCommandException("[ERROR] Unknown command: " + main);
    }
  }

  //CREATE
  private static CalendarCommand handleCreate(String[] t) throws InvalidCommandException {
    if (t.length < 2) {
      throw new InvalidCommandException("[ERROR] Wrong syntax for create command.");
    }
    switch (t[1].toLowerCase()) {
      case "calendar":
        return parseCreateCalendar(t);
      case "event":
        return parseCreateEvent(t, 2);
      case "series":
        return parseCreateSeries(t, 2);
      default:
        throw new InvalidCommandException("[ERROR] Unknown create sub-command: " + t[1]);
    }
  }

  private static CalendarCommand parseCreateCalendar(String[] t) throws InvalidCommandException {
    // Accept both --name Foo --timezone ... and --timezone ... --name ...
    String name = null;
    String zone = null;

    for (int i = 2; i < t.length - 1; i++) {
      if (t[i].equalsIgnoreCase("--name")) {
        name = t[i + 1];
      }
      if (t[i].equalsIgnoreCase("--timezone")) {
        zone = t[i + 1];
      }
    }

    if (name == null || zone == null) {
      throw new InvalidCommandException("[ERROR] Usage: create calendar --name <name> --timezone "
              + "<zone>");
    }


    try {
      ZoneId tz = ZoneId.of(zone);
      return new CreateCalendarCommand(name, tz);
    } catch (Exception e) {
      throw new InvalidCommandException("[ERROR] Invalid timezone: " + zone);
    }
  }

  //USE CALENDAR
  private static CalendarCommand handleUse(String[] t) throws InvalidCommandException {
    // Support both: use calendar Foo  OR  use calendar --name Foo
    if (t.length == 3 && t[1].equalsIgnoreCase("calendar")) {
      return new UseCalendarCommand(t[2]);
    }
    if (t.length == 4 && t[1].equalsIgnoreCase("calendar")
            && t[2].equalsIgnoreCase("--name")) {
      return new UseCalendarCommand(t[3]);
    }
    throw new InvalidCommandException("[ERROR] Usage: use calendar <name>");
  }

  //EDIT
  private static CalendarCommand handleEdit(String[] t) throws InvalidCommandException {
    if (t.length < 2) {
      throw new InvalidCommandException("[ERROR] Wrong syntax for edit command.");
    }
    if (t[1].equalsIgnoreCase("calendar")) {
      return parseEditCalendar(t);
    }

    if (t[1].equalsIgnoreCase("event")) {
      return parseEditEvent(t, 2);
    }
    if (t[1].equalsIgnoreCase("events")) {
      return parseEditEvents(t, 2);
    }
    if (t[1].equalsIgnoreCase("series")) {
      return parseEditSeries(t, 2);
    }

    throw new InvalidCommandException("[ERROR] Unknown edit sub-command: " + t[1]);
  }

  private static CalendarCommand parseEditCalendar(String[] t) throws InvalidCommandException {
    // edit calendar --name Foo --property name NewFoo
    String name = null;
    String prop = null;
    String value = null;

    for (int i = 2; i < t.length - 1; i++) {
      if (t[i].equalsIgnoreCase("--name")) {
        name = t[i + 1];
      }

      if (t[i].equalsIgnoreCase("--property")) {
        prop = t[i + 1];
        if (i + 2 < t.length) {
          value = t[i + 2];
        }
      }
    }

    if (name == null || prop == null || value == null) {
      throw new InvalidCommandException("[ERROR] Usage: edit calendar --name <name> --property "
              + "<prop> <value>");
    }

    if (!VALID_CALENDAR_PROPERTIES.contains(prop.toLowerCase())) {
      throw new InvalidCommandException("[ERROR] Invalid property for edit calendar: " + prop);
    }

    return new EditCalendarCommand(name, prop, value);
  }

  //PRINT
  private static CalendarCommand handlePrint(String[] t) throws InvalidCommandException {
    if (t.length == 4 && t[1].equalsIgnoreCase("events")
            && t[2].equalsIgnoreCase("on")) {
      return new PrintEventsCommand(t[3]);
    }
    if (t.length == 6 && t[1].equalsIgnoreCase("events")
            && t[2].equalsIgnoreCase("from")
            && t[4].equalsIgnoreCase("to")) {
      return new PrintEventsCommand(t[3], t[5]);
    }
    throw new InvalidCommandException("[ERROR] Wrong syntax for print events");
  }


  //SHOW STATUS
  private static CalendarCommand handleShow(String[] t) throws InvalidCommandException {
    if (t.length == 4 && t[1].equalsIgnoreCase("status")
            && t[2].equalsIgnoreCase("on")) {
      return new ShowStatusCommand(t[3]);
    }
    throw new InvalidCommandException("[ERROR] Wrong syntax for show status");
  }

  //COPY
  private static CalendarCommand handleCopy(String[] t) throws InvalidCommandException {
    //copy event <name> on <dt> --target <cal> to <dt>
    if (t.length == 9 && t[1].equalsIgnoreCase("event")
            && t[3].equalsIgnoreCase("on")
            && t[5].equalsIgnoreCase("--target")
            && t[7].equalsIgnoreCase("to")) {

      try {
        LocalDateTime srcDT = LocalDateTime.parse(t[4], DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime destDT = LocalDateTime.parse(t[8], DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return new CopyEventCommand(t[2], srcDT, t[6], destDT);

      } catch (DateTimeParseException e) {
        throw new InvalidCommandException("[ERROR] Invalid datetime format in copy event.");
      }
    }
    //copy events on <date> --target <cal> to <date>
    if (t.length == 8 && t[1].equalsIgnoreCase("events")
            && t[2].equalsIgnoreCase("on")
            && t[4].equalsIgnoreCase("--target")
            && t[6].equalsIgnoreCase("to")) {

      try {
        LocalDate srcDate = LocalDate.parse(t[3], DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate destDate = LocalDate.parse(t[7], DateTimeFormatter.ISO_LOCAL_DATE);
        return new CopyEventsCommand(srcDate, t[5], destDate);
      } catch (DateTimeParseException e) {
        throw new InvalidCommandException("[ERROR] Invalid date format in copy events on.");
      }
    }
    //copy events between <d1> and <d2> --target <cal> to <d3>
    if (t.length == 10 && t[1].equalsIgnoreCase("events")
            && t[2].equalsIgnoreCase("between")
            && t[4].equalsIgnoreCase("and")
            && t[6].equalsIgnoreCase("--target")
            && t[8].equalsIgnoreCase("to")) {

      try {
        LocalDate from = LocalDate.parse(t[3], DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate to = LocalDate.parse(t[5], DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate dest = LocalDate.parse(t[9], DateTimeFormatter.ISO_LOCAL_DATE);
        return new CopyEventsRangeCommand(from, to, t[7], dest);
      } catch (DateTimeParseException e) {
        throw new InvalidCommandException("[ERROR] Invalid date format in copy events between.");
      }
    }
    throw new InvalidCommandException("[ERROR] Unknown or malformed copy command");
  }

  //CREATE EVENT, SERIES
  private static CalendarCommand parseCreateEvent(String[] tokens, int idx)
          throws InvalidCommandException {

    List<String> subjectParts = new ArrayList<>();
    int i = idx;
    while (i < tokens.length && !tokens[i].equalsIgnoreCase("from")
            && !tokens[i].equalsIgnoreCase("on")) {
      subjectParts.add(tokens[i]);
      i++;
    }
    if (subjectParts.isEmpty()) {
      throw new InvalidCommandException("[ERROR] Missing subject for create event");
    }

    String subject = String.join(" ", subjectParts);
    if (i >= tokens.length) {
      throw new InvalidCommandException("[ERROR] Missing 'from' or 'on' keyword for create event");
    }

    String whenKeyword = tokens[i].toLowerCase();
    if (whenKeyword.equals("from")) {
      if (i + 1 >= tokens.length) {
        throw new InvalidCommandException("[ERROR] Missing start time for create event");
      }
      String startStr = tokens[i + 1];
      String endStr = null;
      int nextIdx = i + 2;

      if (nextIdx < tokens.length && tokens[nextIdx].equalsIgnoreCase("to")) {

        if (nextIdx + 1 >= tokens.length) {
          throw new InvalidCommandException("[ERROR] Missing end time for create event");
        }
        endStr = tokens[nextIdx + 1];
        nextIdx += 2;
      } else {

        try {
          LocalDateTime startDT = LocalDateTime.parse(startStr,
                  DateTimeFormatter.ISO_LOCAL_DATE_TIME);

          endStr = startDT.plusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ex) {

          try {
            LocalDate date = LocalDate.parse(startStr, DateTimeFormatter.ISO_LOCAL_DATE);
            startStr = date.atTime(8, 0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            endStr = date.atTime(17, 0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
          } catch (DateTimeParseException e2) {
            throw new InvalidCommandException("[ERROR] Invalid date or datetime format for create "
                    +
                    "event");
          }
        }
      }


      if (nextIdx < tokens.length && tokens[nextIdx].equalsIgnoreCase("repeats")) {
        return parseCreateEventSeriesFrom(tokens, subject, startStr, endStr, nextIdx);
      }
      return new CreateEventCommand(subject, startStr, endStr, "", "",
              true);
    } else if (whenKeyword.equals("on")) {

      if (i + 1 >= tokens.length) {
        throw new InvalidCommandException("[ERROR] Missing date after 'on' for create event");
      }
      String dateStr = tokens[i + 1];
      i = i + 2;
      String startStr = dateStr + "T08:00";
      String endStr = dateStr + "T17:00";

      if (i < tokens.length && tokens[i].equalsIgnoreCase("repeats")) {
        return parseCreateEventSeriesOn(tokens, subject, dateStr, i);
      }
      return new CreateEventCommand(subject, startStr, endStr, "", "",
              true);
    }
    throw new InvalidCommandException("[ERROR] Expected 'from' or 'on' after subject for " +
            "create event");
  }


  private static CalendarCommand parseCreateEventSeriesFrom(String[] tokens,
                                                            String subject, String startStr,
                                                            String endStr, int idx) throws
          InvalidCommandException {

    if (idx + 1 >= tokens.length) {
      throw new InvalidCommandException("[ERROR] Missing weekday pattern after 'repeats'");
    }

    String pattern = tokens[idx + 1];
    validateRepeatPattern(pattern);
    idx += 2;

    if (idx >= tokens.length) {
      throw new InvalidCommandException("[ERROR] Missing 'for' or 'until' after pattern");
    }
    String countOrUntil = tokens[idx];

    if (countOrUntil.equalsIgnoreCase("for")) {
      if (idx + 1 >= tokens.length) {
        throw new InvalidCommandException("[ERROR] "
                + "Missing count after 'for'");
      }
      return new CreateSeriesCommand(subject, startStr, endStr, pattern, tokens[idx + 1],
              false);
    }

    if (countOrUntil.equalsIgnoreCase("until")) {
      if (idx + 1 >= tokens.length) {
        throw new InvalidCommandException("[ERROR] Missing date "
                + "after 'until'");
      }
      return new CreateSeriesCommand(subject, startStr, endStr, pattern, tokens[idx + 1],
              true);
    }
    //try to parse as integer or date
    try {
      Integer.parseInt(countOrUntil);
      return new CreateSeriesCommand(subject, startStr,
              endStr, pattern, countOrUntil, false);
    } catch (NumberFormatException e) {
      return new CreateSeriesCommand(subject, startStr,
              endStr, pattern, countOrUntil, true);
    }
  }


  private static CalendarCommand parseCreateEventSeriesOn(String[] tokens, String subject,
                                                          String dateStr, int idx) throws
          InvalidCommandException {

    if (idx + 1 >= tokens.length) {
      throw new InvalidCommandException("[ERROR] Missing weekday pattern after 'repeats'");
    }

    String pattern = tokens[idx + 1];
    validateRepeatPattern(pattern);
    idx += 2;
    if (idx >= tokens.length) {

      throw new InvalidCommandException("[ERROR] Missing 'for' or 'until' after pattern");
    }

    String countOrUntil = tokens[idx];
    String startStr = dateStr + "T08:00";
    String endStr = dateStr + "T17:00";
    if (countOrUntil.equalsIgnoreCase("for")) {
      if (idx + 1 >= tokens.length) {
        throw new InvalidCommandException("[ERROR] "
                + "Missing count after 'for'");
      }
      {
        return new CreateSeriesCommand(subject, startStr, endStr, pattern, tokens[idx + 1],
                false);
      }
    }
    if (countOrUntil.equalsIgnoreCase("until")) {
      if (idx + 1 >= tokens.length) {
        throw new InvalidCommandException("[ERROR] Missing "
                + "date after 'until'");
      }
      return new CreateSeriesCommand(subject, startStr, endStr, pattern, tokens[idx + 1],
              true);
    }

    try {
      Integer.parseInt(countOrUntil);
      return new CreateSeriesCommand(subject, startStr,
              endStr, pattern, countOrUntil, false);
    } catch (NumberFormatException ex) {
      return new CreateSeriesCommand(subject, startStr,
              endStr, pattern, countOrUntil, true);
    }
  }

  private static void validateRepeatPattern(String pattern) throws InvalidCommandException {
    //Only allow letters from MTWRFSU, case-insensitive
    for (char c : pattern.toUpperCase().toCharArray()) {
      if ("MTWRFSU".indexOf(c) == -1) {
        throw new InvalidCommandException("[ERROR] Invalid repeat pattern: " + pattern);
      }
    }
  }

  //CREATE SERIES
  private static CalendarCommand parseCreateSeries(String[] tokens, int idx) throws
          InvalidCommandException {
    // Allow: create series Subject from ... to ... repeats ... for N/until ...
    List<String> subjectParts = new ArrayList<>();
    int i = idx;
    while (i < tokens.length && !tokens[i].equalsIgnoreCase("from")
            && !tokens[i].equalsIgnoreCase("on")) {
      subjectParts.add(tokens[i]);
      i++;
    }
    if (subjectParts.isEmpty()) {
      throw new InvalidCommandException("[ERROR] Missing subject for create series");
    }
    String subject = String.join(" ", subjectParts);

    if (i >= tokens.length) {
      throw new InvalidCommandException("[ERROR] Missing 'from' or 'on' after "
              + "subject for create series");
    }
    String whenKeyword = tokens[i].toLowerCase();

    if (whenKeyword.equals("from")) {
      if (i + 3 >= tokens.length) {
        throw new InvalidCommandException("[ERROR] Missing start or end time for create series");
      }
      String startStr = tokens[i + 1];

      if (!tokens[i + 2].equalsIgnoreCase("to")) {
        throw new InvalidCommandException("[ERROR] Missing 'to' after start time "
                + "for create series");
      }

      String endStr = tokens[i + 3];
      i = i + 4;

      if (i >= tokens.length || !tokens[i].equalsIgnoreCase("repeats")) {
        throw new InvalidCommandException("[ERROR] Missing 'repeats' for create series");
      }
      return parseCreateEventSeriesFrom(tokens, subject, startStr, endStr, i);
    }
    if (whenKeyword.equals("on")) {
      if (i + 1 >= tokens.length) {
        throw new InvalidCommandException("[ERROR] Missing date after 'on' for create series");
      }

      String dateStr = tokens[i + 1];
      i = i + 2;
      if (i >= tokens.length || !tokens[i].equalsIgnoreCase("repeats")) {
        throw new InvalidCommandException("[ERROR] Missing 'repeats' for create series");
      }
      return parseCreateEventSeriesOn(tokens, subject, dateStr, i);
    }
    throw new InvalidCommandException("[ERROR] Expected 'from' or 'on' after subject for "
            + "create series");
  }

  //EDIT EVENT/SERIES
  private static CalendarCommand parseEditEvent(String[] tokens, int idx) throws
          InvalidCommandException {
    // edit event <property> <subject> from <start> to <end> with <newValue>
    if (idx + 1 >= tokens.length) {
      throw new InvalidCommandException("[ERROR] Missing property for edit event");
    }

    String property = tokens[idx];
    idx++;
    List<String> subjectParts = new ArrayList<>();

    while (idx < tokens.length && !tokens[idx].equalsIgnoreCase("from")) {
      subjectParts.add(tokens[idx]);
      idx++;
    }

    if (subjectParts.isEmpty()) {
      throw new InvalidCommandException("[ERROR] Missing subject for edit event");
    }

    String subject = String.join(" ", subjectParts);

    if (idx >= tokens.length || !tokens[idx].equalsIgnoreCase("from")) {
      throw new InvalidCommandException("[ERROR] Missing 'from' for edit event");
    }

    if (idx + 1 >= tokens.length) {
      throw new InvalidCommandException("[ERROR] Missing start time for edit event");
    }


    String startStr = tokens[idx + 1];
    idx += 2;

    if (idx >= tokens.length || !tokens[idx].equalsIgnoreCase("to")) {
      throw new InvalidCommandException("[ERROR] Missing 'to' for edit event");
    }

    if (idx + 1 >= tokens.length) {
      throw new InvalidCommandException("[ERROR] Missing end time for edit event");
    }
    String endStr = tokens[idx + 1];
    idx += 2;

    if (idx >= tokens.length || !tokens[idx].equalsIgnoreCase("with")) {
      throw new InvalidCommandException("[ERROR] Missing 'with' for edit event");
    }

    if (idx + 1 >= tokens.length) {
      throw new InvalidCommandException("[ERROR] Missing new value for edit event");
    }

    String newValue = String.join(" ", Arrays.copyOfRange(tokens, idx + 1,
            tokens.length));
    return new EditEventCommand(subject, startStr, endStr, property, newValue);
  }

  private static CalendarCommand parseEditEvents(String[] tokens, int idx) throws
          InvalidCommandException {
    // edit events <property> <subject> from <start> with <newValue>
    if (idx + 1 >= tokens.length) {
      throw new InvalidCommandException("[ERROR] Missing property for edit events");
    }

    String property = tokens[idx];
    idx++;
    List<String> subjectParts = new ArrayList<>();
    while (idx < tokens.length && !tokens[idx].equalsIgnoreCase("from")) {
      subjectParts.add(tokens[idx]);
      idx++;
    }
    if (subjectParts.isEmpty()) {
      throw new InvalidCommandException("[ERROR] Missing subject for edit events");
    }

    String subject = String.join(" ", subjectParts);

    if (idx >= tokens.length || !tokens[idx].equalsIgnoreCase("from")) {
      throw new InvalidCommandException("[ERROR] Missing 'from' for edit events");
    }


    if (idx < tokens.length && tokens[idx].equalsIgnoreCase("to")) {
      idx++;
      if (idx < tokens.length) {
        idx++;
      }
    }

    if (idx + 1 >= tokens.length) {
      throw new InvalidCommandException("[ERROR] Missing start time for edit events");
    }

    String startStr = tokens[idx + 1];
    idx += 2;
    if (idx >= tokens.length || !tokens[idx].equalsIgnoreCase("with")) {
      throw new InvalidCommandException("[ERROR] Missing 'with' for edit events");
    }

    if (idx + 1 >= tokens.length) {
      throw new InvalidCommandException("[ERROR] Missing new value for edit events");
    }

    String newValue = String.join(" ", Arrays.copyOfRange(tokens, idx + 1,
            tokens.length));

    return model -> {
      try {
        LocalDateTime startDT = LocalDateTime.parse(startStr,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        List<Event> onDay = model.getEventsOn(startDT.toLocalDate());
        EventId foundId = null;
        for (Event e : onDay) {
          if (e.getSubject().equals(subject) && e.getStart().equals(startDT)) {
            foundId = new EventId(e.getSubject(), e.getStart(), e.getEnd());
            break;
          }
        }
        if (foundId == null) {
          return String.format("[ERROR] No event found to edit from: \"%s\" from %s",
                  subject, startDT);
        }
        model.editEventsFrom(foundId, property, newValue);
        return String.format("[OK] Updated '%s' for all occurrences from %s", property, startDT);
      } catch (DateTimeParseException e) {
        return "[ERROR] Invalid datetime format. Use 'YYYY-MM-DDTHH:MM'.";
      } catch (Exception e) {
        return "[ERROR] " + e.getMessage();
      }
    };
  }

  private static CalendarCommand parseEditSeries(String[] tokens, int idx) throws
          InvalidCommandException {
    //edit series <property> <subject> from <start> with <newValue>
    if (idx + 1 >= tokens.length) {
      throw new InvalidCommandException("[ERROR] Missing property for edit series");
    }

    String property = tokens[idx];
    idx++;
    List<String> subjectParts = new ArrayList<>();
    while (idx < tokens.length && !tokens[idx].equalsIgnoreCase("from")) {
      subjectParts.add(tokens[idx]);
      idx++;
    }

    if (subjectParts.isEmpty()) {
      throw new InvalidCommandException("[ERROR] Missing subject for edit series");
    }

    String subject = String.join(" ", subjectParts);

    if (idx >= tokens.length || !tokens[idx].equalsIgnoreCase("from")) {
      throw new InvalidCommandException("[ERROR] Missing 'from' for edit series");
    }

    if (idx + 1 >= tokens.length) {
      throw new InvalidCommandException("[ERROR] Missing start time for edit series");
    }


    if (idx < tokens.length && tokens[idx].equalsIgnoreCase("to")) {
      idx++;
      if (idx < tokens.length) {
        idx++;
      }
    }

    String startStr = tokens[idx + 1];
    idx += 2;

    if (idx >= tokens.length || !tokens[idx].equalsIgnoreCase("with")) {
      throw new InvalidCommandException("[ERROR] Missing 'with' for edit series");
    }

    if (idx + 1 >= tokens.length) {
      throw new InvalidCommandException("[ERROR] Missing new value for edit series");
    }

    String newValue = String.join(" ", Arrays.copyOfRange(tokens, idx + 1,
            tokens.length));
    return model -> {

      try {
        LocalDateTime startDT = LocalDateTime.parse(startStr,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        model.editSeries(subject, startDT, property, newValue);
        return String.format("[OK] Updated '%s' for entire series '%s' starting at %s",
                property, subject, startDT);

      } catch (NoSuchElementException e) {

        return "[ERROR] No series found for subject '" + subject + "' with start " + startStr;
      } catch (Exception e) {
        return "[ERROR] " + e.getMessage();
      }
    };
  }


  /**
   * Safely parses user input into a CalendarCommand without throwing exceptions.
   *
   * @param input the raw user input string
   * @return an Optional containing the parsed CalendarCommand, or empty if parsing fails
   */
  public static Optional<CalendarCommand> safeParse(String input) {
    try {
      return Optional.of(parse(input));
    } catch (InvalidCommandException ex) {
      return Optional.empty();
    }
  }

  /**
   * Represents the result of a command parse attempt,
   * containing either a CalendarCommand or an error message.
   */
  public static class ParseResult {
    public final Optional<CalendarCommand> command;
    public final String errorMessage;

    public ParseResult(Optional<CalendarCommand> command, String errorMessage) {
      this.command = command;
      this.errorMessage = errorMessage;
    }
  }


  /**
   * Safely attempts to parse the given input into a CalendarCommand.
   * Returns a ParseResult containing either the command or an error message.
   *
   * @param input the raw input string to parse
   * @return a ParseResult containing the command if successful, or an error message if not
   */
  public static ParseResult safeParseWithError(String input) {
    try {
      return new ParseResult(Optional.of(parse(input)), null);
    } catch (InvalidCommandException ex) {
      return new ParseResult(Optional.empty(), ex.getMessage());
    }
  }


}
