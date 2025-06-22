package model;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import calendarapp.model.Event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for EventTest class. Verifies the constructor, default event durations, and equality.
 */
public class EventTest {

  /**
   * Tests the three-argument constructor for valid inputs and default field values.
   */
  @Test
  public void testThreeArgConstructorValid() {
    LocalDateTime start = LocalDateTime.of(2025, 7, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 7, 1, 11, 0);
    Event event = new Event("Basic Event", start, end);

    assertEquals("Basic Event", event.getSubject());
    assertEquals(start, event.getStart());
    assertEquals(end, event.getEnd());
    assertEquals("Default description should be empty.", "",
            event.getDescription());
    assertEquals("Default location should be empty.", "", event.getLocation());
    assertTrue("Default status should be public.", event.isPublic());
  }

  /**
   * Tests the full constructor with all arguments provided.
   */
  @Test
  public void testFullConstructorValid() {
    LocalDateTime start = LocalDateTime.of(2025, 7, 1, 14, 0);
    LocalDateTime end = LocalDateTime.of(2025, 7, 1, 15, 30);
    Event event = new Event("Full Event", start, end,
            "Details here", "Room 101", false);

    assertEquals("Full Event", event.getSubject());
    assertEquals(start, event.getStart());
    assertEquals(end, event.getEnd());
    assertEquals("Details here", event.getDescription());
    assertEquals("Room 101", event.getLocation());
    assertFalse(event.isPublic());
  }

  /**
   * Tests that the constructor throws IllegalArgumentException for a null subject.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNullSubject() {
    new Event(null, LocalDateTime.now(), LocalDateTime.now().plusHours(1));
  }

  /**
   * Tests that the constructor throws IllegalArgumentException
   * for a blank subject.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConstructorBlankSubject() {
    new Event(" ", LocalDateTime.now(), LocalDateTime.now().plusHours(1));
  }

  /**
   * Tests that the constructor throws IllegalArgumentException for a null start time.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNullStart() {
    new Event("No Start", null, LocalDateTime.now().plusHours(1));
  }

  /**
   * Tests that the constructor throws IllegalArgumentException
   * if the end time is before the start time.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConstructorEndBeforeStart() {
    LocalDateTime start = LocalDateTime.of(2025, 7, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 7, 1, 9, 0);
    new Event("Time Warp", start, end);
  }

  /**
   * Tests that the constructor throws IllegalArgumentException
   * if the end time is equal to the start time.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConstructorEndEqualsStart() {
    LocalDateTime time = LocalDateTime.of(2025, 7, 1, 10, 0);
    new Event("Zero Duration", time, time);
  }

  /**
   * Tests that providing a null end time correctly creates
   * an all-day event with default start and end times.
   */
  @Test
  public void testConstructorNullEndIsAllDayDefaultTimes() {
    LocalDateTime startTimeProvided =
            LocalDateTime.of(2025, 7, 1, 10, 0);
    Event event = new Event("All Day By Null End", startTimeProvided,
            null, "Desc", "Loc", true);

    LocalDate expectedDate = startTimeProvided.toLocalDate();
    LocalTime defaultStart = LocalTime.of(8, 0);
    LocalTime defaultEnd = LocalTime.of(17, 0);

    assertEquals(LocalDateTime.of(expectedDate, defaultStart), event.getStart());
    assertEquals(LocalDateTime.of(expectedDate, defaultEnd), event.getEnd());
    assertTrue(event.isAllDay());
  }

  /**
   * Tests that null values for description and location in the constructor are set to empty.
   */
  @Test
  public void testConstructorNullDescriptionAndLocationBecomeEmpty() {
    Event event = new Event("Test Nulls", LocalDateTime.now(),
            LocalDateTime.now().plusHours(1), null, null, true);
    assertEquals("", event.getDescription());
    assertEquals("", event.getLocation());
  }

  /**
   * Tests isAllDay() returns true for an event explicitly set to default 8 AM - 5 PM.
   */
  @Test
  public void testIsAllDayTrue() {
    LocalDateTime start = LocalDateTime.of(2025, 7, 2, 8, 0);
    LocalDateTime end = LocalDateTime.of(2025, 7, 2, 17, 0);
    Event event = new Event("Proper All Day", start, end);
    assertTrue(event.isAllDay());
  }

  /**
   * Tests isAllDay() returns false if the start time is not the default all-day start time.
   */
  @Test
  public void testIsAllDayFalseWrongStartTime() {
    LocalDateTime start = LocalDateTime.of(2025, 7, 2, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 7, 2, 17, 0);
    Event event = new Event("Not All Day", start, end);
    assertFalse(event.isAllDay());
  }

  /**
   * Tests isAllDay() returns false if the end time is not the default all-day end time.
   */
  @Test
  public void testIsAllDayFalseWrongEndTime() {
    LocalDateTime start = LocalDateTime.of(2025, 7, 2, 8, 0);
    LocalDateTime end = LocalDateTime.of(2025, 7, 2, 18, 0);
    Event event = new Event("Not All Day", start, end);
    assertFalse(event.isAllDay());
  }

  /**
   * Tests the toString() method for an event with minimal details.
   */
  @Test
  public void testToStringMinimal() {
    Event event = new Event("Minimal",
            LocalDateTime.of(2025, 1, 1, 10, 0),
            LocalDateTime.of(2025, 1, 1, 11, 0));
    String expected = "Minimal: 2025-01-01T10:00 to 2025-01-01T11:00 [public]";
    assertEquals(expected, event.toString());
  }

  /**
   * Tests the toString() method for an event that includes a location.
   */
  @Test
  public void testToStringWithLocation() {
    Event event = new Event("Loc Event",
            LocalDateTime.of(2025, 1, 1, 10, 0),
            LocalDateTime.of(2025, 1, 1, 11, 0),
            "", "Office", true);
    String expected = "Loc Event: 2025-01-01T10:00 to 2025-01-01T11:00 @ Office [public]";
    assertEquals(expected, event.toString());
  }

  /**
   * Tests the toString() method for an event that includes a description and is private.
   */
  @Test
  public void testToStringWithDescription() {
    Event event = new Event("Desc Event",
            LocalDateTime.of(2025, 1, 1, 10, 0),
            LocalDateTime.of(2025, 1, 1, 11, 0),
            "Important", "", false);
    String expected = "Desc Event: 2025-01-01T10:00 to 2025-01-01T11:00 - Important [private]";
    assertEquals(expected, event.toString());
  }

  /**
   * Tests the toString() method for an event with all optional details provided.
   */
  @Test
  public void testToStringWithAllDetails() {
    Event event = new Event("Full Details",
            LocalDateTime.of(2025, 1, 1, 10, 0),
            LocalDateTime.of(2025, 1, 1, 11, 0),
            "Very Important", "Board Room", true);
    String expected = "Full Details: 2025-01-01T10:00 to 2025-01-01T11:00 "
            + "@ Board Room - Very Important [public]";
    assertEquals(expected, event.toString());
  }

  /**
   * Tests that an event is equal to itself.
   */
  @Test
  public void testEqualsSameObject() {
    Event event = new Event("Event", LocalDateTime.now(), LocalDateTime.now().plusHours(1));
    assertTrue(event.equals(event));
  }

  /**
   * Tests that an event is not equal to null.
   */
  @Test
  public void testEqualsNull() {
    Event event = new Event("Event", LocalDateTime.now(), LocalDateTime.now().plusHours(1));
    assertFalse(event == null);
  }

  /**
   * Tests that an event is not equal to an object of a different class.
   */
  @Test
  public void testEqualsDifferentClass() {
    Event event = new Event("Event", LocalDateTime.now(), LocalDateTime.now().plusHours(1));
    assertFalse(event.equals("SomeString"));
  }

  /**
   * Tests that two events with identical properties are considered equal.
   */
  @Test
  public void testEqualsIdenticalEvents() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 1, 11, 0);
    Event event1 = new Event("Identical", start, end,
            "Desc", "Loc", true);
    Event event2 = new Event("Identical", start, end,
            "Desc", "Loc", true);
    assertTrue(event1.equals(event2));
    assertTrue("Equals should be symmetric.", event2.equals(event1));
  }

  /**
   * Tests that two events with different subjects are not equal.
   */
  @Test
  public void testEqualsDifferentSubject() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 1, 11, 0);
    Event event1 = new Event("Subject A", start, end);
    Event event2 = new Event("Subject B", start, end);
    assertFalse(event1.equals(event2));
  }

  /**
   * Tests that two events with different start times are not equal.
   */
  @Test
  public void testEqualsDifferentStart() {
    Event event1 = new Event("Time Diff",
            LocalDateTime.of(2025, 1, 1, 10, 0),
            LocalDateTime.of(2025, 1, 1, 11, 0));
    Event event2 = new Event("Time Diff",
            LocalDateTime.of(2025, 1, 1, 10, 0, 1),
            LocalDateTime.of(2025, 1, 1, 11, 0));
    assertFalse(event1.equals(event2));
  }

  /**
   * Tests that the hash codes of two equal events are the same.
   */
  @Test
  public void testHashCodeConsistentForEqualEvents() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 1, 11, 0);
    Event event1 = new Event("Identical", start, end,
            "Desc", "Loc", true);
    Event event2 = new Event("Identical", start, end,
            "Desc", "Loc", true);
    assertEquals(event1.hashCode(), event2.hashCode());
  }

  /**
   * Tests that the hash codes of two unequal events are different.
   */
  @Test
  public void testHashCodeDifferentForUnequalEvents() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 1, 11, 0);
    Event event1 = new Event("Event One", start, end);
    Event event2 = new Event("Event Two", start, end);
    assertNotEquals(event1.hashCode(), event2.hashCode());
  }
}