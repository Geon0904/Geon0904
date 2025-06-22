package controller.commands;

import calendarapp.controller.commands.ShowStatusCommand;
import calendarapp.model.CalendarImpl;
import calendarapp.model.CalendarModel;
import calendarapp.model.Event;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the ShowStatusCommand class.
 * Verifies whether a given date-time is marked as busy or available,
 * and handles invalid date-time formats or null input.
 */
public class ShowStatusCommandTest {

  private CalendarModel calendar;

  @Before
  public void setup() {
    calendar = new CalendarImpl();
    calendar.addEvent(new Event(
            "Morning Meeting",
            LocalDateTime.of(2025, 7, 4, 10, 0),
            LocalDateTime.of(2025, 7, 4, 11, 0),
            "Team sync", "Zoom", true));
  }

  @Test
  //Checks that time within an event is marked as busy.
  public void testBusyWithinEventTime() {
    ShowStatusCommand cmd = new ShowStatusCommand("2025-07-04T10:30");
    String result = cmd.execute(calendar);
    assertEquals("[BUSY] 2025-07-04T10:30", result);
  }

  @Test
  //Checks availability before event starts.
  public void testAvailableOutsideEventTime() {
    ShowStatusCommand cmd = new ShowStatusCommand("2025-07-04T08:00");
    String result = cmd.execute(calendar);
    assertEquals("[AVAILABLE] 2025-07-04T08:00", result);
  }

  @Test
  //Verifies busy status at exact event start time.
  public void testBusyAtEventStartTime() {
    ShowStatusCommand cmd = new ShowStatusCommand("2025-07-04T10:00");
    String result = cmd.execute(calendar);
    assertEquals("[BUSY] 2025-07-04T10:00", result);
  }

  @Test
  //Verifies available status at exact event end time.
  public void testAvailableAtEventEndTime() {
    ShowStatusCommand cmd = new ShowStatusCommand("2025-07-04T11:00");
    String result = cmd.execute(calendar);
    assertEquals("[AVAILABLE] 2025-07-04T11:00", result);
  }

  @Test
  //Confirms busy status during overlapping events.
  public void testMultipleEventsOverlapping() {
    CalendarModel localCalendar = new CalendarImpl();
    localCalendar.addEvent(new Event("Morning Standup",
            LocalDateTime.of(2025, 7, 4, 9, 0),
            LocalDateTime.of(2025, 7, 4, 10, 30),
            "Sync", "Zoom", true));
    localCalendar.addEvent(new Event("Business Meeting",
            LocalDateTime.of(2025, 7, 4, 10, 31),
            LocalDateTime.of(2025, 7, 4, 11, 30),
            "Work", "Office", false));
    ShowStatusCommand cmd = new ShowStatusCommand("2025-07-04T11:15");
    String result = cmd.execute(localCalendar);
    assertEquals("[BUSY] 2025-07-04T11:15", result);
  }

  @Test
  //Rejects input with slashes instead of hyphens and missing 'T'.
  public void testInvalidDateTimeFormat_slashes() {
    ShowStatusCommand cmd = new ShowStatusCommand("2025/07/04 10:00");
    String result = cmd.execute(calendar);
    assertEquals("[ERROR] Invalid date-time format. Use 'YYYY-MM-DDTHH:MM'.", result);
  }

  @Test
  //Rejects input with space instead of 'T' separator.
  public void testInvalidDateTimeFormat_missingT() {
    ShowStatusCommand cmd = new ShowStatusCommand("2025-07-04 10:00");
    String result = cmd.execute(calendar);
    assertEquals("[ERROR] Invalid date-time format. Use 'YYYY-MM-DDTHH:MM'.", result);
  }

  @Test
  //Rejects empty string input.
  public void testInvalidDateTimeFormat_emptyString() {
    ShowStatusCommand cmd = new ShowStatusCommand("");
    String result = cmd.execute(calendar);
    assertEquals("[ERROR] Invalid date-time format. Use 'YYYY-MM-DDTHH:MM'.", result);
  }

  @Test
  //Rejects month value beyond valid range.
  public void testInvalidDateTimeFormat_invalidMonth() {
    ShowStatusCommand cmd = new ShowStatusCommand("2025-13-01T09:00");
    String result = cmd.execute(calendar);
    assertEquals("[ERROR] Invalid date-time format. Use 'YYYY-MM-DDTHH:MM'.", result);
  }

  @Test
  //Ensures calendar without any events shows available.
  public void testNoEventsCalendar() {
    CalendarModel emptyCalendar = new CalendarImpl();
    ShowStatusCommand cmd = new ShowStatusCommand("2025-07-04T10:00");
    String result = cmd.execute(emptyCalendar);
    assertEquals("[AVAILABLE] 2025-07-04T10:00", result);
  }

  @Test
  //Input with leading/trailing whitespace should fail.
  public void testWhitespaceAroundDateTime() {
    ShowStatusCommand cmd = new ShowStatusCommand(" 2025-07-04T10:30 ");
    String result = cmd.execute(calendar);
    assertEquals("[ERROR] Invalid date-time format. Use 'YYYY-MM-DDTHH:MM'.", result);
  }

  @Test
  //Rejects invalid day in February.
  public void testInvalidDayOfMonth() {
    ShowStatusCommand cmd = new ShowStatusCommand("2025-02-30T10:00");
    String result = cmd.execute(calendar);
    assertEquals("[ERROR] Invalid date-time format. Use 'YYYY-MM-DDTHH:MM'.", result);
  }

  @Test
  //Confirms seconds in input are parsed correctly.
  public void testSecondsInDateTimeAreAccepted() {
    ShowStatusCommand cmd = new ShowStatusCommand("2025-07-04T10:00:00");
    String result = cmd.execute(calendar);
    assertEquals("[BUSY] 2025-07-04T10:00", result);
  }

  @Test(expected = NullPointerException.class)
  //Null input should throw exception as expected.
  public void testNullInputThrows() {
    ShowStatusCommand cmd = new ShowStatusCommand(null);
    cmd.execute(calendar);
  }

  @Test
  //Test valid leap year date.
  public void testLeapYearTimeIsParsedCorrectly() {
    calendar.addEvent(new Event("Leap Day Sync",
            LocalDateTime.of(2024, 2, 29, 14, 0),
            LocalDateTime.of(2024, 2, 29, 15, 0),
            "Leap event", "Zoom", true));
    ShowStatusCommand cmd = new ShowStatusCommand("2024-02-29T14:30");
    String result = cmd.execute(calendar);
    assertEquals("[BUSY] 2024-02-29T14:30", result);
  }

  @Test
  // Tests availability right before event starts.
  public void testRightBeforeEventIsAvailable() {
    ShowStatusCommand cmd = new ShowStatusCommand("2025-07-04T09:59");
    String result = cmd.execute(calendar);
    assertEquals("[AVAILABLE] 2025-07-04T09:59", result);
  }

  @Test
  public void testAdjacentEventsNoOverlap() {
    CalendarImpl cal = new CalendarImpl();
    cal.addEvent(new Event("A", LocalDateTime.of(2025, 7, 4,
            10, 0), LocalDateTime.of(2025, 7, 4, 11,
            0)));
    cal.addEvent(new Event("B", LocalDateTime.of(2025, 7, 4,
            11, 0), LocalDateTime.of(2025, 7, 4, 12,
            0)));
    ShowStatusCommand cmd = new ShowStatusCommand("2025-07-04T11:00");
    String result = cmd.execute(cal);
    assertEquals("[BUSY] 2025-07-04T11:00", result);
  }

}
