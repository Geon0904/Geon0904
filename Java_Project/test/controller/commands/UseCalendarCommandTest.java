package controller.commands;

import calendarapp.controller.commands.UseCalendarCommand;
import calendarapp.model.CalendarManagerImpl;
import calendarapp.model.CalendarModel;
import calendarapp.model.MultiCalendarModel;

import org.junit.Before;
import org.junit.Test;

import java.time.ZoneId;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the UseCalendarCommand class.
 * Verifies switching between calendars and handling of non-existent calendar names.
 */
public class UseCalendarCommandTest {

  private MultiCalendarModel manager;

  @Before
  public void setUp() {
    manager = new CalendarManagerImpl();
    // Pre-populate the manager with some calendars to use in tests
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.createCalendar("Personal", ZoneId.of("UTC"));
  }

  // Tests successfully switching to an existing calendar.
  @Test
  public void testUseExistingCalendarSuccess() {
    UseCalendarCommand cmd = new UseCalendarCommand("Work");
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[OK] Switched to calendar 'Work'", result);
  }

  // Tests attempting to switch to a calendar that does not exist.
  @Test
  public void testUseNonExistentCalendar() {
    UseCalendarCommand cmd = new UseCalendarCommand("School");
    String result = cmd.execute((CalendarModel) manager);
    // This message comes from the command catching the model's NoSuchElementException
    assertEquals("[ERROR] No calendar named 'School'", result);
  }

  // Verifies that the calendar lookup is case-sensitive.
  @Test
  public void testUseCalendarIsCaseSensitive() {
    UseCalendarCommand cmd = new UseCalendarCommand("work");
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[ERROR] No calendar named 'work'", result);
  }

  // Tests using an empty string as the calendar name.
  @Test
  public void testUseCalendarWithEmptyName() {
    UseCalendarCommand cmd = new UseCalendarCommand("");
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[ERROR] No calendar named ''", result);
  }

  // Tests that using a null name results in an exception from the model.
  @Test(expected = IllegalArgumentException.class)
  public void testUseCalendarWithNullName() {
    UseCalendarCommand cmd = new UseCalendarCommand(null);
    cmd.execute((CalendarModel) manager);
  }


  // Tests successfully switching to another existing calendar.
  @Test
  public void testUseAnotherExistingCalendar() {
    UseCalendarCommand cmd = new UseCalendarCommand("Personal");
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[OK] Switched to calendar 'Personal'", result);
  }

  // Tests switching to the same calendar multiple times (idempotence)
  @Test
  public void testSwitchToSameCalendarMultipleTimes() {
    UseCalendarCommand cmd = new UseCalendarCommand("Work");
    String result1 = cmd.execute((CalendarModel) manager);
    String result2 = cmd.execute((CalendarModel) manager);
    assertEquals("[OK] Switched to calendar 'Work'", result1);
    assertEquals("[OK] Switched to calendar 'Work'", result2);
  }

  // Tests using a calendar name with leading/trailing whitespace (should fail)
  @Test
  public void testUseCalendarWithWhitespaceName() {
    UseCalendarCommand cmd = new UseCalendarCommand(" Work ");
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[ERROR] No calendar named ' Work '", result);
  }

  // Tests switching between multiple calendars in succession
  @Test
  public void testSwitchingBackAndForth() {
    UseCalendarCommand cmd1 = new UseCalendarCommand("Work");
    UseCalendarCommand cmd2 = new UseCalendarCommand("Personal");
    String result1 = cmd1.execute((CalendarModel) manager);
    String result2 = cmd2.execute((CalendarModel) manager);
    String result3 = cmd1.execute((CalendarModel) manager);
    assertEquals("[OK] Switched to calendar 'Work'", result1);
    assertEquals("[OK] Switched to calendar 'Personal'", result2);
    assertEquals("[OK] Switched to calendar 'Work'", result3);
  }

  @Test
  public void testUseCalendarWithSpecialCharacters() {
    manager.createCalendar("$$$_CAL_2025", ZoneId.of("UTC"));
    UseCalendarCommand cmd = new UseCalendarCommand("$$$_CAL_2025");
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[OK] Switched to calendar '$$$_CAL_2025'", result);
  }

}