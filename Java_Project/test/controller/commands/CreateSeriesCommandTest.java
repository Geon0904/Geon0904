package controller.commands;

import calendarapp.controller.commands.CreateSeriesCommand;
import calendarapp.controller.commands.CalendarCommand;
import calendarapp.exceptions.DuplicateEventException;
import calendarapp.model.CalendarModel;
import calendarapp.model.CalendarImpl;
import calendarapp.model.EventSeries;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the CreateSeriesCommand class.
 * Verifies proper creation and validation of recurring event series.
 */
public class CreateSeriesCommandTest {

  private CalendarModel model;

  @Before
  public void setUp() {
    model = new CalendarImpl();
  }

  // Tests successful creation of a recurring event series using repeat count.
  @Test
  public void testCreateSeriesSuccessWithCount() {
    CalendarCommand command = new CreateSeriesCommand(
            "Study Session",
            "2025-06-01T10:00",
            "2025-06-01T11:00",
            "MWF",
            "6",
            false
    );
    String result = command.execute(model);
    assertTrue(result.startsWith("[OK] Created event series 'Study Session' ("));
  }

  // Tests successful creation of a recurring event series using 'until' end date.
  @Test
  public void testCreateSeriesSuccessWithUntil() {
    CalendarCommand command = new CreateSeriesCommand(
            "Class",
            "2025-06-03T09:00",
            "2025-06-03T10:00",
            "TR",
            "2025-06-17",
            true
    );
    String result = command.execute(model);
    assertTrue(result.startsWith("[OK] Created event series 'Class' ("));
  }

  // Verifies error is returned if an event crosses over into the next day.
  @Test
  public void testEndDateDifferentDayFromStart() {
    CalendarCommand command = new CreateSeriesCommand(
            "Split Day Event",
            "2025-06-01T22:00",
            "2025-06-02T01:00",
            "MW",
            "5",
            false
    );
    String result = command.execute(model);
    assertEquals("[ERROR] Each event in a series must start and end on the same day.",
            result);
  }

  // Verifies error is returned for incorrectly formatted start datetime.
  @Test
  public void testInvalidDateFormat() {
    CalendarCommand command = new CreateSeriesCommand(
            "Bad Format",
            "2025/06/01 10:00",
            "2025-06-01T11:00",
            "MW",
            "5",
            false
    );
    String result = command.execute(model);
    assertEquals("[ERROR] Invalid date or time format. Use 'YYYY-MM-DDTHH:MM' "
            + "and 'YYYY-MM-DD'.", result);
  }

  // Verifies error is returned when repeat count is not a valid integer.
  @Test
  public void testInvalidCountFormat() {
    CalendarCommand command = new CreateSeriesCommand(
            "Invalid Count",
            "2025-06-01T10:00",
            "2025-06-01T11:00",
            "MW",
            "five",
            false
    );
    String result = command.execute(model);
    assertEquals("[ERROR] Invalid repeat count. Must be an integer.", result);
  }

  // Verifies error is returned when the pattern contains invalid weekday characters.
  @Test
  public void testInvalidWeekdayCharacter() {
    CalendarCommand command = new CreateSeriesCommand(
            "Bad Day",
            "2025-06-01T10:00",
            "2025-06-01T11:00",
            "MX",
            "3",
            false
    );
    String result = command.execute(model);
    assertEquals("[ERROR] Invalid weekday character: X", result);
  }

  // Verifies DuplicateEventException is caught and reported correctly.
  @Test
  public void testDuplicateEventSeriesException() {
    CalendarModel duplicateModel = new CalendarImpl() {
      @Override
      public void addEventSeries(EventSeries s) throws DuplicateEventException {
        throw new DuplicateEventException("Series already exists");
      }
    };
    CalendarCommand command = new CreateSeriesCommand(
            "Duplicate Series",
            "2025-06-01T10:00",
            "2025-06-01T11:00",
            "MW",
            "3",
            false
    );
    String result = command.execute(duplicateModel);
    assertEquals("[ERROR] Series already exists", result);
  }

  // Verifies IllegalArgumentException is thrown when count is zero.
  @Test(expected = IllegalArgumentException.class)
  public void testCreateSeriesWithZeroCount() {
    CalendarCommand command = new CreateSeriesCommand(
            "Zero Count",
            "2025-06-01T10:00",
            "2025-06-01T11:00",
            "MWF",
            "0",
            false
    );
    command.execute(model);
  }

  // Verifies IllegalArgumentException is thrown when count is negative.
  @Test(expected = IllegalArgumentException.class)
  public void testCreateSeriesWithNegativeCount() {
    CalendarCommand command = new CreateSeriesCommand(
            "Negative Count",
            "2025-06-01T10:00",
            "2025-06-01T11:00",
            "TR",
            "-5",
            false
    );
    command.execute(model);
  }

  // Verifies IllegalArgumentException is thrown when pattern is empty.
  @Test(expected = IllegalArgumentException.class)
  public void testCreateSeriesWithEmptyPattern() {
    CalendarCommand command = new CreateSeriesCommand(
            "Empty Pattern",
            "2025-06-01T10:00",
            "2025-06-01T11:00",
            "",
            "3",
            false
    );
    command.execute(model);
  }

  // Verifies lowercase weekday characters are accepted as valid input.
  @Test
  public void testCreateSeriesWithLowercasePattern() {
    CalendarCommand command = new CreateSeriesCommand(
            "Lowercase Pattern",
            "2025-06-01T10:00",
            "2025-06-01T11:00",
            "mwf",
            "3",
            false
    );
    String result = command.execute(model);
    assertTrue(result.startsWith("[OK] Created event series 'Lowercase Pattern' ("));
  }

  // Verifies that a backward 'until' date results in a single occurrence.
  @Test
  public void testCreateSeriesUntilBeforeStart() {
    CalendarCommand command = new CreateSeriesCommand(
            "Backward Until",
            "2025-06-03T09:00",
            "2025-06-03T10:00",
            "W",
            "2025-06-02",
            true
    );
    String result = command.execute(model);
    assertTrue(result.startsWith("[OK] Created event series 'Backward Until' (1 events)"));
  }

  // Verifies that an 'until' date equal to start results in a single occurrence.
  @Test
  public void testCreateSeriesUntilEqualsStartDate() {
    CalendarCommand command = new CreateSeriesCommand(
            "Exact Until",
            "2025-06-03T09:00",
            "2025-06-03T10:00",
            "W",
            "2025-06-03",
            true
    );
    String result = command.execute(model);
    assertTrue(result.startsWith("[OK] Created event series 'Exact Until' (1 events)"));
  }

  // Verifies error is returned for invalid 'until' date format.
  @Test
  public void testCreateSeriesWithInvalidUntilFormat() {
    CalendarCommand command = new CreateSeriesCommand(
            "Bad Until Format",
            "2025-06-01T10:00",
            "2025-06-01T11:00",
            "MW",
            "06-17-2025",
            true
    );
    String result = command.execute(model);
    assertEquals("[ERROR] Invalid date format. Use 'YYYY-MM-DD'.", result);
  }

  // Verifies that a long-range 'until' generates multiple valid events.
  @Test
  public void testCreateSeriesWithLargeUntilRange() {
    CalendarCommand command = new CreateSeriesCommand(
            "Long Until",
            "2025-01-01T08:00",
            "2025-01-01T09:00",
            "U",
            "2025-12-31",
            true
    );
    String result = command.execute(model);
    assertTrue(result.startsWith("[OK] Created event series 'Long Until' ("));
  }


}