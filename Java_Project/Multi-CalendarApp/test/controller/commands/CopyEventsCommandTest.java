package controller.commands;

import calendarapp.controller.commands.CopyEventsCommand;
import calendarapp.model.CalendarManagerImpl;
import calendarapp.model.CalendarModel;
import calendarapp.model.Event;
import calendarapp.model.MultiCalendarModel;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the CopyEventsCommand class.
 * Verifies copying all events on a specific date between calendars.
 */
public class CopyEventsCommandTest {

  private MultiCalendarModel manager;
  private final LocalDate sourceDate = LocalDate.of(2025, 7, 20);
  private final LocalDate destDate = LocalDate.of(2025, 8, 20);

  @Before
  public void setUp() {
    manager = new CalendarManagerImpl();
    manager.createCalendar("SourceCal", ZoneId.of("UTC"));
    manager.createCalendar("TargetCal", ZoneId.of("America/New_York"));
    manager.useCalendar("SourceCal");

    // Add events on the source date
    manager.addEvent(new Event("Morning Event",
            sourceDate.atTime(9, 0), sourceDate.atTime(10, 0)));
    manager.addEvent(new Event("Afternoon Event",
            sourceDate.atTime(14, 0), sourceDate.atTime(15, 30)));
    // Add an event on a different day that should not be copied
    manager.addEvent(new Event("Wrong Day Event", sourceDate.plusDays(1)
            .atTime(9, 0), sourceDate.plusDays(1)
            .atTime(10, 0)));
  }

  // Tests successfully copying all events on a given date.
  @Test
  public void testCopyEventsOnDateSuccessfully() {
    CopyEventsCommand cmd = new CopyEventsCommand(sourceDate, "TargetCal", destDate);
    String result = cmd.execute((CalendarModel) manager);
    String expected = String.format(
            "[OK] Copied 2 events from %s to calendar 'TargetCal' on %s",
            sourceDate, destDate
    );
    assertEquals(expected, result);

    // Switch to the target calendar to verify the events were copied there
    manager.useCalendar("TargetCal");

    // Verify events exist in target calendar
    assertEquals(2, manager.getActiveCalendar().getEventsOn(destDate).size());
  }

  // Tests copying from a date that has no events.
  @Test
  public void testCopyEventsOnDateWithNoEvents() {
    LocalDate emptyDate = LocalDate.of(2025, 1, 1);
    CopyEventsCommand cmd = new CopyEventsCommand(emptyDate, "TargetCal", destDate);
    String result = cmd.execute((CalendarModel) manager);
    assertEquals(
            "[OK] Copied 0 events from " + emptyDate + " to calendar 'TargetCal' on "
                    + destDate,
            result
    );
  }

  // Tests copying events to a non-existent calendar.
  @Test
  public void testCopyEventsToNonExistentCalendar() {
    CopyEventsCommand cmd = new CopyEventsCommand(sourceDate, "FakeCal", destDate);
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[ERROR] No calendar named 'FakeCal'.", result);
  }

  // Tests copying events to the same calendar but on a different date.
  @Test
  public void testCopyEventsToSameCalendarOnDifferentDate() {
    CopyEventsCommand cmd = new CopyEventsCommand(sourceDate, "SourceCal", destDate);
    String result = cmd.execute((CalendarModel) manager);
    assertEquals(
            "[OK] Copied 2 events from " + sourceDate + " to calendar 'SourceCal' on "
                    + destDate,
            result
    );
    assertEquals(2, manager.getActiveCalendar().getEventsOn(destDate).size());
  }

  // Tests that copying a day with events twice doesn't duplicate them in the target.
  @Test
  public void testCopyEventsHandlesDuplicates() {
    CopyEventsCommand cmd1 = new CopyEventsCommand(sourceDate, "TargetCal", destDate);
    cmd1.execute((CalendarModel) manager); // First copy

    CopyEventsCommand cmd2 = new CopyEventsCommand(sourceDate, "TargetCal", destDate);
    String result = cmd2.execute((CalendarModel) manager); // Second copy should copy 0 new events
    assertEquals(
            "[OK] Copied 0 events from " + sourceDate + " to calendar 'TargetCal' on "
                    + destDate, result
    );
  }

  @Test
  public void testCopyEventsSameSourceAndDestDate() {
    CopyEventsCommand cmd = new CopyEventsCommand(sourceDate, "TargetCal", sourceDate);
    cmd.execute((CalendarModel) manager);
    String result = cmd.execute((CalendarModel) manager);
    assertEquals(
            "[OK] Copied 0 events from " + sourceDate + " to calendar 'TargetCal' on "
                    + sourceDate, result
    );
  }

  @Test
  public void testCopyEventsTargetCalendarCaseSensitivity() {
    CopyEventsCommand cmd = new CopyEventsCommand(sourceDate, "targetcal", destDate);
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[ERROR] No calendar named 'targetcal'.", result);
  }
}