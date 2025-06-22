package controller;


import calendarapp.controller.CommandParser;
import calendarapp.controller.commands.CalendarCommand;
import calendarapp.controller.commands.CopyEventCommand;
import calendarapp.controller.commands.CopyEventsCommand;
import calendarapp.controller.commands.CopyEventsRangeCommand;
import calendarapp.controller.commands.CreateCalendarCommand;
import calendarapp.controller.commands.CreateEventCommand;
import calendarapp.controller.commands.CreateSeriesCommand;
import calendarapp.controller.commands.PrintEventsCommand;
import calendarapp.controller.commands.ShowStatusCommand;
import calendarapp.controller.commands.UseCalendarCommand;
import calendarapp.exceptions.InvalidCommandException;

import org.junit.Test;


import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;


/**
 * Unit tests for the CommandParser class.
 * Verifies correct parsing and error handling of all supported command formats.
 */
public class CommandParserTest {

  private static class DummyCalendarModel implements calendarapp.model.CalendarModel {
    @Override
    public void addEvent(calendarapp.model.Event event) {
      //
    }

    @Override
    public void addEventSeries(calendarapp.model.EventSeries s) {
      //
    }

    @Override
    public java.util.List<calendarapp.model.Event> getEventsOn(java.time.LocalDate date) {
      return java.util.Collections.emptyList();
    }

    @Override
    public java.util.List<calendarapp.model.Event> getEventsBetween(java.time.LocalDateTime from,
                                                                    java.time.LocalDateTime to) {
      return java.util.Collections.emptyList();
    }

    @Override
    public boolean isBusyAt(java.time.LocalDateTime dt) {
      return false;
    }

    @Override
    public calendarapp.model.Event getEventById(calendarapp.model.EventId id) {
      return null;
    }

    @Override
    public void editEvent(calendarapp.model.EventId id, calendarapp.model.Event event) {
      //
    }

    @Override
    public void editEventsFrom(calendarapp.model.EventId id, String p, String val) {
      //
    }

    @Override
    public void editSeries(String sub, java.time.LocalDateTime start, String p, String val) {
      //
    }

    @Override
    public java.util.List<calendarapp.model.Event> getEventsFrom(java.time.LocalDate start,
                                                                 int max) {
      return java.util.Collections.emptyList();
    }

    @Override
    public void deleteEvent(calendarapp.model.EventId id) {
      //do nothing
    }

  }


  //Missing args in legacy create-event should throw
  @Test(expected = InvalidCommandException.class)
  public void parseLegacyCreateEventMissingArgs() throws InvalidCommandException {
    CommandParser.parse("create-event,Meeting,2025-07-01T09:00");
  }


  //create-calendar command parses with name+timezone
  @Test
  public void parseCreateCalendar() throws InvalidCommandException {
    CalendarCommand cmd = CommandParser.parse(
            "create calendar --name Work --timezone America/New_York"
    );
    assertTrue(cmd instanceof CreateCalendarCommand);
  }


  //create-calendar with bad syntax throws
  @Test(expected = InvalidCommandException.class)
  public void parseCreateCalendarBadSyntax() throws InvalidCommandException {
    CommandParser.parse("create calendar name Work timezone EST");
  }

  //use-calendar command parses
  @Test
  public void parseUseCalendar() throws InvalidCommandException {
    CalendarCommand cmd = CommandParser.parse("use calendar Work");
    assertTrue(cmd instanceof UseCalendarCommand);
  }

  //use-calendar wrong syntax throws
  @Test(expected = InvalidCommandException.class)
  public void parseUseCalendarBad() throws InvalidCommandException {
    CommandParser.parse("use Work");
  }

  //create-event  parses
  @Test
  public void parseCreateEventSpaceStyle() throws InvalidCommandException {
    CalendarCommand cmd = CommandParser.parse("create event TeamMeeting from "
            + "2025-08-01T09:00 to 2025-08-01T10:00");
    assertTrue(cmd instanceof CreateEventCommand);
  }

  //create-event missing “from” throws
  @Test(expected = InvalidCommandException.class)
  public void parseCreateEventSpaceMissingFrom() throws InvalidCommandException {
    CommandParser.parse("create event TeamMeeting 2025-08-01T09:00 to 2025-08-01T10:00");
  }

  //create-series parses count
  @Test
  public void parseCreateSeriesSpaceCount() throws InvalidCommandException {
    CalendarCommand cmd = CommandParser.parse(
            "create series Yoga from 2025-08-01T07:00 to 2025-08-01T08:00 repeats MWF for 3"
    );
    assertTrue(cmd instanceof CreateSeriesCommand);
  }

  //create-series parses until
  @Test
  public void parseCreateSeriesSpaceUntil() throws InvalidCommandException {
    CalendarCommand cmd = CommandParser.parse(
            "create series Class from 2025-09-01T09:00 to 2025-09-01T10:00 "
                    + "repeats TR until 2025-09-30"
    );
    assertTrue(cmd instanceof CreateSeriesCommand);
  }

  //print-events on date parses
  @Test
  public void parsePrintEventsOn() throws InvalidCommandException {
    CalendarCommand cmd = CommandParser.parse("print events on 2025-10-05");
    assertTrue(cmd instanceof PrintEventsCommand);
  }


  //print-events range parses
  @Test
  public void parsePrintEventsRange() throws InvalidCommandException {
    CalendarCommand cmd = CommandParser.parse("print events from 2025-10-05T00:00 to "
            + "2025-10-05T23:59");
    assertTrue(cmd instanceof PrintEventsCommand);
  }

  //show status parses.
  @Test
  public void parseShowStatus() throws InvalidCommandException {
    CalendarCommand cmd = CommandParser.parse("show status on 2025-10-05T12:00");
    assertTrue(cmd instanceof ShowStatusCommand);
  }


  //copy-event parses
  @Test
  public void parseCopyEvent() throws InvalidCommandException {
    CalendarCommand cmd = CommandParser.parse(
            "copy event Meeting on 2025-11-01T09:00 --target Work to 2025-11-15T09:00"
    );
    assertTrue(cmd instanceof CopyEventCommand);
  }

  //copy-events-on parses
  @Test
  public void parseCopyEventsOn() throws InvalidCommandException {
    CalendarCommand cmd = CommandParser.parse(
            "copy events on 2025-11-01 --target Work to 2025-12-01"
    );
    assertTrue(cmd instanceof CopyEventsCommand);
  }

  //copy events between parses
  @Test
  public void parseCopyEventsBetween() throws InvalidCommandException {
    CalendarCommand cmd = CommandParser.parse(
            "copy events between 2025-11-01 and 2025-11-07 --target Work to 2025-12-01"
    );
    assertTrue(cmd instanceof CopyEventsRangeCommand);
  }

  //safeParse returns empty on bad command
  @Test
  public void testSafeParseEmpty() {
    Optional<CalendarCommand> opt = CommandParser.safeParse("this is nonsense");
    assertFalse(opt.isPresent());
  }


  // Empty command throws
  @Test(expected = InvalidCommandException.class)
  public void parseEmptyCommand() throws InvalidCommandException {
    CommandParser.parse("");
  }

  // Unknown command throws
  @Test(expected = InvalidCommandException.class)
  public void parseUnknownCommand() throws InvalidCommandException {
    CommandParser.parse("delete calendar Work");
  }

  // create-event with invalid datetime format throws
  @Test(expected = InvalidCommandException.class)
  public void parseCreateEventInvalidDateTime() throws InvalidCommandException {
    CommandParser.parse("create event BadDate from 2025/08/01 09:00 to 2025-08-01T10:00");
  }

  // create-series with invalid repeat pattern throws
  @Test(expected = InvalidCommandException.class)
  public void parseCreateSeriesInvalidPattern() throws InvalidCommandException {
    CommandParser.parse("create series BadPattern from 2025-08-01T07:00 to "
            + "2025-08-01T08:00 repeats XYZ for 3");
  }

  // edit-calendar with invalid property throws
  @Test(expected = InvalidCommandException.class)
  public void parseEditCalendarInvalidProperty() throws InvalidCommandException {
    CommandParser.parse("edit calendar --name Work --property invalid value");
  }


  // create-calendar with invalid timezone throws
  @Test(expected = InvalidCommandException.class)
  public void parseCreateCalendarInvalidTimezone() throws InvalidCommandException {
    CommandParser.parse("create calendar --name Work --timezone Fake/Timezone");
  }

  // edit-event with missing 'with' clause throws
  @Test(expected = InvalidCommandException.class)
  public void parseEditEventMissingWith() throws InvalidCommandException {
    CommandParser.parse("edit event location TeamMeeting from 2025-08-01T09:00 "
            + "to 2025-08-01T10:00");
  }

  // create-event with multi-word subject parses
  @Test
  public void parseCreateEventMultiWordSubject() throws InvalidCommandException {
    CalendarCommand cmd = CommandParser.parse(
            "create event Quarterly Planning Meeting from 2025-08-01T09:00 to "
                    + "2025-08-01T10:00"
    );
    assertTrue(cmd instanceof CreateEventCommand);
  }

  // safeParse returns present for valid command
  @Test
  public void testSafeParsePresent() {
    Optional<CalendarCommand> opt = CommandParser.safeParse("create calendar --name Test "
            + "--timezone UTC");
    assertTrue(opt.isPresent());
  }

  // Handles leading/trailing whitespace in command
  @Test
  public void parseCommandWithWhitespace() throws InvalidCommandException {
    assertTrue(CommandParser.parse("  create event X from 2025-10-05T10:00 to "
            + "2025-10-05T11:00  ") instanceof CreateEventCommand);
  }


  // Throws if legacy create-event mixes comma and space
  @Test(expected = InvalidCommandException.class)
  public void parseLegacyCreateEventWithCommaAndSpace() throws InvalidCommandException {
    CommandParser.parse("create-event,Meeting 2025-07-01T09:00,2025-07-01T10:00,"
            + "Discuss,Room,true");
  }

  // Throws if create calendar is missing timezone option
  @Test(expected = InvalidCommandException.class)
  public void parseCreateCalendarMissingTimezone() throws InvalidCommandException {
    CommandParser.parse("create calendar --name Work");
  }

  // Throws if create series uses bad weekday pattern
  @Test(expected = InvalidCommandException.class)
  public void parseCreateSeriesWithInvalidWeekday() throws InvalidCommandException {
    CommandParser.parse("create series Bad from 2025-08-01T07:00 to "
            + "2025-08-01T08:00 repeats Q for 3");
  }

  // Throws if edit event missing property/value
  @Test(expected = InvalidCommandException.class)
  public void parseEditEventMissingProperty() throws InvalidCommandException {
    CommandParser.parse("edit event subject Meeting from 2025-07-01T09:00 "
            + "to 2025-07-01T10:00");
  }




  @Test(expected = InvalidCommandException.class)
  public void parseLegacyCreateEvent() throws Exception {
    CommandParser.parse("create-event,Meeting,2025-07-01T09:00,2025-07-01T10:00,Discuss," +
            "Room,true");
  }


  // create event with default time (end omitted)
  // Throws if create event omits required end time
  @Test
  public void parseCreateEventWithDefaultEndTime() throws Exception {
    CalendarCommand cmd = CommandParser.parse("create event FocusTime from 2025-11-01T09:00");
    String result = cmd.execute(new DummyCalendarModel());

    assertTrue(result.contains("FocusTime") && result.contains("2025-11-01T09:00"));
  }


  // create series with multi-word subject and spaces
  @Test
  public void parseCreateSeriesMultiWordSubject() throws InvalidCommandException {
    CalendarCommand cmd = CommandParser.parse(
            "create series Early Morning Run from 2025-07-01T06:00 to 2025-07-01T07:00" +
                    " repeats MWF for 3"
    );
    assertTrue(cmd instanceof CreateSeriesCommand);
  }

  // print events on with leading/trailing spaces
  @Test
  public void parsePrintEventsOnWithSpaces() throws InvalidCommandException {
    CalendarCommand cmd = CommandParser.parse("  print events on 2025-10-05  ");
    assertTrue(cmd instanceof PrintEventsCommand);
  }

  // print events range where from == to
  @Test
  public void parsePrintEventsRangeSameFromTo() throws InvalidCommandException {
    CalendarCommand cmd = CommandParser.parse("print events from 2025-10-05T09:00 to " +
            "2025-10-05T09:00");
    assertTrue(cmd instanceof PrintEventsCommand);
  }

  // Throws if legacy create-series is missing repeat or end/count
  @Test(expected = InvalidCommandException.class)
  public void parseLegacyCreateSeriesMissingRepeat() throws InvalidCommandException {
    CommandParser.parse("create-series,Workout,2025-07-01T07:00,2025-07-01T08:00");
  }

  // Throws if legacy edit-event has too few args
  @Test(expected = InvalidCommandException.class)
  public void parseLegacyEditEventMissingArgs() throws InvalidCommandException {
    CommandParser.parse("edit-event,Meeting,2025-07-01T09:00");
  }

  // Throws if create event omits both "from" and "to"
  @Test(expected = InvalidCommandException.class)
  public void parseCreateEventMissingFromTo() throws InvalidCommandException {
    CommandParser.parse("create event Test 2025-07-01T09:00 2025-07-01T10:00");
  }

  // Throws if create series is missing repeats/for/until
  @Test(expected = InvalidCommandException.class)
  public void parseCreateSeriesMissingRepeats() throws InvalidCommandException {
    CommandParser.parse("create series Run from 2025-07-01T09:00 to 2025-07-01T10:00");
  }


  // Throws if print events on is missing the date
  @Test(expected = InvalidCommandException.class)
  public void parsePrintEventsOnMissingDate() throws InvalidCommandException {
    CommandParser.parse("print events on");
  }

  // Throws if print events from is missing end time
  @Test(expected = InvalidCommandException.class)
  public void parsePrintEventsFromMissingEnd() throws InvalidCommandException {
    CommandParser.parse("print events from 2025-10-05T00:00");
  }

  // Throws if edit calendar is missing property and value
  @Test(expected = InvalidCommandException.class)
  public void parseEditCalendarMissingProperty() throws InvalidCommandException {
    CommandParser.parse("edit calendar --name Work");
  }

  // Throws if show status on is missing datetime
  @Test(expected = InvalidCommandException.class)
  public void parseShowStatusMissingDateTime() throws InvalidCommandException {
    CommandParser.parse("show status on");
  }

  // Throws if copy events on is missing --target or to date
  @Test(expected = InvalidCommandException.class)
  public void parseCopyEventsOnMissingTarget() throws InvalidCommandException {
    CommandParser.parse("copy events on 2025-10-05 to 2025-11-05");
  }

  // Throws if copy events between is missing and or to
  @Test(expected = InvalidCommandException.class)
  public void parseCopyEventsBetweenMissingAnd() throws InvalidCommandException {
    CommandParser.parse("copy events between 2025-10-05 2025-10-08 --target Work to " +
            "2025-12-01");
  }

  // Throws if copy event missing "on" or "--target"
  @Test(expected = InvalidCommandException.class)
  public void parseCopyEventMissingOnOrTarget() throws InvalidCommandException {
    CommandParser.parse("copy event Meeting 2025-10-05T09:00 to 2025-10-15T09:00");
  }

  // Throws if random/unknown text is given
  @Test(expected = InvalidCommandException.class)
  public void parseCompletelyUnknownCommand() throws InvalidCommandException {
    CommandParser.parse("abracadabra foo bar baz");
  }

  @Test
  public void parseCreateCalendarMultiWordName() throws InvalidCommandException {
    CalendarCommand cmd = CommandParser.parse("create calendar --name \"Personal Work\"" +
            " --timezone Europe/Berlin");
    assertTrue(cmd instanceof CreateCalendarCommand);
  }

  @Test
  public void parseCommandExtraWhitespaceBetweenFlags() throws InvalidCommandException {
    CalendarCommand cmd = CommandParser.parse("create    event    Math    from    " +
            "2025-10-05T10:00    to   2025-10-05T11:00");
    assertTrue(cmd instanceof CreateEventCommand);
  }

  @Test
  public void parseEditCalendarPropertyUppercase() throws InvalidCommandException {
    CalendarCommand cmd = CommandParser.parse("edit calendar --name Work --property " +
            "TIMEZONE Europe/London");
    assertTrue(cmd != null);
  }






}
