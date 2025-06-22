package controller.commands;

import calendarapp.controller.commands.CreateEventCommand;
import calendarapp.exceptions.DuplicateEventException;
import calendarapp.model.CalendarImpl;
import calendarapp.model.CalendarModel;
import calendarapp.model.Event;
import calendarapp.model.EventId;
import calendarapp.model.EventSeries;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the CreateEventCommand class.
 * Verifies expected output and error handling for valid and invalid event creation cases.
 */
public class CreateEventCommandTest {

  private CalendarModel calendarModel;

  @Before
  public void setUp() {
    calendarModel = new CalendarModel() {
      @Override
      public void addEvent(Event event) throws DuplicateEventException {
        //No-op for dummy test implementation
      }

      @Override
      public void addEventSeries(EventSeries s) {
        //No-op for dummy test implementation
      }

      @Override
      public List<Event> getEventsOn(LocalDate date) {
        return Collections.emptyList();
      }

      @Override
      public List<Event> getEventsBetween(LocalDateTime from, LocalDateTime to) {
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
        //No-op for dummy test implementation
      }

      @Override
      public void editEventsFrom(EventId id, String prop, String val) {
        //No-op for dummy test implementation
      }

      @Override
      public void editSeries(String sub, LocalDateTime start, String p, String val) {
        //No-op for dummy test implementation
      }

      @Override
      public List<Event> getEventsFrom(LocalDate start, int maxCount) {
        return java.util.Collections.emptyList();
      }

      @Override
      public void deleteEvent(calendarapp.model.EventId id) {
        //do nothing
      }

    };
  }

  // Valid event creation with all fields
  @Test
  public void testExecuteSuccess() {
    CreateEventCommand command = new CreateEventCommand(
            "Team Meeting", "2025-07-04T09:00", "2025-07-04T10:00",
            "Weekly sync", "Conference Room A", true
    );
    String result = command.execute(calendarModel);
    assertEquals("[OK] Created event 'Team Meeting' "
            + "from 2025-07-04T09:00 to 2025-07-04T10:00.", result);
  }

  // Empty subject should throw IllegalArgumentException
  @Test(expected = IllegalArgumentException.class)
  public void testExecuteWithEmptySubject() {
    CreateEventCommand command = new CreateEventCommand(
            "", "2025-07-04T09:00", "2025-07-04T10:00",
            null, null, false
    );
    command.execute(calendarModel);
  }

  // Invalid date format in start time
  @Test
  public void testExecuteWithInvalidDateFormat() {
    CreateEventCommand command = new CreateEventCommand(
            "Invalid Date", "2025/07/04 09:00", "2025-07-04T10:00",
            null, null, false
    );
    String result = command.execute(calendarModel);
    assertEquals("[ERROR] Invalid date-time format. Use 'YYYY-MM-DDTHH:MM'.", result);
  }

  // End time is before start time
  @Test
  public void testExecuteWithEndBeforeStart() {
    CreateEventCommand command = new CreateEventCommand(
            "Backwards Event", "2025-07-04T10:00", "2025-07-04T09:00",
            null, null, false
    );
    String result = command.execute(calendarModel);
    assertEquals("[ERROR] End time must be after start time.", result);
  }

  // Start and end times are exactly the same
  @Test
  public void testExecuteWithSameStartEndTime() {
    CreateEventCommand command = new CreateEventCommand(
            "Instant Event", "2025-07-04T10:00", "2025-07-04T10:00",
            null, null, false
    );
    String result = command.execute(calendarModel);
    assertEquals("[ERROR] End time must be after start time.", result);
  }

  // Calendar throws DuplicateEventException
  @Test
  public void testExecuteWithDuplicateEvent() {
    CalendarModel throwingModel = new CalendarImpl() {
      @Override
      public void addEvent(Event event) throws DuplicateEventException {
        throw new DuplicateEventException("Event already exists");
      }
    };

    CreateEventCommand command = new CreateEventCommand(
            "Duplicate", "2025-07-04T10:00", "2025-07-04T11:00",
            null, null, false
    );
    String result = command.execute(throwingModel);
    assertEquals("[ERROR] Event already exists", result);
  }

  // Event that crosses over midnight into the next day
  @Test
  public void testExecuteWithOvernightEvent() {
    CreateEventCommand command = new CreateEventCommand(
            "Overnight", "2025-07-04T22:00", "2025-07-05T02:00",
            null, null, false
    );
    String result = command.execute(calendarModel);
    assertTrue(result.startsWith("[OK] Created event 'Overnight' from 2025-07-04T22:00"));
  }

  // Valid event with only required fields
  @Test
  public void testExecuteWithMinimalFields() {
    CreateEventCommand command = new CreateEventCommand(
            "Minimal", "2025-07-04T10:00", "2025-07-04T11:00",
            null, null, false
    );
    String result = command.execute(calendarModel);
    assertTrue(result.startsWith("[OK] Created event 'Minimal'"));
  }

  // Very long-duration event
  @Test
  public void testExecuteWithVeryLongDuration() {
    CreateEventCommand command = new CreateEventCommand(
            "Long Event", "2025-01-01T00:00", "2025-12-31T23:59",
            null, null, false
    );
    String result = command.execute(calendarModel);
    assertTrue(result.startsWith("[OK] Created event 'Long Event'"));
  }

  // Null start time should throw NullPointerException
  @Test(expected = NullPointerException.class)
  public void testExecuteWithNullStart() {
    CreateEventCommand command = new CreateEventCommand(
            "NullStart", null, "2025-07-04T10:00",
            null, null, true
    );
    command.execute(calendarModel);
  }

  // Null end time should default to 08:00–17:00 on same date
  @Test
  public void testExecuteWithNullEnd() {
    CreateEventCommand command = new CreateEventCommand(
            "NullEnd", "2025-07-04T09:00", null,
            null, null, false
    );
    String result = command.execute(calendarModel);
    assertEquals(
            "[OK] Created event 'NullEnd' from 2025-07-04T08:00 to 2025-07-04T17:00.",
            result
    );
  }

  // Invalid month (13) in date
  @Test
  public void testExecuteWithInvalidMonth() {
    CreateEventCommand command = new CreateEventCommand(
            "InvalidMonth", "2025-13-01T09:00", "2025-07-01T10:00",
            null, null, false
    );
    String result = command.execute(calendarModel);
    assertEquals("[ERROR] Invalid date-time format. Use 'YYYY-MM-DDTHH:MM'.", result);
  }

  // Time includes seconds, should still be valid
  @Test
  public void testExecuteWithSecondsInTime() {
    CreateEventCommand command = new CreateEventCommand(
            "WithSeconds", "2025-07-04T09:00:00", "2025-07-04T10:00:00",
            null, null, true
    );
    String result = command.execute(calendarModel);
    assertTrue(result.startsWith("[OK] Created event 'WithSeconds' from 2025-07-04T09:00"));
  }

  // Leading/trailing whitespace in time fields causes parse error
  @Test
  public void testExecuteWithWhitespaceAroundInput() {
    CreateEventCommand command = new CreateEventCommand(
            "TrimTest", " 2025-07-04T09:00 ", " 2025-07-04T10:00 ",
            "Desc", "Loc", true
    );
    String result = command.execute(calendarModel);
    assertEquals("[ERROR] Invalid date-time format. Use 'YYYY-MM-DDTHH:MM'.", result);
  }

  // Multi-year spanning event
  @Test
  public void testExecuteWithHugeDurationSpanningYears() {
    CreateEventCommand command = new CreateEventCommand(
            "YearSpanning", "2020-01-01T00:00", "2030-01-01T00:00",
            "Long duration", "Anywhere", false
    );
    String result = command.execute(calendarModel);
    assertTrue(result.startsWith("[OK] Created event 'YearSpanning' from 2020-01-01T00:00"));
  }

  // Subject contains special characters
  @Test
  public void testExecuteWithSpecialCharactersInSubject() {
    CreateEventCommand command = new CreateEventCommand(
            "Spec!@#", "2025-07-04T09:00", "2025-07-04T10:00",
            null, null, true
    );
    String result = command.execute(calendarModel);
    assertTrue(result.contains("'Spec!@#'"));
  }

  // Null subject should throw NullPointerException
  @Test(expected = IllegalArgumentException.class)
  public void testExecuteWithNullSubject() {
    CreateEventCommand command = new CreateEventCommand(
            null, "2025-07-04T09:00", "2025-07-04T10:00", null,
            null, true
    );
    command.execute(calendarModel);
  }

  @Test
  public void testCreateEventWithMaxLengthFields() {
    String maxLen = "A".repeat(255);
    CreateEventCommand command = new CreateEventCommand(
            maxLen, "2025-07-04T09:00", "2025-07-04T10:00", maxLen, maxLen, true);
    String result = command.execute(calendarModel);
    assertTrue(result.contains(maxLen));
  }


  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventWithWhitespaceSubject() {
    CreateEventCommand command = new CreateEventCommand(
            "   ", "2025-07-04T09:00", "2025-07-04T10:00", null,
            null, false);
    command.execute(calendarModel);
  }


}
