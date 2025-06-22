package model;

import calendarapp.exceptions.DuplicateEventException;
import calendarapp.model.CalendarImpl;
import calendarapp.model.CalendarManagerImpl;
import calendarapp.model.CalendarModel;
import calendarapp.model.Event;
import calendarapp.model.EventId;
import calendarapp.model.EventSeries;
import calendarapp.model.RecurrenceRule;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.time.DayOfWeek;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the CalendarImpl class, covering event and series management,
 * querying, and editing functionalities.
 */
public class CalendarImplTest {

  private CalendarModel model;
  private CalendarManagerImpl mgr;

  /**
   * Initializes a new CalendarModel for each test.
   */
  @Before
  public void setUp() {
    model = new CalendarImpl();
    mgr = new CalendarManagerImpl();
    mgr.createCalendar("Events", ZoneId.of("UTC"));
    mgr.useCalendar("Events");
  }

  /**
   * Tests adding a single valid event to the calendar.
   */
  @Test
  public void addValidEvent() {
    Event event = new Event("Breakfast",
            LocalDateTime.of(2025, 7, 1, 10, 0),
            LocalDateTime.of(2025, 7, 1, 11, 0));
    model.addEvent(event);
    EventId eventId = new EventId("Breakfast",
            LocalDateTime.of(2025, 7, 1, 10, 0),
            LocalDateTime.of(2025, 7, 1, 11, 0));
    assertNotNull("Added event should be retrievable by its ID.",
            model.getEventById(eventId));
    assertEquals("Retrieved event should be the one that was added.", event,
            model.getEventById(eventId));
  }


  /**
   * Events are still reported with the correct local date/time after a timezone change.
   */
  @Test
  public void eventsReflectTimezoneAfterChange() {
    CalendarModel cal = mgr.getActiveCalendar();

    LocalDateTime utcStart = LocalDateTime.of(2025, 7, 10, 14, 0);
    LocalDateTime utcEnd   = utcStart.plusHours(1);
    cal.addEvent(new Event("Meeting", utcStart, utcEnd));


    mgr.editCalendar("Events", "timezone", "America/New_York");


    List<Event> events = cal.getEventsOn(LocalDate.of(2025, 7, 10));
    assertEquals(1, events.size());
    assertEquals(utcStart, events.get(0).getStart());
  }


  /**
   * Tests that attempting to add a duplicate event throws a DuplicateEventException.
   */
  @Test(expected = DuplicateEventException.class)
  public void addDuplicateEventThrowsException() {
    Event event1 = new Event("Duplicate Test", // Subject name indicates test purpose
            LocalDateTime.of(2025, 7, 2, 10, 0),
            LocalDateTime.of(2025, 7, 2, 11, 0));
    model.addEvent(event1);
    Event event2 = new Event("Duplicate Test",
            LocalDateTime.of(2025, 7, 2, 10, 0),
            LocalDateTime.of(2025, 7, 2, 11, 0));
    model.addEvent(event2);
  }

  /**
   * Tests that adding an event that overlaps with an existing event (different subject)
   * is now allowed and both events are present.
   */
  @Test
  public void addOverlappingEventIsAllowed() {
    Event event1 = new Event("Breakfast",
            LocalDateTime.of(2025, 7, 3, 10, 0),
            LocalDateTime.of(2025, 7, 3, 12, 0));
    model.addEvent(event1);
    EventId event1Id = new EventId(event1.getSubject(), event1.getStart(), event1.getEnd());

    Event event2 = new Event("Lunch", // Different subject
            LocalDateTime.of(2025, 7, 3, 11, 0), // Overlaps with Breakfast
            LocalDateTime.of(2025, 7, 3, 13, 0));
    model.addEvent(event2); // Should now be allowed
    EventId event2Id = new EventId(event2.getSubject(), event2.getStart(), event2.getEnd());

    assertNotNull("Breakfast event should be in the calendar.",
            model.getEventById(event1Id));
    assertNotNull("Lunch (overlapping) event should also be in the calendar.",
            model.getEventById(event2Id));
    assertEquals(2,
            model.getEventsOn(LocalDate.of(2025, 7, 3)).size());
  }

  /**
   * Tests adding a valid event series, ensuring all individual events are added.
   */
  @Test
  public void addValidEventSeries() {
    RecurrenceRule rule = new RecurrenceRule(EnumSet.of(DayOfWeek.MONDAY), 1); // 2 events
    EventSeries series = new EventSeries("Dinner",
            LocalDateTime.of(2025, 7, 7, 9, 0), // Monday
            LocalDateTime.of(2025, 7, 7, 10, 0),
            "Status update", "Online", true, rule);
    model.addEventSeries(series);

    assertEquals("Two events should be generated from the series.",
            2, series.getEvents().size());
    assertNotNull(model.getEventById(new EventId("Dinner",
            LocalDateTime.of(2025, 7, 7, 9, 0),
            LocalDateTime.of(2025, 7, 7, 10, 0))));
    assertNotNull(model.getEventById(new EventId("Dinner",
            LocalDateTime.of(2025, 7, 14, 9, 0), // Next Monday
            LocalDateTime.of(2025, 7, 14, 10, 0))));
  }

  /**
   * Tests that adding an event series where one of its generated events overlaps
   * with a pre-existing event is now allowed.
   */
  @Test
  public void addEventSeriesWithOverlapIsAllowed() {
    Event blockerEvent = new Event("Breakfast",
            LocalDateTime.of(2025, 7, 7, 9, 30),
            LocalDateTime.of(2025, 7, 7, 10, 30));
    model.addEvent(blockerEvent);
    EventId blockerId = new EventId(blockerEvent.getSubject(),
            blockerEvent.getStart(), blockerEvent.getEnd());

    RecurrenceRule rule = new RecurrenceRule(EnumSet.of(DayOfWeek.MONDAY), 1); // 2 events
    EventSeries series = new EventSeries("Lunch",
            LocalDateTime.of(2025, 7, 7, 9, 0), // Monday
            LocalDateTime.of(2025, 7, 7, 10, 0),//first overlaps
            "", "", true, rule);
    model.addEventSeries(series); // Should now be allowed

    assertNotNull("Blocker event should still be present.", model.getEventById(blockerId));
    EventId seriesEvent1Id = new EventId("Lunch",
            LocalDateTime.of(2025, 7, 7, 9, 0),
            LocalDateTime.of(2025, 7, 7, 10, 0));
    EventId seriesEvent2Id = new EventId("Lunch",
            LocalDateTime.of(2025, 7, 14, 9, 0),
            LocalDateTime.of(2025, 7, 14, 10, 0));
    assertNotNull("First event of the series should be present.",
            model.getEventById(seriesEvent1Id));
    assertNotNull("Second event of the series should be present.",
            model.getEventById(seriesEvent2Id));

    List<Event> eventsOnDate = model.getEventsOn(LocalDate.of(2025, 7, 7));
    assertEquals("Should be 2 events on 2025-07-07 (Blocker and Series event 1).",
            2, eventsOnDate.size());
  }

  /**
   * Tests retrieving events on a date when no events are scheduled.
   */
  @Test
  public void getEventsOnDateNone() {
    List<Event> events = model.getEventsOn(LocalDate.of(2025, 7, 1));
    assertTrue("Should return an empty list if no events are scheduled on the date.",
            events.isEmpty());
  }

  /**
   * Tests retrieving events on a date when a single event is scheduled.
   */
  @Test
  public void getEventsOnDateSingle() {
    Event event = new Event("Breakfast",
            LocalDateTime.of(2025, 7, 4, 14, 0),
            LocalDateTime.of(2025, 7, 4, 15, 0));
    model.addEvent(event);
    List<Event> events = model.getEventsOn(LocalDate.of(2025, 7, 4));
    assertEquals("Should return one event.", 1, events.size());
    assertEquals("Returned event should match the added one.", event, events.get(0));
  }

  /**
   * Tests retrieving multiple events on a date, ensuring they are sorted by start time.
   */
  @Test
  public void getEventsOnDateMultipleSorted() {
    Event event1 = new Event("Lunch", // 10:00 - 11:00
            LocalDateTime.of(2025, 7, 5, 10, 0),
            LocalDateTime.of(2025, 7, 5, 11, 0));
    Event event2 = new Event("Breakfast", // 09:00 - 09:30
            LocalDateTime.of(2025, 7, 5, 9, 0),
            LocalDateTime.of(2025, 7, 5, 9, 30));
    model.addEvent(event1);
    model.addEvent(event2);
    List<Event> events = model.getEventsOn(LocalDate.of(2025, 7, 5));
    assertEquals("Should return two events.", 2, events.size());
    String sortMsg1 = "Events should be sorted by start time; Breakfast should be first.";
    assertEquals(sortMsg1, event2, events.get(0));
    String sortMsg2 = "Events should be sorted by start time; Lunch should be second.";
    assertEquals(sortMsg2, event1, events.get(1));
  }

  /**
   * Tests retrieving events between a date-time range when no events fall in range.
   */
  @Test
  public void getEventsBetweenNone() {
    List<Event> events = model.getEventsBetween(
            LocalDateTime.of(2025, 7, 6, 0, 0),
            LocalDateTime.of(2025, 7, 6, 23, 59));
    assertTrue("Should return an empty list if no events are within the specified range.",
            events.isEmpty());
  }

  /**
   * Tests retrieving an event that is fully contained within the date-time range.
   */
  @Test
  public void getEventsBetweenFullyContained() {
    Event event = new Event("Lunch",
            LocalDateTime.of(2025, 7, 7, 12, 0),
            LocalDateTime.of(2025, 7, 7, 13, 0));
    model.addEvent(event);
    List<Event> events = model.getEventsBetween(
            LocalDateTime.of(2025, 7, 7, 10, 0),
            LocalDateTime.of(2025, 7, 7, 14, 0));
    assertEquals("Should find one event fully contained in the range.", 1,
            events.size());
    assertEquals(event, events.get(0));
  }


  /**
   * Tests retrieving events that align with start/end boundaries of the range.
   */
  @Test
  public void getEventsBetweenBoundaryConditions() {
    Event eventStartBoundary = new Event("Breakfast",
            LocalDateTime.of(2025, 7, 8, 10, 0),
            LocalDateTime.of(2025, 7, 8, 11, 0));
    Event eventEndBoundary = new Event("Lunch",
            LocalDateTime.of(2025, 7, 8, 13, 0),
            LocalDateTime.of(2025, 7, 8, 14, 0));
    model.addEvent(eventStartBoundary);
    model.addEvent(eventEndBoundary);

    List<Event> events = model.getEventsBetween( // 10:00 to 14:00
            LocalDateTime.of(2025, 7, 8, 10, 0),
            LocalDateTime.of(2025, 7, 8, 14, 0));
    assertEquals("Should find two events that meet boundary conditions.", 2,
            events.size());
    assertTrue("List should contain event at start boundary.",
            events.contains(eventStartBoundary));
    assertTrue("List should contain event at end boundary.",
            events.contains(eventEndBoundary));
  }

  /**
   * Tests isBusyAt method for times before, during, at start, and at/after an event.
   */
  @Test
  public void isBusyAtVariousTimes() {
    Event event = new Event("Dinner",
            LocalDateTime.of(2025, 7, 9, 15, 0),
            LocalDateTime.of(2025, 7, 9, 16, 0));
    model.addEvent(event);

    assertFalse("Should not be busy before the event starts.", model.isBusyAt(
            LocalDateTime.of(2025, 7, 9, 14, 0)));
    assertTrue("Should be busy exactly at the event start time.", model.isBusyAt(
            LocalDateTime.of(2025, 7, 9, 15, 0)));
    assertTrue("Should be busy during the event.", model.isBusyAt(LocalDateTime.of(
            2025, 7, 9, 15, 30)));
    assertFalse("Should not be busy exactly at event end time (exclusive end).", model
            .isBusyAt(LocalDateTime.of(2025, 7, 9, 16, 0)));
    assertFalse("Should not be busy after the event ends.", model.isBusyAt(
            LocalDateTime.of(2025, 7, 9, 17, 0)));
  }

  /**
   * Tests retrieving an existing event by its EventId.
   */
  @Test
  public void getEventByIdExisting() {
    Event event = new Event("Breakfast",
            LocalDateTime.of(2025, 7, 10, 10, 0),
            LocalDateTime.of(2025, 7, 10, 11, 0));
    EventId id = new EventId("Breakfast",
            LocalDateTime.of(2025, 7, 10, 10, 0),
            LocalDateTime.of(2025, 7, 10, 11, 0));
    model.addEvent(event);
    assertEquals(event, model.getEventById(id));
  }

  /**
   * Tests retrieving non-existing event by EventId, expecting NoSuchElementException.
   */
  @Test(expected = NoSuchElementException.class)
  public void getEventByIdNonExisting() {
    EventId id = new EventId("Lunch",
            LocalDateTime.of(2025, 7, 11, 10, 0),
            LocalDateTime.of(2025, 7, 11, 11, 0));
    model.getEventById(id); // Should throw
  }

  /**
   * Tests successfully editing an existing event.
   */
  @Test
  public void editEventSuccessfully() {
    Event originalEvent = new Event("Breakfast",
            LocalDateTime.of(2025, 8, 1, 9, 0),
            LocalDateTime.of(2025, 8, 1, 10, 0));
    EventId originalId = new EventId("Breakfast",
            LocalDateTime.of(2025, 8, 1, 9, 0),
            LocalDateTime.of(2025, 8, 1, 10, 0));
    model.addEvent(originalEvent);

    Event updatedEvent = new Event("Lunch", // New subject
            LocalDateTime.of(2025, 8, 1, 9, 30), // New start
            LocalDateTime.of(2025, 8, 1, 10, 30), // New end
            "New Desc", "New Loc", false);
    model.editEvent(originalId, updatedEvent);

    boolean originalEventFound = true;
    try {
      model.getEventById(originalId);
    } catch (NoSuchElementException e) {
      originalEventFound = false; // Expected: original ID is gone
    }
    assertFalse("Original event ID should no longer be retrievable.", originalEventFound);

    EventId updatedId = new EventId(updatedEvent.getSubject(),
            updatedEvent.getStart(), updatedEvent.getEnd());
    assertEquals("Updated event should be retrievable by its new ID.",
            updatedEvent, model.getEventById(updatedId));
  }

  /**
   * Tests that attempting to edit a non-existing event throws NoSuchElementException.
   */
  @Test(expected = NoSuchElementException.class)
  public void editNonExistingEvent() {
    EventId nonExistingId = new EventId("Dinner",
            LocalDateTime.of(2025, 8, 2, 10, 0),
            LocalDateTime.of(2025, 8, 2, 11, 0));
    Event someEvent = new Event("Breakfast",
            LocalDateTime.of(2025, 8, 2, 10, 0),
            LocalDateTime.of(2025, 8, 2, 11, 0));
    model.editEvent(nonExistingId, someEvent);
  }

  /**
   * Tests that editing an event to a time that overlaps with another existing event
   * is now allowed.
   */
  @Test
  public void editEventToOverlapIsAllowed() {
    Event event1 = new Event("Breakfast",
            LocalDateTime.of(2025, 8, 3, 10, 0),
            LocalDateTime.of(2025, 8, 3, 11, 0));
    Event event2 = new Event("Lunch", // Originally 12:00 - 13:00
            LocalDateTime.of(2025, 8, 3, 12, 0),
            LocalDateTime.of(2025, 8, 3, 13, 0));
    EventId event2IdOriginal = new EventId(event2.getSubject(),
            event2.getStart(), event2.getEnd());
    model.addEvent(event1);
    model.addEvent(event2);

    // Edit event2 to overlap with event1: 10:30 - 11:30
    Event conflictingUpdate = new Event(event2.getSubject(),
            LocalDateTime.of(2025, 8, 3, 10, 30),
            LocalDateTime.of(2025, 8, 3, 11, 30));
    model.editEvent(event2IdOriginal, conflictingUpdate); // Should now be allowed

    EventId event1Id = new EventId(event1.getSubject(), event1.getStart(), event1.getEnd());
    EventId event2IdUpdated = new EventId(conflictingUpdate.getSubject(),
            conflictingUpdate.getStart(), conflictingUpdate.getEnd());

    assertNotNull("Breakfast event should still be present.",
            model.getEventById(event1Id));
    assertNotNull("Updated Lunch event should be present.",
            model.getEventById(event2IdUpdated));
    assertEquals(conflictingUpdate, model.getEventById(event2IdUpdated));

    List<Event> eventsOnDate = model.getEventsOn(LocalDate.of(2025, 8, 3));
    assertEquals("There should be two events on the date.",
            2, eventsOnDate.size());
    assertTrue(eventsOnDate.contains(event1));
    assertTrue(eventsOnDate.contains(conflictingUpdate));
  }


  /**
   * Tests editing the subject of events starting from a specific event in a series.
   */
  @Test
  public void editEventsFromSubject() {
    LocalDateTime start1Original =
            LocalDateTime.of(2025, 9, 1, 10, 0);
    LocalDateTime end1Original =
            LocalDateTime.of(2025, 9, 1, 11, 0);
    Event originalEvent = new Event("Breakfast", start1Original, end1Original,
            "Desc1", "Loc1", true);
    model.addEvent(originalEvent);
    EventId startingIdForEdit = new EventId("Breakfast", start1Original, end1Original);

    Event unrelatedLaterEvent = new Event("Breakfast", start1Original.plusDays(7),
            end1Original.plusDays(7));
    model.addEvent(unrelatedLaterEvent);

    model.editEventsFrom(startingIdForEdit, "subject", "Lunch");

    boolean originalFound = true;
    try {
      model.getEventById(startingIdForEdit);
    } catch (NoSuchElementException e) {
      originalFound = false;
    }
    assertFalse("Original event ID should not exist after subject change.", originalFound);

    EventId newEvent1Id = new EventId("Lunch", start1Original, end1Original);
    Event retrievedEvent1 = model.getEventById(newEvent1Id);
    assertNotNull("Event1 should be retrievable by new ID.", retrievedEvent1);
    assertEquals("Lunch", retrievedEvent1.getSubject());
    assertEquals("Desc1", retrievedEvent1.getDescription());

    EventId unrelatedId = new EventId("Breakfast",
            unrelatedLaterEvent.getStart(), unrelatedLaterEvent.getEnd());
    Event retrievedUnrelated = model.getEventById(unrelatedId);
    assertNotNull("Unrelated event should still exist with original subject.",
            retrievedUnrelated);
    assertEquals("Breakfast", retrievedUnrelated.getSubject());

    model = new CalendarImpl(); // Reset model
    RecurrenceRule rule = new RecurrenceRule(EnumSet.of(DayOfWeek.MONDAY), 2); // Sep 1, 8, 15
    EventSeries actualSeries = new EventSeries("Dinner",
            LocalDateTime.of(2025, 9, 1, 10, 0),
            LocalDateTime.of(2025, 9, 1, 11, 0),
            "Planning", "Conf Room", true, rule);
    model.addEventSeries(actualSeries);

    EventId e1Orig = new EventId("Dinner",
            LocalDateTime.of(2025, 9, 1, 10, 0),
            LocalDateTime.of(2025, 9, 1, 11, 0));
    EventId e2Orig = new EventId("Dinner",
            LocalDateTime.of(2025, 9, 8, 10, 0),
            LocalDateTime.of(2025, 9, 8, 11, 0));
    EventId e3Orig = new EventId("Dinner",
            LocalDateTime.of(2025, 9, 15, 10, 0),
            LocalDateTime.of(2025, 9, 15, 11, 0));

    model.editEventsFrom(e2Orig, "subject", "Breakfast");

    assertEquals("Dinner", model.getEventById(e1Orig).getSubject());

    EventId e2New = new EventId("Breakfast",
            LocalDateTime.of(2025, 9, 8, 10, 0),
            LocalDateTime.of(2025, 9, 8, 11, 0));
    assertEquals("Breakfast", model.getEventById(e2New).getSubject());
    boolean e2OrigFound = true;
    try {
      model.getEventById(e2Orig);
    } catch (NoSuchElementException e) {
      e2OrigFound = false;
    }
    assertFalse("Old ID for event 2 should be gone.", e2OrigFound);

    EventId e3New = new EventId("Breakfast",
            LocalDateTime.of(2025, 9, 15, 10, 0),
            LocalDateTime.of(2025, 9, 15, 11, 0));
    assertEquals("Breakfast", model.getEventById(e3New).getSubject());
    boolean e3OrigFound = true;
    try {
      model.getEventById(e3Orig);
    } catch (NoSuchElementException e) {
      e3OrigFound = false;
    }
    assertFalse("Old ID for event 3 should be gone.", e3OrigFound);
  }


  /**
   * Tests that editEventsFrom throws IllegalArgumentException for unsupported property.
   */
  @Test(expected = IllegalArgumentException.class)
  public void editEventsFromInvalidProperty() {
    Event event1 = new Event("Lunch",
            LocalDateTime.of(2025, 9, 2, 10, 0),
            LocalDateTime.of(2025, 9, 2, 11, 0));
    model.addEvent(event1);
    EventId id = new EventId("Lunch",
            LocalDateTime.of(2025, 9, 2, 10, 0),
            LocalDateTime.of(2025, 9, 2, 11, 0));
    model.editEventsFrom(id, "invalidProperty", "newValue");
  }


  /**
   * Tests editing the description for all events in a series.
   */
  @Test
  public void editSeriesDescription() {
    RecurrenceRule rule = new RecurrenceRule(EnumSet.of(DayOfWeek.MONDAY), 1); // 2 events
    LocalDateTime baseStart =
            LocalDateTime.of(2025, 10, 6, 14, 0); // Monday
    EventSeries series = new EventSeries("Dinner",
            baseStart, baseStart.plusHours(1),
            "Old Desc", "Room A", true, rule);
    model.addEventSeries(series); // Oct 6, Oct 13

    model.editSeries("Dinner", baseStart,
            "description", "Updated Description");

    EventId id1 = new EventId("Dinner", baseStart, baseStart.plusHours(1));
    EventId id2 = new EventId("Dinner",
            baseStart.plusWeeks(1), baseStart.plusWeeks(1).plusHours(1));

    Event updatedS1 = model.getEventById(id1);
    Event updatedS2 = model.getEventById(id2);

    assertNotNull(updatedS1);
    assertEquals("Updated Description", updatedS1.getDescription());
    assertEquals("Dinner", updatedS1.getSubject());
    assertNotNull(updatedS2);
    assertEquals("Updated Description", updatedS2.getDescription());
    assertEquals("Dinner", updatedS2.getSubject());
  }

  /**
   * Tests that editSeries is allowed even if a modified event in the series
   * overlaps with another existing event (different ID).
   */
  @Test
  public void editSeriesOverlappingIsAllowed() {
    LocalDateTime seriesBaseStart =
            LocalDateTime.of(2025, 10, 13, 10, 0); // Monday
    RecurrenceRule rule = new RecurrenceRule(EnumSet.of(DayOfWeek.MONDAY), 1);
    EventSeries seriesToEdit = new EventSeries("Breakfast",
            seriesBaseStart, seriesBaseStart.plusHours(1),
            "Original desc", "loc", true, rule);
    model.addEventSeries(seriesToEdit);

    Event blocker = new Event("Lunch",
            seriesBaseStart.plusMinutes(30),
            seriesBaseStart.plusHours(1).plusMinutes(30));
    model.addEvent(blocker);
    EventId blockerId = new EventId(blocker.getSubject(), blocker.getStart(), blocker.getEnd());

    model.editSeries("Breakfast", seriesBaseStart,
            "description", "New Description");

    EventId seriesId1 = new EventId("Breakfast", seriesBaseStart,
            seriesBaseStart.plusHours(1));
    EventId seriesId2 = new EventId("Breakfast", seriesBaseStart.plusWeeks(1),
            seriesBaseStart.plusWeeks(1).plusHours(1));

    Event s1Upd = model.getEventById(seriesId1);
    Event s2Upd = model.getEventById(seriesId2);

    assertNotNull("Blocker should still exist", model.getEventById(blockerId));
    assertNotNull("Series event 1 should exist", s1Upd);
    assertEquals("New Description", s1Upd.getDescription());
    assertNotNull("Series event 2 should exist", s2Upd);
    assertEquals("New Description", s2Upd.getDescription());

    List<Event> eventsOnFirstDate = model.getEventsOn(seriesBaseStart.toLocalDate());
    assertEquals("Should be 2 events on the first date.", 2,
            eventsOnFirstDate.size());
    assertTrue(eventsOnFirstDate.contains(s1Upd));
    assertTrue(eventsOnFirstDate.contains(blocker));
  }

  /**
   * Tests isBusyAt when multiple events overlap at the queried time.
   */
  @Test
  public void testIsBusyAtWithOverlappingEvents() {
    Event event1 = new Event("Breakfast",
            LocalDateTime.of(2025, 11, 5, 10, 0),
            LocalDateTime.of(2025, 11, 5, 12, 0));
    Event event2 = new Event("Lunch",
            LocalDateTime.of(2025, 11, 5, 11, 0),
            LocalDateTime.of(2025, 11, 5, 13, 0));
    model.addEvent(event1);
    model.addEvent(event2);

    assertTrue(model.isBusyAt(
            LocalDateTime.of(2025, 11, 5, 10, 30))); // Only event1
    assertTrue(model.isBusyAt(
            LocalDateTime.of(2025, 11, 5, 11, 30))); // Overlap
    assertTrue(model.isBusyAt(
            LocalDateTime.of(2025, 11, 5, 12, 30))); // Only event2
  }

  /**
   * Tests getEventsOn when multiple overlapping events exist on that date.
   */
  @Test
  public void testGetEventsOnWithOverlappingEvents() {
    Event event1 = new Event("Lunch",
            LocalDateTime.of(2025, 11, 6, 10, 0),
            LocalDateTime.of(2025, 11, 6, 12, 0));
    Event event2 = new Event("Breakfast",
            LocalDateTime.of(2025, 11, 6, 9, 0),
            LocalDateTime.of(2025, 11, 6, 11, 0));
    Event event3 = new Event("Dinner",
            LocalDateTime.of(2025, 11, 6, 10, 30),
            LocalDateTime.of(2025, 11, 6, 11, 30));
    model.addEvent(event1); // Lunch
    model.addEvent(event2); // Breakfast
    model.addEvent(event3); // Dinner

    List<Event> events = model.getEventsOn(LocalDate.of(2025, 11, 6));
    assertEquals(3, events.size());
    assertEquals(event2, events.get(0)); // Breakfast (09:00)
    assertEquals(event1, events.get(1)); // Lunch (10:00)
    assertEquals(event3, events.get(2)); // Dinner (10:30)
  }

  /**
   * Tests getEventsBetween with various overlapping event scenarios.
   */
  @Test
  public void testGetEventsBetweenWithOverlappingEvents() {
    Event eventA = new Event("Breakfast",
            LocalDateTime.of(2025, 11, 7, 9, 0),
            LocalDateTime.of(2025, 11, 7, 11, 0)); // 9-11
    Event eventB = new Event("Lunch",
            LocalDateTime.of(2025, 11, 7, 10, 0),
            LocalDateTime.of(2025, 11, 7, 12, 0)); // 10-12
    Event eventC = new Event("Dinner",
            LocalDateTime.of(2025, 11, 7, 13, 0),
            LocalDateTime.of(2025, 11, 7, 14, 0)); // 13-14
    Event eventD = new Event("Snack",
            LocalDateTime.of(2025, 11, 7, 8, 0),
            LocalDateTime.of(2025, 11, 7, 9, 30));   // 8-9:30
    model.addEvent(eventA);
    model.addEvent(eventB);
    model.addEvent(eventC);
    model.addEvent(eventD);

    List<Event> events = model.getEventsBetween(
            LocalDateTime.of(2025, 11, 7, 9, 15),
            LocalDateTime.of(2025, 11, 7, 12, 15)
    );
    assertEquals(3, events.size()); // D (Snack), A (Breakfast), B (Lunch)
    List<Event> expected = List.of(eventD, eventA, eventB);
    List<Event> mutableExpected = new ArrayList<>(expected);
    mutableExpected.sort(Comparator.comparing(Event::getStart));

    assertEquals(mutableExpected.get(0), events.get(0)); // D (Snack) at 08:00
    assertEquals(mutableExpected.get(1), events.get(1)); // A (Breakfast) at 09:00
    assertEquals(mutableExpected.get(2), events.get(2)); // B (Lunch) at 10:00

    assertTrue(events.contains(eventA));
    assertTrue(events.contains(eventB));
    assertTrue(events.contains(eventD));
    assertFalse(events.contains(eventC));
  }

  /**
   * Tests adding series where one occurrence duplicates an existing independent event.
   */
  @Test(expected = DuplicateEventException.class)
  public void testAddEventSeriesCreatesDuplicateOfExistingEvent() {
    Event existing = new Event("Breakfast",
            LocalDateTime.of(2025, 12, 8, 8, 0), // Monday
            LocalDateTime.of(2025, 12, 8, 9, 0));
    model.addEvent(existing);

    RecurrenceRule rule = new RecurrenceRule(EnumSet.of(DayOfWeek.MONDAY), 1); // Dec 1, 8
    EventSeries series = new EventSeries("Breakfast", // Same subject
            LocalDateTime.of(2025, 12, 1, 8, 0),
            LocalDateTime.of(2025, 12, 1, 9, 0),
            "Desc", "Loc", true, rule);
    model.addEventSeries(series); // Fails on Dec 8th instance
  }

  /**
   * Tests editing a series property, causing one event to duplicate another existing event.
   */
  @Test(expected = DuplicateEventException.class)
  public void testEditSeriesPropertyChangeResultsInDuplicate() {
    Event blocker = new Event("Lunch",
            LocalDateTime.of(2025, 12, 15, 12, 0), // Monday
            LocalDateTime.of(2025, 12, 15, 13, 0));
    model.addEvent(blocker);

    LocalDateTime seriesStart =
            LocalDateTime.of(2025, 12, 8, 12, 0); // Mon
    RecurrenceRule rule = new RecurrenceRule(EnumSet.of(DayOfWeek.MONDAY), 1); // Dec 8, 15
    EventSeries series = new EventSeries("Dinner", // Different subject initially
            seriesStart, seriesStart.plusHours(1),
            "Desc", "Loc", true, rule);
    model.addEventSeries(series);

    // Edit series subject to "Lunch". Dec 15 instance will duplicate blocker.
    model.editSeries("Dinner", seriesStart, "subject", "Lunch");
  }

  /**
   * Tests editing series events from a point, causing one to duplicate another.
   */
  @Test(expected = DuplicateEventException.class)
  public void testEditEventsFromPropertyChangeResultsInDuplicate() {
    Event preExisting = new Event("Breakfast",
            LocalDateTime.of(2025, 12, 22, 9, 0), // Monday
            LocalDateTime.of(2025, 12, 22, 10, 0));
    model.addEvent(preExisting);

    LocalDateTime seriesStart =
            LocalDateTime.of(2025, 12, 8, 9, 0); // Mon
    RecurrenceRule rule = new RecurrenceRule(EnumSet.of(DayOfWeek.MONDAY), 2); // Dec 8,15,22
    EventSeries series = new EventSeries("Lunch", // Different subject
            seriesStart, seriesStart.plusHours(1), "Desc",
            "Loc", true, rule);
    model.addEventSeries(series);

    EventId startingId = new EventId("Lunch",
            LocalDateTime.of(2025, 12, 15, 9, 0),
            LocalDateTime.of(2025, 12, 15, 10, 0));

    // Edit subject will duplicate preExisting.
    model.editEventsFrom(startingId, "subject", "Breakfast");
  }

  //Tests adding an event that starts before midnight and ends after midnight
  @Test
  public void addOvernightEvent() {
    Event overnight = new Event("Overnight",
            LocalDateTime.of(2025, 7, 4, 23, 30),
            LocalDateTime.of(2025, 7, 5, 1, 0));
    model.addEvent(overnight);


    assertTrue(model.getEventsOn(LocalDate.of(2025, 7,
            4)).contains(overnight));
    assertTrue(model.getEventsOn(LocalDate.of(2025, 7,
            5)).contains(overnight));
  }

  //Tests that creating a zero-duration event throws an exception
  @Test(expected = IllegalArgumentException.class)
  public void addZeroDurationEventThrows() {
    Event invalid = new Event("Zero", LocalDateTime.of(2025, 8, 10,
            12, 0),
            LocalDateTime.of(2025, 8, 10, 12, 0));
    model.addEvent(invalid);
  }


  //Tests that creating a series with start time after end time throws an exception.
  @Test(expected = IllegalArgumentException.class)
  public void addEventSeriesWithEndBeforeStartThrows() {
    RecurrenceRule rule = new RecurrenceRule(EnumSet.of(DayOfWeek.MONDAY), 1);
    new EventSeries("BadSeries", LocalDateTime.of(2025, 7, 7,
            10, 0),
            LocalDateTime.of(2025, 7, 7, 9, 0),
            "desc", "loc", true, rule);
  }


  //Tests isBusyAt returns false when there are events, but not at that time.
  @Test
  public void isBusyAtNoOverlap() {
    Event event = new Event("Event", LocalDateTime.of(2025, 10, 10,
            10, 0),
            LocalDateTime.of(2025, 10, 10, 11, 0));
    model.addEvent(event);
    assertFalse(model.isBusyAt(LocalDateTime.of(2025, 10, 10, 9,
            59)));
    assertFalse(model.isBusyAt(LocalDateTime.of(2025, 10, 10, 11,
            0)));
  }

  //Tests editing an event with the exact same data (no change).
  @Test
  public void editEventWithNoChanges() {
    Event event = new Event("Same", LocalDateTime.of(2025, 11, 11,
            11, 0),
            LocalDateTime.of(2025, 11, 11, 12, 0));
    EventId id = new EventId(event.getSubject(), event.getStart(), event.getEnd());
    model.addEvent(event);
    model.editEvent(id, event);
    assertEquals(event, model.getEventById(id));
  }

  //Tests that adding or editing with null Event or EventId throws NullPointerException.
  @Test(expected = NullPointerException.class)
  public void addEventNullThrows() {
    model.addEvent(null);
  }


  //Tests that adding an event series with zero count throws an exception
  @Test(expected = IllegalArgumentException.class)
  public void addEventSeriesWithZeroCountThrows() {
    RecurrenceRule rule = new RecurrenceRule(EnumSet.of(DayOfWeek.MONDAY), 0);
    new EventSeries("Empty", LocalDateTime.of(2025, 7, 7, 8,
            0),
            LocalDateTime.of(2025, 7, 7, 9, 0),
            "desc", "loc", true, rule);
  }

  //Tests that editing an event with a null EventId throws NoSuchElementException
  @Test(expected = NoSuchElementException.class)
  public void editEventNullIdThrows() {
    Event e = new Event("A", LocalDateTime.of(2025, 9, 9, 9,
            0),
            LocalDateTime.of(2025, 9, 9, 10, 0));
    model.editEvent(null, e);
  }

  //Tests that editing an event with a null Event throws NoSuchElementException
  @Test(expected = NoSuchElementException.class)
  public void editEventNullEventThrows() {
    EventId id = new EventId("A", LocalDateTime.of(2025, 9, 9,
            9, 0),
            LocalDateTime.of(2025, 9, 9, 10, 0));
    model.editEvent(id, null);
  }


}