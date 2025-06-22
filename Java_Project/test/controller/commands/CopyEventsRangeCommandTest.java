package controller.commands;

import calendarapp.controller.commands.CopyEventsRangeCommand;
import calendarapp.model.CalendarManagerImpl;
import calendarapp.model.CalendarModel;
import calendarapp.model.Event;
import calendarapp.model.MultiCalendarModel;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the CopyEventsRangeCommand class.
 * Verifies copying events within a date range between calendars.
 */
public class CopyEventsRangeCommandTest {

  private MultiCalendarModel manager;
  private final LocalDate sourceStart = LocalDate.of(2025, 8, 10);
  private final LocalDate sourceEnd = LocalDate.of(2025, 8, 12);
  private final LocalDate destStart = LocalDate.of(2025, 9, 1);

  @Before
  public void setUp() {
    manager = new CalendarManagerImpl();
    manager.createCalendar("SourceCal", ZoneId.of("UTC"));
    manager.createCalendar("TargetCal", ZoneId.of("America/Chicago"));
    manager.useCalendar("SourceCal");

    // Add events inside and outside the source range
    manager.addEvent(new Event("Day 1 Event", sourceStart.atTime(10, 0),
            sourceStart.atTime(11, 0)));
    manager.addEvent(new Event("Day 2 Event", sourceStart.plusDays(1)
            .atTime(10, 0), sourceStart.plusDays(1)
            .atTime(11, 0)));
    manager.addEvent(new Event("Outside Range Event", sourceEnd.plusDays(2)
            .atTime(10, 0), sourceEnd.plusDays(2)
            .atTime(11, 0)));
  }

  // Tests successfully copying events within a date range.
  @Test
  public void testCopyEventsInRangeSuccessfully() {
    CopyEventsRangeCommand cmd = new CopyEventsRangeCommand(sourceStart, sourceEnd,
            "TargetCal", destStart);
    String result = cmd.execute((CalendarModel) manager);
    String expected = String.format(
            "[OK] Copied 2 events from %s–%s to calendar 'TargetCal' starting %s",
            sourceStart, sourceEnd, destStart
    );
    assertEquals(expected, result);
  }

  // Tests copying from a range that contains no events.
  @Test
  public void testCopyEventsInRangeWithNoEvents() {
    LocalDate emptyStart = LocalDate.of(2025, 1, 1);
    LocalDate emptyEnd = LocalDate.of(2025, 1, 31);
    CopyEventsRangeCommand cmd = new CopyEventsRangeCommand(emptyStart, emptyEnd,
            "TargetCal", destStart);
    String result = cmd.execute((CalendarModel) manager);
    assertEquals(
            "[OK] Copied 0 events from " + emptyStart + "–" + emptyEnd
                    + " to calendar 'TargetCal' starting " + destStart,
            result
    );
  }

  // Tests copying events to a calendar that does not exist.
  @Test
  public void testCopyEventsRangeToNonExistentCalendar() {
    CopyEventsRangeCommand cmd = new CopyEventsRangeCommand(sourceStart, sourceEnd,
            "FakeCal", destStart);
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[ERROR] No calendar named 'FakeCal'.", result);
  }

  // Tests copying a range of a single day.
  @Test
  public void testCopyEventsSingleDayRange() {
    CopyEventsRangeCommand cmd = new CopyEventsRangeCommand(sourceStart, sourceStart,
            "TargetCal", destStart);
    String result = cmd.execute((CalendarModel) manager);
    assertEquals(
            "[OK] Copied 1 events from " + sourceStart + "–" + sourceStart
                    + " to calendar 'TargetCal' starting " + destStart,
            result
    );
  }

  // Tests a range where the start date is after the end date.
  @Test
  public void testCopyEventsInvertedRange() {
    CopyEventsRangeCommand cmd = new CopyEventsRangeCommand(sourceEnd, sourceStart,
            "TargetCal", destStart);
    String result = cmd.execute((CalendarModel) manager);
    assertEquals(
            "[OK] Copied 0 events from " + sourceEnd + "–" + sourceStart
                    + " to calendar 'TargetCal' starting " + destStart,
            result
    );
  }

  // Ensures duplicate events are not created on repeated copy
  @Test
  public void testCopyEventsRangeNoDuplicateOnSecondCopy() {
    CopyEventsRangeCommand cmd1 = new CopyEventsRangeCommand(sourceStart, sourceEnd,
            "TargetCal", destStart);
    String result1 = cmd1.execute((CalendarModel) manager);
    assertEquals(
            "[OK] Copied 2 events from " + sourceStart + "–" + sourceEnd
                    + " to calendar 'TargetCal' starting " + destStart,
            result1
    );
    CopyEventsRangeCommand cmd2 = new CopyEventsRangeCommand(sourceStart, sourceEnd,
            "TargetCal", destStart);
    String result2 = cmd2.execute((CalendarModel) manager);
    assertEquals(
            "[OK] Copied 0 events from " + sourceStart + "–" + sourceEnd
                    + " to calendar 'TargetCal' starting " + destStart,
            result2
    );
  }

  // Confirms range can be copied to the same calendar but at a different, non-overlapping date
  @Test
  public void testCopyEventsRangeToSameCalendarDifferentDates() {
    CopyEventsRangeCommand cmd = new CopyEventsRangeCommand(sourceStart, sourceEnd,
            "SourceCal", destStart.plusDays(10));
    String result = cmd.execute((CalendarModel) manager);
    assertEquals(
            "[OK] Copied 2 events from " + sourceStart + "–" + sourceEnd
                    + " to calendar 'SourceCal' starting " + destStart.plusDays(10),
            result
    );
  }

  //Validates calendar name is case sensitive
  @Test
  public void testCopyEventsRangeTargetCalendarCaseSensitive() {
    CopyEventsRangeCommand cmd = new CopyEventsRangeCommand(sourceStart, sourceEnd,
            "targetcal", destStart);
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[ERROR] No calendar named 'targetcal'.", result);
  }

  @Test
  public void testCopyEventsRangeLargeDateSpan() {
    LocalDate farStart = LocalDate.of(2020, 1, 1);
    LocalDate farEnd = LocalDate.of(2030, 12, 31);
    CopyEventsRangeCommand cmd = new CopyEventsRangeCommand(farStart, farEnd,
            "TargetCal", destStart);
    String result = cmd.execute((CalendarModel) manager);
    assertTrue(result.startsWith("[OK] Copied "));
  }

  @Test
  public void testCopyEventsSingleDayNoEvents() {
    LocalDate loneDay = LocalDate.of(2025, 12, 25);
    CopyEventsRangeCommand cmd = new CopyEventsRangeCommand(loneDay, loneDay,
            "TargetCal", destStart);
    String result = cmd.execute((CalendarModel) manager);
    assertTrue(result.startsWith("[OK] Copied 0 events from "));
  }



}