package controller.commands;

import calendarapp.controller.commands.PrintEventsCommand;
import calendarapp.model.CalendarModel;
import calendarapp.model.Event;
import calendarapp.model.EventId;
import calendarapp.model.EventSeries;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the PrintEventsCommand class.
 * Validates output for event queries on specific dates or within time ranges.
 */
public class PrintEventsCommandTest {

  private CalendarModel calendar;

  @Before
  public void setUp() {
    calendar = new CalendarModel() {
      @Override
      public void addEvent(Event event) {
        //Stub method for testing
      }

      @Override
      public void addEventSeries(EventSeries s) {
        //Stub method for testing
      }

      @Override
      public List<Event> getEventsOn(LocalDate date) {
        if (date.equals(LocalDate.of(2025, 7, 4))) {
          return Arrays.asList(new Event("Independence Day",
                  LocalDateTime.of(2025, 7, 4, 9, 0),
                  LocalDateTime.of(2025, 7, 4, 10, 0),
                  "Parade", "Boston", true));
        } else if (date.equals(LocalDate.of(2024, 2, 29))) {
          return Arrays.asList(new Event("Leap Day Special",
                  LocalDateTime.of(2024, 2, 29, 12, 0),
                  LocalDateTime.of(2024, 2, 29, 13, 0),
                  "Celebration", "New York", true));
        }
        return Collections.emptyList();
      }

      @Override
      public List<Event> getEventsBetween(LocalDateTime from, LocalDateTime to) {
        if (from.equals(LocalDateTime.of(2025, 7, 4, 0, 0))
                && to.equals(LocalDateTime.of(2025, 7, 5,
                0, 0))) {
          return Arrays.asList(
                  new Event("Event A",
                          LocalDateTime.of(2025, 7, 4, 10, 0),
                          LocalDateTime.of(2025, 7, 4, 11, 0),
                          "Meeting", null, false),
                  new Event("Event B",
                          LocalDateTime.of(2025, 7, 4, 13, 0),
                          LocalDateTime.of(2025, 7, 4, 14, 0),
                          null, null, true)
          );
        }
        return Collections.emptyList();
      }

      @Override
      public boolean isBusyAt(LocalDateTime dt) {
        return false;
      }

      @Override
      public Event getEventById(EventId id) {
        return null;
      }

      @Override
      public void editEvent(EventId id, Event event) {
        //Stub method for testing
      }

      @Override
      public void editEventsFrom(EventId id, String p, String val) {
        //Stub method for testing
      }

      @Override
      public void editSeries(String sub, LocalDateTime start, String p, String val) {
        //Stub method for testing
      }

      @Override
      public List<Event> getEventsFrom(LocalDate start, int maxCount) {
        return Collections.emptyList();
      }

      @Override
      public void deleteEvent(EventId id) {
        //do nothing for testing
      }


    };
  }

  @Test
  //Tests event retrieval for a specific date with events.
  public void testPrintEventsOnDateWithResults() {
    PrintEventsCommand cmd = new PrintEventsCommand("2025-07-04");
    String result = cmd.execute(calendar);
    String expected = "[EVENTS ON 2025-07-04]\n"
            + "• Independence Day: 2025-07-04T09:00 to 2025-07-04T10:00 @ Boston - Parade [public]";
    assertEquals(expected, result);
  }

  @Test
  //Tests event retrieval for a specific date with no events.
  public void testPrintEventsOnDateNoResults() {
    PrintEventsCommand cmd = new PrintEventsCommand("2025-12-25");
    String result = cmd.execute(calendar);
    assertEquals("[NO EVENTS] on 2025-12-25", result);
  }

  @Test
  //Tests event retrieval for a time range with multiple events.
  public void testPrintEventsBetweenDatesWithResults() {
    PrintEventsCommand cmd = new PrintEventsCommand("2025-07-04T00:00", "2025-07-05T00:00");
    String result = cmd.execute(calendar);
    String expected = "[EVENTS FROM 2025-07-04T00:00 TO 2025-07-05T00:00]\n"
            + "• Event A: 2025-07-04T10:00 to 2025-07-04T11:00 - Meeting [private]\n"
            + "• Event B: 2025-07-04T13:00 to 2025-07-04T14:00 [public]";
    assertEquals(expected, result);
  }

  @Test
  //Tests event retrieval for a time range with no events.
  public void testPrintEventsBetweenDatesNoResults() {
    PrintEventsCommand cmd = new PrintEventsCommand("2025-08-01T00:00", "2025-08-02T00:00");
    String result = cmd.execute(calendar);
    assertEquals("[NO EVENTS] from 2025-08-01T00:00 to 2025-08-02T00:00", result);
  }

  @Test
  //Tests invalid format for single date.
  public void testInvalidDateFormatInSingleDate() {
    PrintEventsCommand cmd = new PrintEventsCommand("20250704");
    String result = cmd.execute(calendar);
    assertEquals("[ERROR] Invalid date or time format. Use YYYY-MM-DD or "
            + "YYYY-MM-DDTHH:MM", result);
  }

  @Test
  //Tests invalid format in date range (start).
  public void testInvalidDateTimeFormatInRange() {
    PrintEventsCommand cmd = new PrintEventsCommand("2025-07-04 00:00", "2025-07-05T00:00");
    String result = cmd.execute(calendar);
    assertEquals("[ERROR] Invalid date or time format. Use YYYY-MM-DD or "
            + "YYYY-MM-DDTHH:MM", result);
  }

  @Test
  //Tests malformed end date in range.
  public void testMalformedToDateInRange() {
    PrintEventsCommand cmd = new PrintEventsCommand("2025-07-04T00:00", "July 5th");
    String result = cmd.execute(calendar);
    assertEquals("[ERROR] Invalid date or time format. Use YYYY-MM-DD or "
            + "YYYY-MM-DDTHH:MM", result);
  }

  @Test
  //Tests that datetime is rejected in single date constructor.
  public void testSingleConstructorWithDateTimeFormat() {
    PrintEventsCommand cmd = new PrintEventsCommand("2025-07-04T09:00");
    String result = cmd.execute(calendar);
    assertEquals("[ERROR] Invalid date or time format. Use YYYY-MM-DD or "
            + "YYYY-MM-DDTHH:MM", result);
  }

  @Test
  //Tests that reversed date range still returns a no-events message.
  public void testRangeStartAfterEnd() {
    PrintEventsCommand cmd = new PrintEventsCommand("2025-07-05T00:00", "2025-07-04T00:00");
    String result = cmd.execute(calendar);
    assertEquals("[NO EVENTS] from 2025-07-05T00:00 to 2025-07-04T00:00", result);
  }

  @Test
  //Tests whitespace-padded input string for date.
  public void testSingleDateWithWhitespace() {
    PrintEventsCommand cmd = new PrintEventsCommand(" 2025-07-04 ");
    String result = cmd.execute(calendar);
    assertEquals("[ERROR] Invalid date or time format. Use YYYY-MM-DD or "
            + "YYYY-MM-DDTHH:MM", result);
  }

  @Test
  //Tests valid leap day event retrieval.
  public void testLeapYearDateEvent() {
    PrintEventsCommand cmd = new PrintEventsCommand("2024-02-29");
    String result = cmd.execute(calendar);
    String expected = "[EVENTS ON 2024-02-29]\n"
            + "• Leap Day Special: 2024-02-29T12:00 to 2024-02-29T13:00 @ New York - "
            + "Celebration [public]";
    assertEquals(expected, result);
  }

  @Test
  //Tests input with empty string.
  public void testEmptyInputDate() {
    PrintEventsCommand cmd = new PrintEventsCommand("");
    String result = cmd.execute(calendar);
    assertEquals("[ERROR] Invalid date or time format. Use YYYY-MM-DD or "
            + "YYYY-MM-DDTHH:MM", result);
  }

  @Test
  public void testPrintEventsRangeZeroDuration() {
    PrintEventsCommand cmd = new PrintEventsCommand("2025-07-04T10:00", "2025-07-04T10:00");
    String result = cmd.execute(calendar);
    assertEquals("[NO EVENTS] from 2025-07-04T10:00 to 2025-07-04T10:00", result);
  }


}
