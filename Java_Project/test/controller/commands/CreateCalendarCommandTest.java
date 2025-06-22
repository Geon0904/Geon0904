package controller.commands;

import calendarapp.controller.commands.CreateCalendarCommand;
import calendarapp.model.CalendarManagerImpl;
import calendarapp.model.CalendarModel;
import calendarapp.model.MultiCalendarModel;

import org.junit.Before;
import org.junit.Test;

import java.time.ZoneId;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the CreateCalendarCommand class.
 * Verifies that calendars are created successfully and that duplicate names are handled correctly.
 */
public class CreateCalendarCommandTest {

  private MultiCalendarModel manager;

  @Before
  public void setUp() {
    manager = new CalendarManagerImpl();
  }

  // Tests the successful creation of a new calendar.
  @Test
  public void testCreateCalendarSuccess() {
    CreateCalendarCommand cmd = new CreateCalendarCommand("Work", ZoneId.of("UTC"));
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[OK] Created calendar 'Work' with timezone UTC", result);
  }

  // Tests that creating a calendar with a duplicate name returns an error.
  @Test
  public void testCreateDuplicateCalendar() {
    // First creation should succeed
    CreateCalendarCommand cmd1 = new CreateCalendarCommand("Personal",
            ZoneId.of("America/New_York"));
    cmd1.execute((CalendarModel) manager);

    // Second should fail and return a formatted error message from the command
    CreateCalendarCommand cmd2 = new CreateCalendarCommand("Personal",
            ZoneId.of("UTC"));
    String result = cmd2.execute((CalendarModel) manager);
    assertEquals("[ERROR] Calendar 'Personal' already exists.", result);
  }

  // Tests creating a calendar with a complex timezone name.
  @Test
  public void testCreateCalendarWithComplexTimezone() {
    CreateCalendarCommand cmd = new CreateCalendarCommand("Appointments",
            ZoneId.of("America/Los_Angeles"));
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[OK] Created calendar 'Appointments' with timezone America/Los_Angeles",
            result);
  }

  // Tests that an IllegalArgumentException is thrown when the model receives a null name.
  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarWithNullName() {
    CreateCalendarCommand cmd = new CreateCalendarCommand(null, ZoneId.of("UTC"));
    cmd.execute((CalendarModel) manager);
  }


  // Tests that creating a calendar with an empty name is successful.
  @Test
  public void testCreateCalendarWithEmptyName() {
    CreateCalendarCommand cmd = new CreateCalendarCommand("", ZoneId.of("UTC"));
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[OK] Created calendar '' with timezone UTC", result);
  }

  // Tests that a NullPointerException is thrown by ZoneId.of() with a null timezone.
  @Test(expected = NullPointerException.class)
  public void testCreateCalendarWithNullTimezone() {
    // The command constructor itself will fail here, which is the expected behavior.
    new CreateCalendarCommand("Fails", ZoneId.of((String) null));
  }

  // Verifies that different calendar names can be created successfully.
  @Test
  public void testCreateMultipleDifferentCalendars() {
    CreateCalendarCommand cmd1 = new CreateCalendarCommand("Work", ZoneId.of("UTC"));
    String result1 = cmd1.execute((CalendarModel) manager);
    assertEquals("[OK] Created calendar 'Work' with timezone UTC", result1);

    CreateCalendarCommand cmd2 = new CreateCalendarCommand("Home",
            ZoneId.of("Europe/London"));
    String result2 = cmd2.execute((CalendarModel) manager);
    assertEquals("[OK] Created calendar 'Home' with timezone Europe/London", result2);
  }


  // Verifies that calendar names are case sensitive
  @Test
  public void testCalendarNameCaseSensitivity() {
    CreateCalendarCommand cmd1 = new CreateCalendarCommand("Work", ZoneId.of("UTC"));
    cmd1.execute((CalendarModel) manager);
    CreateCalendarCommand cmd2 = new CreateCalendarCommand("work", ZoneId.of("UTC"));
    String result = cmd2.execute((CalendarModel) manager);

    assertEquals("[OK] Created calendar 'work' with timezone UTC", result);
  }

  // Tests that creating a calendar with leading/trailing whitespace
  @Test
  public void testCreateCalendarWithWhitespaceName() {
    CreateCalendarCommand cmd = new CreateCalendarCommand("  Whitespace  ",
            ZoneId.of("UTC"));
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[OK] Created calendar '  Whitespace  ' with timezone UTC", result);
  }

  // Verifies that creating a calendar with a timezone string that has extra whitespace fails
  @Test(expected = Exception.class)
  public void testCreateCalendarWithWhitespaceTimezone() {
    // This should fail if ZoneId.of doesn't trim spaces
    new CreateCalendarCommand("Test", ZoneId.of(" UTC "));
  }

  // Verifies that creating a calendar with a non-existent timezone throws an exception
  @Test(expected = Exception.class)
  public void testCreateCalendarWithInvalidTimezoneString() {
    new CreateCalendarCommand("Ghost", ZoneId.of("Fake/Zone"));
  }

  // Ensures that creating a calendar with special characters in the name is allowed
  @Test
  public void testCreateCalendarWithSpecialCharacters() {
    CreateCalendarCommand cmd = new CreateCalendarCommand("$$$!@#$_2025",
            ZoneId.of("UTC"));
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[OK] Created calendar '$$$!@#$_2025' with timezone UTC", result);
  }

  @Test
  public void testCreateCalendarWithSpecialWhitespaceInName() {
    CreateCalendarCommand cmd = new CreateCalendarCommand("Work\n2025\tTab",
            ZoneId.of("UTC"));
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[OK] Created calendar 'Work\n2025\tTab' with timezone UTC", result);
  }

  @Test
  public void testCreateCalendarWithVeryLongName() {
    String longName = "A".repeat(300);
    CreateCalendarCommand cmd = new CreateCalendarCommand(longName, ZoneId.of("UTC"));
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[OK] Created calendar '" + longName + "' with timezone UTC", result);
  }



}