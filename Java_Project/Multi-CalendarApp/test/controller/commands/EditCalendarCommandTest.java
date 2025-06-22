package controller.commands;

import calendarapp.controller.commands.EditCalendarCommand;
import calendarapp.model.CalendarManagerImpl;
import calendarapp.model.CalendarModel;
import calendarapp.model.MultiCalendarModel;

import org.junit.Before;
import org.junit.Test;

import java.time.ZoneId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unit tests for the EditCalendarCommand class.
 * Verifies editing of calendar properties like name and timezone, and handles error cases.
 */
public class EditCalendarCommandTest {

  private MultiCalendarModel manager;

  @Before
  public void setUp() {
    manager = new CalendarManagerImpl();
    manager.createCalendar("Work", ZoneId.of("America/New_York"));
    manager.createCalendar("Personal", ZoneId.of("UTC"));
  }

  // Tests successfully renaming an existing calendar.
  @Test
  public void testEditCalendarNameSuccessfully() {
    EditCalendarCommand cmd = new EditCalendarCommand("Work", "name",
            "Work-Renamed");
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[OK] Calendar renamed 'Work' → 'Work-Renamed'", result);
  }

  // Tests successfully changing the timezone of an existing calendar.
  @Test
  public void testEditCalendarTimezoneSuccessfully() {
    EditCalendarCommand cmd = new EditCalendarCommand("Personal", "timezone",
            "Europe/Paris");
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[OK] Calendar 'Personal' timezone changed to Europe/Paris", result);
  }

  // Tests that renaming a calendar to a name that already exists fails.
  @Test
  public void testEditNameToExistingNameFails() {
    EditCalendarCommand cmd = new EditCalendarCommand("Work", "name",
            "Personal");
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[ERROR] Calendar 'Personal' already exists.", result);
  }

  // Tests that attempting to edit a non-existent calendar fails.
  @Test
  public void testEditNonExistentCalendar() {
    EditCalendarCommand cmd = new EditCalendarCommand("School", "name",
            "My School");
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[ERROR] No calendar named 'School'", result);
  }

  // Tests editing with a property that is not supported.
  @Test
  public void testEditWithUnsupportedProperty() {
    EditCalendarCommand cmd = new EditCalendarCommand("Work", "color",
            "blue");
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[ERROR] Unsupported calendar property: color", result);
  }

  // Tests editing the timezone with an invalid format.
  @Test(expected = java.time.zone.ZoneRulesException.class)
  public void testEditWithInvalidTimezoneFormat() {
    EditCalendarCommand cmd = new EditCalendarCommand("Work", "timezone",
            "Invalid/Timezone");
    // This line will throw the ZoneRulesException, and the test will pass.
    cmd.execute((CalendarModel) manager);
  }

  // Tests that property matching is case-insensitive.
  @Test
  public void testEditWithCaseInsensitiveProperty() {
    EditCalendarCommand cmd = new EditCalendarCommand("Work", "Name",
            "Work-New");
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[OK] Calendar renamed 'Work' → 'Work-New'", result);
  }

  // Tests that attempting to rename a calendar to null throws an exception.
  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarNameToNull() {
    EditCalendarCommand cmd = new EditCalendarCommand("Work", "name", null);
    cmd.execute((CalendarModel) manager);
  }

  // Tests renaming a calendar to an empty string
  @Test
  public void testEditCalendarNameToEmptyString() {
    EditCalendarCommand cmd = new EditCalendarCommand("Work", "name", "");
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[OK] Calendar renamed 'Work' → ''", result);
  }

  // Tests changing a timezone to an empty string
  @Test(expected = java.time.DateTimeException.class)
  public void testEditCalendarTimezoneToEmptyString() {
    EditCalendarCommand cmd = new EditCalendarCommand("Work", "timezone",
            "");
    cmd.execute((CalendarModel) manager);
  }

  // Tests property argument as null
  @Test(expected = NullPointerException.class)
  public void testEditWithNullProperty() {
    EditCalendarCommand cmd = new EditCalendarCommand("Work", null, "X");
    cmd.execute((CalendarModel) manager);
  }

  // Tests editing calendar name with whitespace-only string
  @Test
  public void testEditCalendarNameToWhitespace() {
    EditCalendarCommand cmd = new EditCalendarCommand("Work", "name", "   ");
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[OK] Calendar renamed 'Work' → '   '", result);
  }

  // Tests property argument with leading/trailing spaces
  @Test
  public void testEditCalendarPropertyWithWhitespace() {
    EditCalendarCommand cmd = new EditCalendarCommand("Work", " name ",
            "SomeName");
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[ERROR] Unsupported calendar property:  name ", result);
  }

  @Test
  public void testEditCalendarNameToVeryLongName() {
    String longName = new String(new char[200]).replace('\0', 'A');
    EditCalendarCommand cmd = new EditCalendarCommand("Work", "name", longName);
    String result = cmd.execute((CalendarModel) manager);
    assertEquals("[OK] Calendar renamed 'Work' → '" + longName + "'", result);
  }

  @Test
  public void testEditCalendarTimezoneCaseSensitiveValid() {
    EditCalendarCommand cmd = new EditCalendarCommand("Personal", "timezone",
            "utc");
    try {
      cmd.execute((CalendarModel) manager);
      fail("Should throw DateTimeException for lowercase 'utc'");
    } catch (java.time.DateTimeException e) {
      //
    }
  }



}