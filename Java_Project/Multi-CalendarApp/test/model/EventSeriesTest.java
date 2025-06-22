package model;

import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;

import calendarapp.model.Event;
import calendarapp.model.EventSeries;
import calendarapp.model.RecurrenceRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for the EventSeries class, verifying constructors,
 * event generation, and property access.
 */
public class EventSeriesTest {

  /**
   * Tests the full constructor with valid inputs, verifying
   * series properties and generated event details.
   */
  @Test
  public void testFullConstructorValidSeries() {
    LocalDateTime seriesStartTime =
            LocalDateTime.of(2025, 6, 2, 10, 0);
    LocalDateTime seriesEndTime =
            LocalDateTime.of(2025, 6, 2, 11, 0);
    // For 2 total events: 1 additional occurrence
    RecurrenceRule rule = new RecurrenceRule(EnumSet.of(DayOfWeek.MONDAY), 1);
    EventSeries series = new EventSeries("Weekly Standup", seriesStartTime, seriesEndTime,
            "Team sync", "Office", true, rule);

    assertEquals("Weekly Standup", series.getSubject());
    assertEquals(seriesStartTime, series.getStartTime());
    assertEquals(seriesEndTime, series.getEndTime());
    assertEquals("Team sync", series.getDescription());
    assertEquals("Office", series.getLocation());
    assertTrue(series.isPublic());
    assertSame(rule, series.getRule());

    List<Event> events = series.getEvents();
    // RecurrenceRule with count=1 generates 1 additional event.
    // generateOccurrences returns first event + count additional. So 1+1=2.
    assertEquals(2, events.size());

    Event event1 = events.get(0);
    assertEquals("Weekly Standup", event1.getSubject());
    assertEquals(LocalDateTime.of(2025, 6, 2, 10, 0),
            event1.getStart());
    assertEquals(LocalDateTime.of(2025, 6, 2, 11, 0),
            event1.getEnd());
    assertEquals("Team sync", event1.getDescription());

    Event event2 = events.get(1);
    assertEquals("Weekly Standup", event2.getSubject());
    // RecurrenceIterator used by generateOccurrences starts from day *after* first event's date.
    // First event is June 2 (Monday). Rule is for MONDAY, count is 1.
    // Iterator will find next Monday, June 9.
    assertEquals(LocalDateTime.of(2025, 6, 9, 10, 0),
            event2.getStart());
    assertEquals(LocalDateTime.of(2025, 6, 9, 11, 0), // End time mirrors original event's time
            event2.getEnd());
    assertEquals("Team sync", event2.getDescription());
  }

  /**
   * Tests that the full constructor throws IllegalArgumentException
   * if the series start time is null.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testFullConstructorNullStartTime() {
    LocalDateTime seriesEndTime =
            LocalDateTime.of(2025, 6, 2, 11, 0);
    RecurrenceRule rule = new RecurrenceRule(EnumSet.of(DayOfWeek.MONDAY), 1);
    new EventSeries("Test", null, seriesEndTime, "",
            "", true, rule);
  }

  /**
   * Tests that the full constructor throws IllegalArgumentException
   * if the series end time is null.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testFullConstructorNullEndTime() {
    LocalDateTime seriesStartTime =
            LocalDateTime.of(2025, 6, 2, 10, 0);
    RecurrenceRule rule = new RecurrenceRule(EnumSet.of(DayOfWeek.MONDAY), 1);
    new EventSeries("Test", seriesStartTime, null,
            "", "", true, rule);
  }

  /**
   * Tests that the full constructor throws IllegalArgumentException
   * if series start and end times are on different dates.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testFullConstructorStartAndEndDifferentDates() {
    LocalDateTime seriesStartTime =
            LocalDateTime.of(2025, 6, 2, 10, 0);
    LocalDateTime seriesEndTime =
            LocalDateTime.of(2025, 6, 3, 11, 0); // Different date
    RecurrenceRule rule = new RecurrenceRule(EnumSet.of(DayOfWeek.MONDAY), 1);
    new EventSeries("Test", seriesStartTime, seriesEndTime, "",
            "", true, rule);
  }

  /**
   * Tests the secondary constructor (LocalDate + LocalTime input) for valid series creation.
   */
  @Test
  public void testSecondaryConstructor() {
    LocalTime startTime = LocalTime.of(14, 0);
    LocalTime endTime = LocalTime.of(15, 0);
    LocalDate date = LocalDate.of(2025, 6, 4); // Wednesday
    RecurrenceRule rule = new RecurrenceRule(EnumSet.of(DayOfWeek.WEDNESDAY), 1); // 1 more
    EventSeries series = new EventSeries("Afternoon Class", startTime, endTime, date, rule);

    assertEquals("Afternoon Class", series.getSubject());
    assertEquals(LocalDateTime.of(date, startTime), series.getStartTime());
    assertEquals(LocalDateTime.of(date, endTime), series.getEndTime());
    assertEquals("Default description should be empty.", "",
            series.getDescription());
    assertTrue("Default status should be public.", series.isPublic());

    List<Event> events = series.getEvents();
    // Initial (June 4) + 1 additional (June 11)
    assertEquals(2, events.size());
    assertEquals(LocalDateTime.of(date, startTime), events.get(0).getStart());
    assertEquals(
            LocalDateTime.of(date.plusWeeks(1), startTime), events.get(1).getStart());
  }

  /**
   * Tests that the list of events returned by getEvents() is unmodifiable.
   */
  @Test(expected = UnsupportedOperationException.class)
  public void testGetEventsIsUnmodifiable() {
    RecurrenceRule rule = new RecurrenceRule(EnumSet.of(DayOfWeek.MONDAY), 1);
    EventSeries series = new EventSeries("Test",
            LocalDateTime.of(2025, 1, 6, 10, 0),
            LocalDateTime.of(2025, 1, 6, 11, 0),
            "", "", true, rule);
    List<Event> events = series.getEvents();
    // Attempt to modify the list
    events.add(new Event("Another", LocalDateTime.now(), LocalDateTime.now().plusHours(1)));
    fail("Should have thrown UnsupportedOperationException when trying to modify the events list.");
  }


  /**
   * Tests that generated events in a series correctly use the hour and minute
   * from the series' original end time.
   */
  @Test
  public void testEventSeriesGeneratesCorrectEndTimesForEvents() {
    LocalDateTime seriesStartTime =
            LocalDateTime.of(2025, 7, 1, 9, 15);  // Tuesday
    LocalDateTime seriesEndTime =
            LocalDateTime.of(2025, 7, 1, 10, 45); // Same day, different time
    // For 2 total events: 1 additional
    RecurrenceRule rule = new RecurrenceRule(EnumSet.of(DayOfWeek.TUESDAY), 1);
    EventSeries series = new EventSeries("Time Check", seriesStartTime, seriesEndTime,
            "", "", true, rule);

    List<Event> events = series.getEvents();
    assertEquals(2, events.size());

    Event event1 = events.get(0);
    assertEquals(LocalTime.of(9, 15), event1.getStart().toLocalTime());
    assertEquals("End time of generated event should match time part of seriesEndTime.",
            LocalTime.of(10, 45), event1.getEnd().toLocalTime());
    assertEquals(LocalDate.of(2025, 7, 1), event1.getStart().toLocalDate());
    assertEquals(LocalDate.of(2025, 7, 1), event1.getEnd().toLocalDate());


    Event event2 = events.get(1); // Next Tuesday, July 8
    assertEquals(LocalTime.of(9, 15), event2.getStart().toLocalTime());
    assertEquals("End time of generated event should match time part of seriesEndTime.",
            LocalTime.of(10, 45), event2.getEnd().toLocalTime());
    assertEquals(LocalDate.of(2025, 7, 8), event2.getStart().toLocalDate());
    assertEquals(LocalDate.of(2025, 7, 8), event2.getEnd().toLocalDate());
  }

  /**
   * Tests the toString() method format for an EventSeries.
   */
  @Test
  public void testToStringFormat() {
    // For 3 total events: 2 additional occurrences
    RecurrenceRule rule = new RecurrenceRule(EnumSet.of(DayOfWeek.MONDAY), 2);
    EventSeries series = new EventSeries("My Series",
            LocalDateTime.of(2025, 8, 4, 10, 0),
            LocalDateTime.of(2025, 8, 4, 11, 0),
            "", "", true, rule);
    // 1 initial + 2 more = 3 Events
    String expected = "Series: \"My Series\" (3 events)";
    assertEquals(expected, series.toString());
  }
}