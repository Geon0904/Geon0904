package model;


import calendarapp.model.CalendarImpl;
import calendarapp.model.CalendarManagerImpl;
import calendarapp.model.CalendarModel;
import calendarapp.model.Event;
import calendarapp.model.EventSeries;
import calendarapp.model.RecurrenceRule;

import org.junit.Before;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the CalendarManagerImpl class.
 * Verifies correct functionality and all edge cases for calendar creation,
 * event/series copying, date range logic, duplicate handling, and error propagation.
 */
public class CalendarManagerImplTest {
  private CalendarManagerImpl mgr;
  private final LocalDate srcDate = LocalDate.of(2025, 7, 10);
  private final LocalDate dstDate = LocalDate.of(2025, 8, 10);


  private static Set<DayOfWeek> parseWeekdays(String pattern) {
    Set<DayOfWeek> set = new HashSet<>();
    for (char c : pattern.toCharArray()) {
      switch (c) {
        case 'M':
          set.add(DayOfWeek.MONDAY);
          break;
        case 'T':
          set.add(DayOfWeek.TUESDAY);
          break;
        case 'W':
          set.add(DayOfWeek.WEDNESDAY);
          break;
        case 'R':
          set.add(DayOfWeek.THURSDAY);
          break;
        case 'F':
          set.add(DayOfWeek.FRIDAY);
          break;
        case 'S':
          set.add(DayOfWeek.SATURDAY);
          break;
        case 'U':
          set.add(DayOfWeek.SUNDAY);
          break;
        default:
          throw new IllegalArgumentException("Unknown weekday code: " + c);
      }
    }
    return set;
  }

  @Before
  public void setup() {
    mgr = new CalendarManagerImpl();
    mgr.createCalendar("Src", ZoneId.of("UTC"));
    mgr.createCalendar("Dst", ZoneId.of("America/New_York"));
    mgr.useCalendar("Src");
  }

  /**
   * Verifies that a newly‑created calendar stores the correct name and timezone.
   */
  @Test
  public void createCalendarStoresNameAndTimezone() {
    mgr.createCalendar("Work", ZoneId.of("UTC"));
    mgr.useCalendar("Work");
    CalendarModel cal = mgr.getActiveCalendar();
    assertEquals("Work", mgr.getActiveCalendarName());
    assertEquals(ZoneId.of("UTC"), ((CalendarImpl) cal).getTimezone());
  }



  /**
   * Confirms that renaming a calendar makes the new name usable.
   */
  @Test
  public void renameCalendarChangesName() {
    mgr.createCalendar("School", ZoneId.of("UTC"));
    mgr.editCalendar("School", "name", "Personal");
    mgr.useCalendar("Personal");
    assertEquals("Personal", mgr.getActiveCalendarName());
  }


  // old name lookup should fail after rename
  @Test(expected = NoSuchElementException.class)
  public void oldNameLookupFailsAfterRename() {
    mgr.createCalendar("School", ZoneId.of("UTC"));
    mgr.editCalendar("School", "name", "Personal");
    mgr.useCalendar("School"); // should throw
  }

  /**
   * Checks that editing a calendar’s timezone correctly updates the stored ZoneId.
   */
  @Test
  public void editCalendarUpdatesTimezone() {
    mgr.createCalendar("Trip", ZoneId.of("UTC"));
    mgr.editCalendar("Trip", "timezone", "America/New_York");
    mgr.useCalendar("Trip");
    assertEquals(ZoneId.of("America/New_York"), ((CalendarImpl)
            mgr.getActiveCalendar()).getTimezone());
  }

  // Copies single event successfully.
  @Test
  public void copySingleEventSuccess() {
    Event e = new Event("Math", srcDate.atTime(9, 0),
            srcDate.atTime(10, 0));
    mgr.addEvent(e);
    int result = mgr.copyEvent("Math", srcDate.atTime(9, 0), "Dst",
            dstDate.atTime(9, 0));
    assertEquals(1, result);

    mgr.useCalendar("Dst");
    assertEquals(1, mgr.getActiveCalendar().getEventsOn(dstDate).size());
  }

  // Throws if event not found in source
  @Test(expected = NoSuchElementException.class)
  public void copyEventThrowsIfNotFound() {
    mgr.copyEvent("Unknown", srcDate.atTime(12, 0), "Dst",
            dstDate.atTime(12, 0));
  }

  // Throws if target calendar does not exist
  @Test(expected = NoSuchElementException.class)
  public void copyEventThrowsIfTargetMissing() {
    mgr.copyEvent("X", srcDate.atTime(9, 0), "NoSuch",
            dstDate.atTime(9, 0));
  }

  // Copies a series successfully, preserves all attributes.
  @Test
  public void copySeriesEventSuccess() {
    RecurrenceRule rule = new RecurrenceRule(parseWeekdays("MWF"), 3);
    EventSeries s = new EventSeries("Run", srcDate.atTime(6, 0),
            srcDate.atTime(7, 0),
            "Desc", "Track", true, rule);
    mgr.addEventSeries(s);
    int copied = mgr.copyEvent("Run", srcDate.atTime(6, 0), "Dst",
            dstDate.atTime(6, 0));
    assertEquals(4, copied);
    mgr.useCalendar("Dst");
    assertEquals(1, mgr.getActiveCalendar().getEventsOn(dstDate).size());
    Event e = mgr.getActiveCalendar().getEventsOn(dstDate).get(0);
    assertEquals("Desc", e.getDescription());
    assertEquals("Track", e.getLocation());
    assertTrue(e.isPublic());

  }

  // Does not copy series if already present at dest start time
  @Test
  public void copySeriesAlreadyExistsNoDup() {
    RecurrenceRule rule = new RecurrenceRule(parseWeekdays("MWF"), 2);
    EventSeries s = new EventSeries("Run", srcDate.atTime(6, 0),
            srcDate.atTime(7, 0),
            "D", "L", false, rule);
    mgr.addEventSeries(s);
    mgr.copyEvent("Run", srcDate.atTime(6, 0), "Dst",
            dstDate.atTime(6, 0));
    // Second copy, should do nothing
    int again = mgr.copyEvent("Run", srcDate.atTime(6, 0), "Dst",
            dstDate.atTime(6, 0));
    assertEquals(0, again);
  }

  // Copies all events on a given date
  @Test
  public void copyEventsOnDateMixed() {
    mgr.addEvent(new Event("Solo", srcDate.atTime(8, 0),
            srcDate.atTime(8, 30)));
    RecurrenceRule rule = new RecurrenceRule(parseWeekdays("M"), 2);
    EventSeries s = new EventSeries("Seq", srcDate.atTime(9, 0),
            srcDate.atTime(10, 0), "", "", true, rule);
    mgr.addEventSeries(s);
    int total = mgr.copyEventsOn(srcDate, "Dst", dstDate);
    assertEquals(4, total);
    mgr.useCalendar("Dst");
    assertEquals(2, mgr.getActiveCalendar().getEventsOn(dstDate).size());
  }

  // copyEventsOn does nothing if no events on that date
  @Test
  public void copyEventsOnNoEvents() {
    int res = mgr.copyEventsOn(LocalDate.of(2099, 1, 1),
            "Dst", dstDate);
    assertEquals(0, res);
  }

  // copyEventsOn throws if target missing
  @Test(expected = NoSuchElementException.class)
  public void copyEventsOnThrowsIfNoTarget() {
    mgr.copyEventsOn(srcDate, "NoDst", dstDate);
  }

  // Copy range: copies all events over multiple days with correct date shifting
  @Test
  public void copyEventsBetweenRangeNormal() {
    mgr.addEvent(new Event("A", srcDate.atTime(10, 0),
            srcDate.atTime(11, 0)));
    mgr.addEvent(new Event("B", srcDate.plusDays(1).atTime(12, 0),
            srcDate.plusDays(1).atTime(13, 0)));
    int total = mgr.copyEventsBetween(srcDate, srcDate.plusDays(1),
            "Dst", dstDate);
    assertEquals(2, total);
    mgr.useCalendar("Dst");
    assertEquals(1, mgr.getActiveCalendar().getEventsOn(dstDate).size());
    assertEquals(1,
            mgr.getActiveCalendar().getEventsOn(dstDate.plusDays(1)).size());
  }

  // copyEventsBetween returns 0 if no events in range
  @Test
  public void copyEventsBetweenNoEvents() {
    int total = mgr.copyEventsBetween(LocalDate.of(2090, 1, 1),
            LocalDate.of(2090, 1, 2),
            "Dst", LocalDate.of(2099, 1, 1));
    assertEquals(0, total);
  }

  // copyEventsBetween throws if target missing
  @Test(expected = NoSuchElementException.class)
  public void copyEventsBetweenThrowsIfNoTarget() {
    mgr.copyEventsBetween(srcDate, srcDate.plusDays(1), "Missing", dstDate);
  }

  // copyEventsBetween does nothing if range inverted
  @Test
  public void copyEventsBetweenInvertedRange() {
    mgr.addEvent(new Event("E", srcDate.atTime(9, 0),
            srcDate.atTime(10, 0)));
    int total = mgr.copyEventsBetween(srcDate.plusDays(2),
            srcDate, "Dst", dstDate);
    assertEquals(0, total);
  }

  // copyEventsOn same day as src and dest
  @Test
  public void copyEventsOnSameDay() {
    mgr.addEvent(new Event("S", srcDate.atTime(5, 0),
            srcDate.atTime(6, 0)));
    int total = mgr.copyEventsOn(srcDate, "Dst", srcDate);
    assertEquals(1, total);
    mgr.useCalendar("Dst");
    assertEquals(1, mgr.getActiveCalendar().getEventsOn(srcDate).size());
  }

  // Throws if null target calendar for any method
  @Test(expected = NoSuchElementException.class)
  public void copyEventsOnNullTargetThrows() {
    mgr.copyEventsOn(srcDate, null, dstDate);
  }

  // Throws if target calendar is not created yet for copyEventsBetween
  @Test(expected = NoSuchElementException.class)
  public void copyEventsBetweenNullTargetThrows() {
    mgr.copyEventsBetween(srcDate, srcDate, null, dstDate);
  }

  // copyEvent throws if src event not found on that date
  @Test(expected = NoSuchElementException.class)
  public void copyEventSrcEventNotFound() {
    mgr.copyEvent("NotExist", srcDate.atTime(6, 0), "Dst",
            dstDate.atTime(6, 0));
  }

  // copyEvent with a null target calendar name
  @Test(expected = NoSuchElementException.class)
  public void copyEventNullTargetCalendar() {
    Event e = new Event("N", srcDate.atTime(7, 0),
            srcDate.atTime(8, 0));
    mgr.addEvent(e);
    mgr.copyEvent("N", srcDate.atTime(7, 0), null,
            dstDate.atTime(7, 0));
  }

  // copyEventsBetween handles duplicate event in destination gracefully
  @Test
  public void copyEventsBetweenDuplicateInDest() {
    Event e = new Event("X", srcDate.atTime(10, 0),
            srcDate.atTime(11, 0));
    mgr.addEvent(e);
    // First copy
    mgr.copyEventsBetween(srcDate, srcDate, "Dst", dstDate);
    // Duplicate: Should not throw, should return 0 (already exists)
    int again = mgr.copyEventsBetween(srcDate, srcDate, "Dst", dstDate);
    assertEquals(0, again);
  }

  // copyEventsOn does not double-copy the same series if multiple events on same date
  @Test
  public void copyEventsOnNoDoubleSeriesCopy() {
    RecurrenceRule rule = new RecurrenceRule(parseWeekdays("MTWRF"), 5);
    EventSeries s = new EventSeries("School", srcDate.atTime(8, 0),
            srcDate.atTime(9, 0), "", "", true, rule);
    mgr.addEventSeries(s);
    int first = mgr.copyEventsOn(srcDate, "Dst", dstDate);
    int second = mgr.copyEventsOn(srcDate, "Dst", dstDate);
    assertEquals(6, first);
    assertEquals(0, second);
  }

  /**
   * Copies a single event between calendars that share the same timezone.
   */
  @Test
  public void copySingleEventSameTimezone() {
    // destination calendar in the same ZoneId as the source calendar
    mgr.createCalendar("Mirror", ZoneId.of("UTC"));

    // event lives on the source calendar
    Event physics = new Event("Physics", srcDate.atTime(14, 0),
            srcDate.atTime(15, 0));
    mgr.addEvent(physics);

    // copy and verify the return value
    int copied = mgr.copyEvent("Physics", srcDate.atTime(14, 0),
            "Mirror", dstDate.atTime(14, 0));
    assertEquals(1, copied);

    // confirm it now exists on the destination calendar at the shifted date
    mgr.useCalendar("Mirror");
    assertEquals(1, mgr.getActiveCalendar()
            .getEventsOn(dstDate).size());
  }

  /**
   * Copies several events in a date range between calendars that share the same timezone.
   */
  @Test
  public void copyEventsBetweenRangeSameTimezone() {
    mgr.createCalendar("Mirror", ZoneId.of("UTC"));

    // two events on consecutive days in the source calendar
    mgr.addEvent(new Event("X", srcDate.atTime(10, 0),
            srcDate.atTime(11, 0)));
    mgr.addEvent(new Event("Y", srcDate.plusDays(1).atTime(12, 0),
            srcDate.plusDays(1).atTime(13, 0)));

    // copy the range and verify the count
    int total = mgr.copyEventsBetween(srcDate, srcDate.plusDays(1),
            "Mirror", dstDate);
    assertEquals(2, total);

    // each event should appear on the corresponding shifted day
    mgr.useCalendar("Mirror");
    assertEquals(1, mgr.getActiveCalendar()
            .getEventsOn(dstDate).size());
    assertEquals(1, mgr.getActiveCalendar()
            .getEventsOn(dstDate.plusDays(1)).size());
  }
}
