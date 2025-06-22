package integration;

import calendarapp.exceptions.DuplicateEventException;
import calendarapp.model.CalendarImpl;
import calendarapp.model.CalendarModel;
import calendarapp.model.Event;
import calendarapp.model.EventId;
import calendarapp.model.EventSeries;
import calendarapp.model.RecurrenceRule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for event overlaps, duplicate events, and related exceptions.
 * With TimeConflictException removed, overlapping events (distinct IDs) are allowed.
 */
public class EventConflictTest {

  private CalendarModel model;

  @Before
  public void setUp() {
    model = new CalendarImpl();
  }

  /**
   * Tests that adding distinct, non-overlapping events is successful.
   */
  @Test
  public void addDistinctNonOverlappingEvents() {
    Event eventA = new Event("Breakfast",
            LocalDateTime.of(2025, 12, 1, 10, 0),
            LocalDateTime.of(2025, 12, 1, 11, 0));
    model.addEvent(eventA);

    Event eventB = new Event("Lunch",
            LocalDateTime.of(2025, 12, 1, 11, 30),
            LocalDateTime.of(2025, 12, 1, 12, 0));
    model.addEvent(eventB); // Should add successfully.

    assertNotNull(model.getEventById(new EventId("Breakfast",
            eventA.getStart(), eventA.getEnd())));
    assertNotNull(model.getEventById(new EventId("Lunch",
            eventB.getStart(), eventB.getEnd())));
    assertEquals(2, model.getEventsOn(
            LocalDate.of(2025, 12, 1)).size());
  }

  /**
   * Tests that adding an event that exactly overlaps in time with an existing event
   * (but has a different subject) is allowed.
   */
  @Test
  public void addEventWithExactOverlapIsAllowed() {
    Event eventA = new Event("Breakfast",
            LocalDateTime.of(2025, 12, 1, 10, 0),
            LocalDateTime.of(2025, 12, 1, 11, 0));
    model.addEvent(eventA);

    Event overlappingEvent = new Event("Lunch", // Different subject
            LocalDateTime.of(2025, 12, 1, 10, 0),
            LocalDateTime.of(2025, 12, 1, 11, 0));
    model.addEvent(overlappingEvent); // Should add successfully

    assertNotNull(model.getEventById(new EventId(eventA.getSubject(),
            eventA.getStart(), eventA.getEnd())));
    assertNotNull(model.getEventById(new EventId(overlappingEvent.getSubject(),
            overlappingEvent.getStart(), overlappingEvent.getEnd())));
    assertEquals(2, model.getEventsOn(
            LocalDate.of(2025, 12, 1)).size());
  }

  /**
   * Tests that attempting to add an event identical to an existing one
   * (subject, start, end) throws DuplicateEventException.
   */
  @Test(expected = DuplicateEventException.class)
  public void addEventThrowsDuplicateEvent() {
    Event eventA = new Event("Breakfast",
            LocalDateTime.of(2025, 12, 1, 10, 0),
            LocalDateTime.of(2025, 12, 1, 11, 0));
    model.addEvent(eventA);
    Event duplicateEvent = new Event("Breakfast", // Identical
            LocalDateTime.of(2025, 12, 1, 10, 0),
            LocalDateTime.of(2025, 12, 1, 11, 0));
    model.addEvent(duplicateEvent);
  }

  /**
   * Tests adding an event that starts during an existing event (diff subject) is allowed.
   */
  @Test
  public void addEventStartsDuringExistingIsAllowed() {
    Event eventA = new Event("Breakfast",
            LocalDateTime.of(2025, 12, 1, 10, 0),
            LocalDateTime.of(2025, 12, 1, 11, 0));
    model.addEvent(eventA);
    Event overlappingEvent = new Event("Lunch", // Different subject
            LocalDateTime.of(2025, 12, 1, 10, 30),
            LocalDateTime.of(2025, 12, 1, 11, 30));
    model.addEvent(overlappingEvent); // Should be allowed.

    assertNotNull(model.getEventById(new EventId(eventA.getSubject(),
            eventA.getStart(), eventA.getEnd())));
    assertNotNull(model.getEventById(new EventId(overlappingEvent.getSubject(),
            overlappingEvent.getStart(), overlappingEvent.getEnd())));
  }

  /**
   * Tests adding an event that ends during an existing event (diff subject) is allowed.
   */
  @Test
  public void addEventEndsDuringExistingIsAllowed() {
    Event eventA = new Event("Breakfast",
            LocalDateTime.of(2025, 12, 1, 10, 0),
            LocalDateTime.of(2025, 12, 1, 11, 0));
    model.addEvent(eventA);
    Event overlappingEvent = new Event("Lunch", // Different subject
            LocalDateTime.of(2025, 12, 1, 9, 30),
            LocalDateTime.of(2025, 12, 1, 10, 30));
    model.addEvent(overlappingEvent); // Should be allowed.

    assertNotNull(model.getEventById(new EventId(eventA.getSubject(),
            eventA.getStart(), eventA.getEnd())));
    assertNotNull(model.getEventById(new EventId(overlappingEvent.getSubject(),
            overlappingEvent.getStart(), overlappingEvent.getEnd())));
  }

  /**
   * Tests adding event whose duration contains existing event (diff subject) is allowed.
   */
  @Test
  public void addEventNewContainsExistingIsAllowed() {
    Event eventA = new Event("Lunch", // Inner event
            LocalDateTime.of(2025, 12, 1, 10, 0),
            LocalDateTime.of(2025, 12, 1, 11, 0));
    model.addEvent(eventA);
    Event encompassingEvent = new Event("Breakfast", // Different subject, outer
            LocalDateTime.of(2025, 12, 1, 9, 0),
            LocalDateTime.of(2025, 12, 1, 12, 0));
    model.addEvent(encompassingEvent); // Should be allowed.

    assertNotNull(model.getEventById(new EventId(eventA.getSubject(),
            eventA.getStart(), eventA.getEnd())));
    assertNotNull(model.getEventById(new EventId(encompassingEvent.getSubject(),
            encompassingEvent.getStart(), encompassingEvent.getEnd())));
  }

  /**
   * Tests adding event whose duration is contained in existing (diff subject) is allowed.
   */
  @Test
  public void addEventExistingContainsNewIsAllowed() {
    Event eventA = new Event("Breakfast", // Outer event
            LocalDateTime.of(2025, 12, 1, 10, 0),
            LocalDateTime.of(2025, 12, 1, 11, 0));
    model.addEvent(eventA);
    Event innerEvent = new Event("Lunch", // Different subject, inner
            LocalDateTime.of(2025, 12, 1, 10, 15),
            LocalDateTime.of(2025, 12, 1, 10, 45));
    model.addEvent(innerEvent); // Should be allowed.

    assertNotNull(model.getEventById(new EventId(eventA.getSubject(),
            eventA.getStart(), eventA.getEnd())));
    assertNotNull(model.getEventById(new EventId(innerEvent.getSubject(),
            innerEvent.getStart(), innerEvent.getEnd())));
  }

  /**
   * Tests adding back-to-back events is allowed.
   */
  @Test
  public void addBackToBackStartIsAllowed() {
    Event eventA = new Event("Breakfast",
            LocalDateTime.of(2025, 12, 1, 10, 0),
            LocalDateTime.of(2025, 12, 1, 11, 0));
    model.addEvent(eventA);
    Event backToBack = new Event("Lunch",
            LocalDateTime.of(2025, 12, 1, 11, 0),
            LocalDateTime.of(2025, 12, 1, 12, 0));
    model.addEvent(backToBack); // Should be allowed.

    assertNotNull(model.getEventById(new EventId(eventA.getSubject(),
            eventA.getStart(), eventA.getEnd())));
    assertNotNull(model.getEventById(new EventId(backToBack.getSubject(),
            backToBack.getStart(), backToBack.getEnd())));
    assertEquals(2, model.getEventsOn(
            LocalDate.of(2025, 12, 1)).size());
  }

  /**
   * Tests Event constructor: end time before start time.
   */
  @Test(expected = IllegalArgumentException.class)
  public void eventEndBeforeStartThrowsException() {
    new Event("Dinner",
            LocalDateTime.of(2025, 12, 1, 11, 0),
            LocalDateTime.of(2025, 12, 1, 10, 0));
  }

  /**
   * Tests Event constructor: end time equal to start time.
   */
  @Test(expected = IllegalArgumentException.class)
  public void eventEndEqualsStartThrowsException() {
    new Event("Dinner",
            LocalDateTime.of(2025, 12, 1, 10, 0),
            LocalDateTime.of(2025, 12, 1, 10, 0));
  }

  /**
   * Tests adding event series where an occurrence overlaps an existing event is allowed.
   */
  @Test
  public void addEventSeriesWithOverlapIsAllowed() {
    Event existing = new Event("Breakfast", // Monday
            LocalDateTime.of(2025, 12, 1, 10, 0),
            LocalDateTime.of(2025, 12, 1, 11, 0));
    model.addEvent(existing);
    EventId existingId = new EventId(existing.getSubject(),
            existing.getStart(), existing.getEnd());

    EnumSet<DayOfWeek> days = EnumSet.of(DayOfWeek.MONDAY);
    RecurrenceRule rule = new RecurrenceRule(days, 1); // Dec 1 (10-10:30), Dec 8 (10-10:30)
    EventSeries series = new EventSeries("Lunch", // Different subject
            LocalDateTime.of(2025, 12, 1, 10, 0),
            LocalDateTime.of(2025, 12, 1, 10, 30),
            "", "", true, rule);
    model.addEventSeries(series); // Should be allowed.

    assertNotNull("Existing event should still be present.",
            model.getEventById(existingId));
    EventId seriesId1 = new EventId(series.getSubject(),
            series.getEvents().get(0).getStart(), series.getEvents().get(0).getEnd());
    EventId seriesId2 = new EventId(series.getSubject(),
            series.getEvents().get(1).getStart(), series.getEvents().get(1).getEnd());

    assertNotNull("First series event should be present.", model.getEventById(seriesId1));
    assertNotNull("Second series event should be present.", model.getEventById(seriesId2));

    List<Event> eventsOnDate = model.getEventsOn(LocalDate.of(2025, 12, 1));
    String msg = "Should be 2 events (Existing and Series event 1) on Dec 1.";
    assertEquals(msg, 2, eventsOnDate.size());
    assertTrue(eventsOnDate.contains(existing));
    assertTrue(eventsOnDate.contains(model.getEventById(seriesId1)));
  }

  /**
   * Tests adding all-day event when timed event exists on that day (overlap allowed).
   */
  @Test
  public void addAllDayEventWithExistingTimedEventIsAllowed() {
    Event timedEvent = new Event("Lunch",
            LocalDateTime.of(2025, 12, 2, 14, 0),
            LocalDateTime.of(2025, 12, 2, 15, 0));
    model.addEvent(timedEvent);
    EventId timedId = new EventId(timedEvent.getSubject(),
            timedEvent.getStart(), timedEvent.getEnd());

    Event allDayEvent = new Event("Dinner", // All-day event
            LocalDateTime.of(2025, 12, 2, 9, 0),
            null); // Creates an 8-5 event for Dec 2
    model.addEvent(allDayEvent);
    EventId allDayId = new EventId(allDayEvent.getSubject(),
            allDayEvent.getStart(), allDayEvent.getEnd());

    assertNotNull(model.getEventById(timedId));
    assertNotNull(model.getEventById(allDayId));
    assertEquals(2, model.getEventsOn(
            LocalDate.of(2025, 12, 2)).size());
  }

  /**
   * Tests adding timed event when all-day event exists on that day (overlap allowed).
   */
  @Test
  public void addTimedEventWithExistingAllDayEventIsAllowed() {
    Event allDayEvent = new Event("Breakfast", // All-day event
            LocalDateTime.of(2025, 12, 3, 10, 0),
            null); // Creates an 8-5 event for Dec 3
    model.addEvent(allDayEvent);
    EventId allDayId = new EventId(allDayEvent.getSubject(),
            allDayEvent.getStart(), allDayEvent.getEnd());

    Event timedEvent = new Event("Lunch",
            LocalDateTime.of(2025, 12, 3, 12, 0),
            LocalDateTime.of(2025, 12, 3, 13, 0));
    model.addEvent(timedEvent);
    EventId timedId = new EventId(timedEvent.getSubject(),
            timedEvent.getStart(), timedEvent.getEnd());

    assertNotNull(model.getEventById(allDayId));
    assertNotNull(model.getEventById(timedId));
    assertEquals(2, model.getEventsOn(
            LocalDate.of(2025, 12, 3)).size());
  }

  /**
   * Tests that two identical all‐day events throw DuplicateEventException.
   */
  @Test(expected = DuplicateEventException.class)
  public void addDuplicateAllDayEventThrowsDuplicate() {

    Event firstAllDay = new Event("Holiday",
            LocalDateTime.of(2025, 12, 4, 9, 0),
            null);
    model.addEvent(firstAllDay);


    Event secondAllDay = new Event("Holiday",
            LocalDateTime.of(2025, 12, 4, 10, 0),
            null);
    model.addEvent(secondAllDay);
  }

  /**
   * Tests that adding a series whose first occurrence exactly matches an existing event
   * throws DuplicateEventException.
   */
  @Test(expected = DuplicateEventException.class)
  public void addSeriesThatDuplicatesExistingSingleThrowsDuplicate() {

    Event single = new Event("Meeting",
            LocalDateTime.of(2025, 12, 5, 10, 0),
            LocalDateTime.of(2025, 12, 5, 11, 0));
    model.addEvent(single);


    RecurrenceRule rule = new RecurrenceRule(
            EnumSet.of(DayOfWeek.FRIDAY), 1
    );
    EventSeries series = new EventSeries("Meeting",
            LocalDateTime.of(2025, 12, 5, 10, 0),
            LocalDateTime.of(2025, 12, 5, 11, 0),
            "", "", true, rule);

    model.addEventSeries(series);
  }

  /**
   * Tests that a series with multiple weekdays creates the correct number of occurrences.
   */
  @Test
  public void addSeriesWithMultipleWeekdaysCreatesCorrectCount() {
    RecurrenceRule rule = new RecurrenceRule(
            EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
            2
    );

    EventSeries series = new EventSeries("Class",
            LocalDateTime.of(2025, 12, 2, 8, 0),
            LocalDateTime.of(2025, 12, 2, 9, 0),
            "", "", false, rule);

    model.addEventSeries(series);

    List<Event> allEvents = model.getEventsOn(LocalDate.of(2025, 12, 2));
    allEvents.addAll(model.getEventsOn(LocalDate.of(2025, 12, 4)));
    assertEquals(2, allEvents.size());
  }


  /**
   * Tests that a series with an 'until' date that skips all given weekdays
   * still creates at least the start occurrence.
   */
  @Test
  public void addSeriesUntilNoFutureMatchesCreatesOnlyStart() {
    RecurrenceRule rule = new RecurrenceRule(
            EnumSet.of(DayOfWeek.FRIDAY),
            LocalDate.parse("2025-12-02")
    );
    EventSeries series = new EventSeries("Special",
            LocalDateTime.of(2025, 12, 1, 12, 0),
            LocalDateTime.of(2025, 12, 1, 13, 0),
            "", "", false, rule);

    model.addEventSeries(series);


    List<Event> eventsOnDec1 = model.getEventsOn(LocalDate.of(2025, 12, 1));
    List<Event> eventsOnDec2 = model.getEventsOn(LocalDate.of(2025, 12, 2));
    assertEquals(1, eventsOnDec1.size());
    assertTrue(eventsOnDec2.isEmpty());
  }

  /**
   * Tests that adding a multi‐day event and then
   * adding another timed event on Dec 2 01:00 -> 03:00 with a different subject is allowed.
   */
  @Test
  public void addMultiDayEventAndSubsequentTimedEventOnMiddleDayIsAllowed() {
    Event multiDay = new Event("Overnight",
            LocalDateTime.of(2025, 12, 1, 22, 0),
            LocalDateTime.of(2025, 12, 2, 2, 0));
    model.addEvent(multiDay);


    Event partial = new Event("EarlyMeeting",
            LocalDateTime.of(2025, 12, 2, 1, 0),
            LocalDateTime.of(2025, 12, 2, 3, 0));
    model.addEvent(partial);

    EventId overnightId = new EventId(multiDay.getSubject(), multiDay.getStart(),
            multiDay.getEnd());
    EventId partialId   = new EventId(partial.getSubject(),   partial.getStart(),
            partial.getEnd());
    assertNotNull(model.getEventById(overnightId));
    assertNotNull(model.getEventById(partialId));

    List<Event> dec2Events = model.getEventsOn(LocalDate.of(2025, 12, 2));
    assertEquals(2, dec2Events.size());
    assertTrue(dec2Events.contains(multiDay));
    assertTrue(dec2Events.contains(partial));
  }

  /**
   * Tests that adding a single‐day event on Dec 3 (10:00–11:00) and then
   * adding an all‐day event on Dec 3 is allowed,
   * even though their time‐ranges overlap.
   */
  @Test
  public void addTimedAndAllDayEventSameDateIsAllowed() {
    Event timed = new Event("Standup",
            LocalDateTime.of(2025, 12, 3, 10, 0),
            LocalDateTime.of(2025, 12, 3, 11, 0));
    model.addEvent(timed);


    Event allDay = new Event("OfficeHoliday",
            LocalDateTime.of(2025, 12, 3, 9, 0),
            null);
    model.addEvent(allDay);

    EventId timedId  = new EventId(timed.getSubject(), timed.getStart(), timed.getEnd());
    EventId allDayId = new EventId(allDay.getSubject(), allDay.getStart(), allDay.getEnd());
    assertNotNull(model.getEventById(timedId));
    assertNotNull(model.getEventById(allDayId));

    List<Event> dec3 = model.getEventsOn(LocalDate.of(2025, 12, 3));
    assertEquals(2, dec3.size());
    assertTrue(dec3.contains(timed));
    assertTrue(dec3.contains(allDay));
  }


  @Test(expected = IllegalArgumentException.class)
  public void addEventNullSubjectThrows() {
    model.addEvent(new Event(null, LocalDateTime.of(2025, 12,
            1, 10, 0), LocalDateTime.of(2025, 12,
            1, 11, 0)));
  }


}