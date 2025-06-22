package controller.commands;

import calendarapp.controller.commands.EditEventCommand;
import calendarapp.model.CalendarImpl;
import calendarapp.model.Event;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the EditEventCommand class.
 * Verifies editing of event properties including subject, description, location, and status.
 */
public class EditEventCommandTest {

  private CalendarImpl calendar;

  @Before
  public void setUp() {
    calendar = new CalendarImpl();
    Event original = new Event(
            "Project Kickoff",
            LocalDateTime.of(2025, 7, 10, 10, 0),
            LocalDateTime.of(2025, 7, 10, 11, 0),
            "Initial planning",
            "Room 101",
            true
    );
    calendar.addEvent(original);
  }

  // Tests editing the subject field successfully
  @Test
  public void testEditSubjectSuccessfully() {
    EditEventCommand cmd = new EditEventCommand(
            "Project Kickoff",
            "2025-07-10T10:00",
            "2025-07-10T11:00",
            "subject",
            "Kickoff Meeting"
    );
    String result = cmd.execute(calendar);
    assertEquals("[OK] Updated 'subject' (Project Kickoff -> Kickoff Meeting)", result);
  }

  // Tests editing the description field successfully
  @Test
  public void testEditDescriptionSuccessfully() {
    EditEventCommand cmd = new EditEventCommand(
            "Project Kickoff",
            "2025-07-10T10:00",
            "2025-07-10T11:00",
            "description",
            "Kickoff discussion with all teams"
    );
    String result = cmd.execute(calendar);
    assertEquals("[OK] Updated 'description' (Initial planning -> "
            + "Kickoff discussion with all teams)", result);
  }

  // Tests editing the location field successfully
  @Test
  public void testEditLocationSuccessfully() {
    EditEventCommand cmd = new EditEventCommand(
            "Project Kickoff",
            "2025-07-10T10:00",
            "2025-07-10T11:00",
            "location",
            "Main Hall"
    );
    String result = cmd.execute(calendar);
    assertEquals("[OK] Updated 'location' (Room 101 -> Main Hall)", result);
  }

  // Tests editing the status field to private
  @Test
  public void testEditStatusToPrivate() {
    EditEventCommand cmd = new EditEventCommand(
            "Project Kickoff",
            "2025-07-10T10:00",
            "2025-07-10T11:00",
            "status",
            "private"
    );
    String result = cmd.execute(calendar);
    assertEquals("[OK] Updated 'status' (public -> private)", result);
  }

  // Tests editing the status field with capital letters
  @Test
  public void testEditStatusToPublic() {
    EditEventCommand cmd = new EditEventCommand(
            "Project Kickoff",
            "2025-07-10T10:00",
            "2025-07-10T11:00",
            "status",
            "PUBLIC"
    );
    String result = cmd.execute(calendar);
    assertEquals("[OK] Updated 'status' (public -> PUBLIC)", result);
  }

  // Tests editing an unsupported property
  @Test
  public void testEditUnknownProperty() {
    EditEventCommand cmd = new EditEventCommand(
            "Project Kickoff",
            "2025-07-10T10:00",
            "2025-07-10T11:00",
            "priority",
            "high"
    );
    String result = cmd.execute(calendar);
    assertEquals("[ERROR] Unsupported property: priority", result);
  }

  // Tests editing with an invalid datetime format
  @Test
  public void testEditWithInvalidDateFormat() {
    EditEventCommand cmd = new EditEventCommand(
            "Project Kickoff",
            "07-10-2025 10:00",
            "2025-07-10T11:00",
            "subject",
            "Something"
    );
    String result = cmd.execute(calendar);
    assertEquals("[ERROR] Invalid datetime format. Use 'YYYY-MM-DDTHH:MM'.", result);
  }

  // Tests editing a non-existent event
  @Test
  public void testEditWithNonexistentEvent() {
    EditEventCommand cmd = new EditEventCommand(
            "Nonexistent",
            "2025-07-10T10:00",
            "2025-07-10T11:00",
            "subject",
            "Whatever"
    );
    String result = cmd.execute(calendar);
    assertEquals("[ERROR] No such event found to edit.", result);
  }

  // Tests editing end time to a time before start, which throws exception
  @Test(expected = IllegalArgumentException.class)
  public void testEditEndBeforeStartThrows() {
    EditEventCommand cmd = new EditEventCommand(
            "Project Kickoff",
            "2025-07-10T10:00",
            "2025-07-10T11:00",
            "end",
            "2025-07-10T09:00"
    );
    cmd.execute(calendar);
  }

  // Tests editing the status with arbitrary string
  @Test
  public void testEditStatusWithArbitraryValue() {
    EditEventCommand cmd = new EditEventCommand(
            "Project Kickoff",
            "2025-07-10T10:00",
            "2025-07-10T11:00",
            "status",
            "foo"
    );
    String result = cmd.execute(calendar);
    assertEquals("[OK] Updated 'status' (public -> foo)", result);
  }

  // Tests editing the start time successfully
  @Test
  public void testEditStartSuccessfully() {
    EditEventCommand cmd = new EditEventCommand(
            "Project Kickoff",
            "2025-07-10T10:00",
            "2025-07-10T11:00",
            "start",
            "2025-07-10T10:30"
    );
    String result = cmd.execute(calendar);
    assertEquals("[OK] Updated 'start' (2025-07-10T10:00:00 -> 2025-07-10T10:30)", result);
  }

  // Tests editing the end time successfully
  @Test
  public void testEditEndSuccessfully() {
    EditEventCommand cmd = new EditEventCommand(
            "Project Kickoff",
            "2025-07-10T10:00",
            "2025-07-10T11:00",
            "end",
            "2025-07-10T11:30"
    );
    String result = cmd.execute(calendar);
    assertEquals("[OK] Updated 'end' (2025-07-10T11:00:00 -> 2025-07-10T11:30)", result);
  }

  @Test
  public void testEditDescriptionToEmptyString() {
    EditEventCommand cmd = new EditEventCommand(
            "Project Kickoff",
            "2025-07-10T10:00",
            "2025-07-10T11:00",
            "description",
            ""
    );
    String result = cmd.execute(calendar);
    assertEquals("[OK] Updated 'description' (Initial planning -> )", result);
  }

  @Test
  public void testEditUnknownPropertyWithTypo() {
    EditEventCommand cmd = new EditEventCommand(
            "Project Kickoff",
            "2025-07-10T10:00",
            "2025-07-10T11:00",
            "DescRiption",
            "Hello"
    );
    String result = cmd.execute(calendar);
    assertEquals("[OK] Updated 'description' (Initial planning -> Hello)", result);
  }

}