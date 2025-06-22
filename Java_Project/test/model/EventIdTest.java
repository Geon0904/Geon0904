package model;

import calendarapp.model.EventId;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the EventId class, covering constructor, getters, equals,
 * hashCode, and toString methods.
 */
public class EventIdTest {

  /**
   * Tests the constructor and subsequent getter methods for subject, start, and end times.
   */
  @Test
  public void testConstructorAndGetters() {
    String subject = "Test Event";
    LocalDateTime start = LocalDateTime.of(2025, 7, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 7, 1, 11, 0);
    EventId eventId = new EventId(subject, start, end);

    assertEquals("Subject should match constructor argument.",
            subject, eventId.getSubject());
    assertEquals("Start time should match constructor argument.",
            start, eventId.getStart());
    assertEquals("End time should match constructor argument.",
            end, eventId.getEnd());
  }

  /**
   * Tests that an EventId instance is equal to itself.
   */
  @Test
  public void testEqualsSameObject() {
    EventId eventId = new EventId("Meeting",
            LocalDateTime.of(2025, 1, 1, 9, 0),
            LocalDateTime.of(2025, 1, 1, 10, 0));
    assertTrue("EventId should be equal to itself.", eventId.equals(eventId));
  }

  /**
   * Tests that an EventId instance is not equal to null.
   */
  @Test
  public void testEqualsNullObject() {
    EventId eventId = new EventId("Meeting",
            LocalDateTime.of(2025, 1, 1, 9, 0),
            LocalDateTime.of(2025, 1, 1, 10, 0));
    assertFalse("EventId should not be equal to null.", eventId == null);
  }

  /**
   * Tests that an EventId instance is not equal to an object of a different class.
   */
  @Test
  public void testEqualsDifferentClass() {
    EventId eventId = new EventId("Meeting",
            LocalDateTime.of(2025, 1, 1, 9, 0),
            LocalDateTime.of(2025, 1, 1, 10, 0));
    assertFalse("EventId should not be equal to an object of a different class.",
            eventId.equals("A String"));
  }

  /**
   * Tests that two EventId instances with same fields are considered equal.
   */
  @Test
  public void testEqualsIdenticalProperties() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 1, 10, 0);
    EventId eventId1 = new EventId("Conference", start, end);
    EventId eventId2 = new EventId("Conference", start, end);
    assertTrue("EventIds with identical properties should be equal.",
            eventId1.equals(eventId2));
    assertTrue("Equality should go both ways.", eventId2.equals(eventId1));
  }

  /**
   * Tests that EventId instances with different subjects are not equal.
   */
  @Test
  public void testEqualsDifferentSubject() {
    LocalDateTime start = LocalDateTime.of(2025, 1, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 1, 1, 10, 0);
    EventId eventId1 = new EventId("Event Alpha", start, end);
    EventId eventId2 = new EventId("Event Beta", start, end);
    assertFalse("EventIds with different subjects should not be equal.",
            eventId1.equals(eventId2));
  }

  /**
   * Tests that EventId instances with different start times are not equal.
   */
  @Test
  public void testEqualsDifferentStartTime() {
    String subject = "Workshop";
    LocalDateTime end = LocalDateTime.of(2025, 1, 1, 12, 0);
    EventId eventId1 = new EventId(subject,
            LocalDateTime.of(2025, 1, 1, 10, 0), end);
    EventId eventId2 = new EventId(subject,
            LocalDateTime.of(2025, 1, 1, 10, 30), end);
    assertFalse("EventIds with different start times should not be equal.",
            eventId1.equals(eventId2));
  }

  /**
   * Tests that EventId instances with different end times are not equal.
   */
  @Test
  public void testEqualsDifferentEndTime() {
    String subject = "Seminar";
    LocalDateTime start = LocalDateTime.of(2025, 1, 1, 14, 0);
    EventId eventId1 = new EventId(subject, start,
            LocalDateTime.of(2025, 1, 1, 15, 0));
    EventId eventId2 = new EventId(subject, start,
            LocalDateTime.of(2025, 1, 1, 15, 30));
    assertFalse("EventIds with different end times should not be equal.",
            eventId1.equals(eventId2));
  }

  /**
   * Tests that the hash codes of two equal EventId objects are themselves equal.
   */
  @Test
  public void testHashCodeConsistentForEqualObjects() {
    LocalDateTime start = LocalDateTime.of(2025, 3, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 3, 15, 11, 0);
    EventId eventId1 = new EventId("Team Sync", start, end);
    EventId eventId2 = new EventId("Team Sync", start, end);
    assertEquals("Hash codes should be consistent for equal objects.",
            eventId1.hashCode(), eventId2.hashCode());
  }

  /**
   * Tests that hash codes for unequal EventId objects are generally different.
   */
  @Test
  public void testHashCodeDifferentForUnequalObjects() {
    // Note: While not a strict guarantee due to possible hash collisions,
    // well-implemented hash functions usually produce different codes for unequal objects.
    EventId eventId1 = new EventId("Review",
            LocalDateTime.of(2025, 3, 15, 13, 0),
            LocalDateTime.of(2025, 3, 15, 14, 0));
    EventId eventId2 = new EventId("Planning",
            LocalDateTime.of(2025, 3, 15, 13, 0),
            LocalDateTime.of(2025, 3, 15, 14, 0));
    EventId eventId3 = new EventId("Review",
            LocalDateTime.of(2025, 3, 15, 14, 0),
            LocalDateTime.of(2025, 3, 15, 15, 0));

    assertNotEquals("Hash codes should generally differ "
            + "for objects with different subjects.", eventId1.hashCode(), eventId2.hashCode());
    assertNotEquals("Hash codes should generally differ "
            + "for objects with different times.", eventId1.hashCode(), eventId3.hashCode());
  }

  /**
   * Tests the format of the string representation produced by the toString() method.
   */
  @Test
  public void testToStringFormat() {
    String subject = "Sprint Demo";
    LocalDateTime start = LocalDateTime.of(2025, 6, 20, 16, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 20, 17, 0);
    EventId eventId = new EventId(subject, start, end);
    String expectedString = "\"" + subject + "\" from "
            + start.toString() + " to " + end.toString();
    assertEquals("toString format should match expected pattern.",
            expectedString, eventId.toString());
  }

  /**
   * Tests that passing null as the subject to the constructor throws NullPointerException.
   */
  @Test(expected = NullPointerException.class)
  public void testConstructorNullSubjectThrows() {
    LocalDateTime start = LocalDateTime.of(2025, 4, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 4, 1, 11, 0);
    new EventId(null, start, end);
  }

  /**
   * Tests that passing null as the start time to the constructor throws NullPointerException.
   */
  @Test(expected = NullPointerException.class)
  public void testConstructorNullStartThrows() {
    LocalDateTime end = LocalDateTime.of(2025, 4, 2, 11, 0);
    new EventId("NullStartTest", null, end);
  }

  /**
   * Tests that passing null as the end time to the constructor throws NullPointerException.
   */
  @Test(expected = NullPointerException.class)
  public void testConstructorNullEndThrows() {
    LocalDateTime start = LocalDateTime.of(2025, 4, 3, 10, 0);
    new EventId("NullEndTest", start, null);
  }
}