package controller.commands;

import calendarapp.controller.commands.CopyEventCommand;
import calendarapp.model.CalendarManagerImpl;
import calendarapp.model.CalendarModel;
import calendarapp.model.Event;
import calendarapp.model.MultiCalendarModel;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the CopyEventCommand class.
 * Verifies copying a single event between calendars, including series and error conditions.
 */
public class CopyEventCommandTest {

  private MultiCalendarModel manager;
  private final LocalDateTime eventStart =
          LocalDateTime.of(2025, 7, 15, 10, 0);
  private final LocalDateTime eventEnd =
          LocalDateTime.of(2025, 7, 15, 11, 0);
  private final LocalDateTime newEventStart =
          LocalDateTime.of(2025, 9, 1, 14, 0);

  @Before
  public void setUp() {
    manager = new CalendarManagerImpl();
    manager.createCalendar("SourceCal", ZoneId.of("UTC"));
    manager.createCalendar("TargetCal", ZoneId.of("America/New_York"));
    manager.useCalendar("SourceCal");

    Event event = new Event("Team Meeting", eventStart, eventEnd);
    manager.addEvent(event);
  }


  // Tests that attempting to copy an event to a non-existent calendar fails.
  @Test
  public void testCopyEventToNonExistentCalendar() {
    CopyEventCommand cmd = new CopyEventCommand("Team Meeting", eventStart,
            "FakeCal", newEventStart);
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[ERROR] No calendar named 'FakeCal'.", result);
  }

  // Tests that attempting to copy a non-existent event fails.
  @Test
  public void testCopyNonExistentEvent() {
    LocalDateTime fakeStart =
            LocalDateTime.of(2025, 1, 1, 1, 0);
    CopyEventCommand cmd = new CopyEventCommand("Fake Meeting", fakeStart,
            "TargetCal", newEventStart);
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[ERROR] No event named 'Fake Meeting' starting at "
            + fakeStart + ".", result);
  }


  // Tests that an invalid source start time string results in an error from the parser.
  @Test
  public void testCopyEventWithInvalidDateTime() {
    // right subject, wrong time
    CopyEventCommand cmd = new CopyEventCommand("Team Meeting", eventStart.plusHours(1),
            "TargetCal", newEventStart);
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[ERROR] No event named 'Team Meeting' starting at "
            + eventStart.plusHours(1) + ".", result);
  }

  @Test
  public void testCopyEventDuplicateInTargetCalendar() {
    manager.useCalendar("TargetCal");
    Event dup = new Event("Team Meeting", newEventStart, newEventStart.plusHours(1));
    manager.addEvent(dup);
    manager.useCalendar("SourceCal");

    CopyEventCommand cmd = new CopyEventCommand("Team Meeting", eventStart,
            "TargetCal", newEventStart);
    String result = cmd.execute((CalendarModel) manager);


    assertEquals("[ERROR] Event 'Team Meeting' already exists.", result);
  }

  @Test
  public void testCopyEventToSameTimeAndCalendar() {
    CopyEventCommand cmd = new CopyEventCommand("Team Meeting", eventStart,
            "SourceCal", eventStart);
    String result = cmd.execute((CalendarModel) manager);

    assertEquals("[ERROR] Event 'Team Meeting' already exists.", result);
  }

  @Test
  public void testCopySingleEventSuccessfully() {
    CopyEventCommand cmd = new CopyEventCommand("Team Meeting", eventStart,
            "TargetCal", newEventStart);
    String result = cmd.execute((CalendarModel) manager);
    String expected = String.format(
            "[OK] Event 'Team Meeting' copied to calendar 'TargetCal' at %s.",
            newEventStart
    );
    assertEquals(expected, result);
  }

  @Test
  public void testCopyEventToSameCalendar() {
    CopyEventCommand cmd = new CopyEventCommand("Team Meeting", eventStart,
            "SourceCal", newEventStart);
    String result = cmd.execute((CalendarModel) manager);
    String expected = String.format(
            "[OK] Event 'Team Meeting' copied to calendar 'SourceCal' at %s.",
            newEventStart
    );
    assertEquals(expected, result);
  }

  @Test
  public void testCopyEventWithWhitespaceCalendarNames() {
    CopyEventCommand cmd = new CopyEventCommand("Team Meeting", eventStart,
            " TargetCal ", newEventStart);
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[ERROR] No calendar named ' TargetCal '.", result);
  }

  @Test
  public void testCopyEventWithEmptySubject() {
    CopyEventCommand cmd = new CopyEventCommand("", eventStart, "TargetCal",
            newEventStart);
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[ERROR] No event named '' starting at " + eventStart + ".", result);
  }

  @Test
  public void testCopyEventToTargetWhereEventAlreadyExistsAtTime() {
    manager.useCalendar("TargetCal");
    Event conflicting = new Event("Other Event", newEventStart, newEventStart.plusHours(1));
    manager.addEvent(conflicting);
    manager.useCalendar("SourceCal");

    CopyEventCommand cmd = new CopyEventCommand("Team Meeting", eventStart,
            "TargetCal", newEventStart);
    String result = cmd.execute((CalendarModel) manager);

    assertEquals("[OK] Event 'Team Meeting' copied to calendar 'TargetCal' " +
            "at 2025-09-01T14:00.", result);
  }


}