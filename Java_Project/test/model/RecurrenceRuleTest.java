package model;

import calendarapp.model.RecurrenceRule;

import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the RecurrenceRule class.
 */
public class RecurrenceRuleTest {

  // Constructor Tests

  /**
   * Tests that the count constructor throws IllegalArgumentException for null repeat days.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithCountNullDays() {
    new RecurrenceRule(null, 5);
  }

  /**
   * Tests that the count constructor throws IllegalArgumentException for empty repeat days.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithCountEmptyDays() {
    new RecurrenceRule(EnumSet.noneOf(DayOfWeek.class), 5);
  }

  /**
   * Tests that the count constructor throws IllegalArgumentException for zero occurrences.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithCountZeroOccurrences() {
    new RecurrenceRule(EnumSet.of(DayOfWeek.TUESDAY), 0);
  }

  /**
   * Tests that the count constructor throws IllegalArgumentException for negative occurrences.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithCountNegativeOccurrences() {
    new RecurrenceRule(EnumSet.of(DayOfWeek.TUESDAY), -1);
  }

  /**
   * Tests that the until date constructor throws IllegalArgumentException for null repeat days.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithUntilDateNullDays() {
    new RecurrenceRule(null, LocalDate.of(2025, 1, 1));
  }

  /**
   * Tests that the until date constructor throws IllegalArgumentException for empty repeat days.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithUntilDateEmptyDays() {
    new RecurrenceRule(EnumSet.noneOf(DayOfWeek.class),
            LocalDate.of(2025, 1, 1));
  }

  /**
   * Tests that the until date constructor throws IllegalArgumentException for a null until date.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithUntilDateNullDate() {
    new RecurrenceRule(EnumSet.of(DayOfWeek.THURSDAY), null);
  }

  /**
   * Tests that generateOccurrences throws IllegalArgumentException for null firstStart.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testGenerateOccurrencesNullFirstStart() {
    Set<DayOfWeek> days = EnumSet.of(DayOfWeek.MONDAY);
    RecurrenceRule rule = new RecurrenceRule(days, 3);
    rule.generateOccurrences(null);
  }

  /**
   * Tests generating occurrences for count based.
   */
  @Test
  public void testGenerateOccurrencesCountBased() {
    Set<DayOfWeek> days = EnumSet.of(DayOfWeek.WEDNESDAY);
    int additionalOccurrences = 2; // This is the 'count' parameter for the constructor
    RecurrenceRule rule = new RecurrenceRule(days, additionalOccurrences);
    LocalDateTime firstStartDateTime =
            LocalDateTime.of(2025, 6, 4, 10, 0);

    List<LocalDateTime> occurrences = rule.generateOccurrences(firstStartDateTime);

    assertEquals("1 + " + additionalOccurrences + " additional occurrences.",
            1 + additionalOccurrences, occurrences.size());

    // First occurrence is always firstStartDateTime
    assertEquals(firstStartDateTime, occurrences.get(0));
    assertEquals(LocalTime.of(10, 0), occurrences.get(0).toLocalTime());

    // Subsequent occurrences should be on Wednesdays
    assertEquals(LocalDateTime.of(2025, 6, 11, 10, 0),
            occurrences.get(1));
    assertEquals(DayOfWeek.WEDNESDAY, occurrences.get(1).getDayOfWeek());
    assertEquals(LocalTime.of(10, 0), occurrences.get(1).toLocalTime());

    assertEquals(LocalDateTime.of(2025, 6, 18, 10, 0),
            occurrences.get(2));
    assertEquals(DayOfWeek.WEDNESDAY, occurrences.get(2).getDayOfWeek());
    assertEquals(LocalTime.of(10, 0), occurrences.get(2).toLocalTime());
  }

  /**
   * Tests generating occurrences when firstStart's DayOfWeek is not in repeatDays.
   */
  @Test
  public void testGenerateOccurrencesCountBasedFirstStartNotOnRepeatDay() {
    Set<DayOfWeek> days = EnumSet.of(DayOfWeek.FRIDAY);
    int additionalOccurrences = 2;
    RecurrenceRule rule = new RecurrenceRule(days, additionalOccurrences);
    LocalDateTime firstStartDateTime =
            LocalDateTime.of(2025, 6, 4, 12, 0);

    List<LocalDateTime> occurrences = rule.generateOccurrences(firstStartDateTime);

    assertEquals(1 + additionalOccurrences, occurrences.size());
    assertEquals(firstStartDateTime, occurrences.get(0));

    // Next ones should be Fridays
    assertEquals(LocalDateTime.of(2025, 6, 6, 12, 0),
            occurrences.get(1)); // First Friday
    assertEquals(DayOfWeek.FRIDAY, occurrences.get(1).getDayOfWeek());
    assertEquals(LocalTime.of(12, 0), occurrences.get(1).toLocalTime());

    assertEquals(LocalDateTime.of(2025, 6, 13, 12, 0),
            occurrences.get(2)); // Second Friday
    assertEquals(DayOfWeek.FRIDAY, occurrences.get(2).getDayOfWeek());
    assertEquals(LocalTime.of(12, 0), occurrences.get(2).toLocalTime());
  }

  /**
   * Tests generating occurrences for an until date rule.
   */
  @Test
  public void testGenerateOccurrencesUntilDateBased() {
    Set<DayOfWeek> days = EnumSet.of(DayOfWeek.THURSDAY);
    LocalDate untilDate = LocalDate.of(2025, 6, 19);
    RecurrenceRule rule = new RecurrenceRule(days, untilDate);
    LocalDateTime firstStartDateTime =
            LocalDateTime.of(2025, 6, 5, 15, 0);

    List<LocalDateTime> occurrences = rule.generateOccurrences(firstStartDateTime);

    assertEquals(3, occurrences.size());
    assertEquals(firstStartDateTime, occurrences.get(0));
    assertEquals(DayOfWeek.THURSDAY, occurrences.get(0).getDayOfWeek());
    assertEquals(LocalTime.of(15, 0), occurrences.get(0).toLocalTime());

    assertEquals(LocalDateTime.of(2025, 6, 12, 15, 0),
            occurrences.get(1));
    assertEquals(DayOfWeek.THURSDAY, occurrences.get(1).getDayOfWeek());
    assertEquals(LocalTime.of(15, 0), occurrences.get(1).toLocalTime());

    assertEquals(LocalDateTime.of(2025, 6, 19, 15, 0),
            occurrences.get(2)); // Includes untilDate
    assertEquals(DayOfWeek.THURSDAY, occurrences.get(2).getDayOfWeek());
    assertEquals(LocalTime.of(15, 0), occurrences.get(2).toLocalTime());
  }

  /**
   * Tests generating occurrences when firstStart's DayOfWeek is not in repeatDays (until).
   */
  @Test
  public void testGenerateOccurrencesUntilDateBasedFirstStartNotOnRepeatDay() {
    Set<DayOfWeek> days = EnumSet.of(DayOfWeek.FRIDAY);
    LocalDate untilDate = LocalDate.of(2025, 6, 13);
    RecurrenceRule rule = new RecurrenceRule(days, untilDate);
    LocalDateTime firstStartDateTime =
            LocalDateTime.of(2025, 6, 4, 10, 0); //W

    List<LocalDateTime> occurrences = rule.generateOccurrences(firstStartDateTime);

    // Expected: 2025-06-04 (W), 2025-06-06 (F), 2025-06-13 (F)
    assertEquals(3, occurrences.size());
    assertEquals(firstStartDateTime, occurrences.get(0));
    assertEquals(LocalTime.of(10, 0), occurrences.get(0).toLocalTime());

    assertEquals(LocalDateTime.of(2025, 6, 6, 10, 0),
            occurrences.get(1)); // First Friday
    assertEquals(DayOfWeek.FRIDAY, occurrences.get(1).getDayOfWeek());
    assertEquals(LocalTime.of(10, 0), occurrences.get(1).toLocalTime());

    assertEquals(LocalDateTime.of(2025, 6, 13, 10, 0),
            occurrences.get(2)); // Second Friday (also untilDate)
    assertEquals(DayOfWeek.FRIDAY, occurrences.get(2).getDayOfWeek());
    assertEquals(LocalTime.of(10, 0), occurrences.get(2).toLocalTime());
  }


  /**
   * Tests that only firstStart is generated if the start date is after the until date.
   */
  @Test
  public void testGenerateOccurrencesUntilDateBasedStartAfterUntil() {
    Set<DayOfWeek> days = EnumSet.of(DayOfWeek.MONDAY);
    LocalDate untilDate = LocalDate.of(2025, 5, 31);
    RecurrenceRule rule = new RecurrenceRule(days, untilDate);
    LocalDateTime firstStartDateTime =
            LocalDateTime.of(2025, 6, 1, 10, 0); // after until

    List<LocalDateTime> occurrences = rule.generateOccurrences(firstStartDateTime);
    assertEquals("Only firstStart should be present "
            + "if it's after until date for recurrences.", 1, occurrences.size());
    assertEquals(firstStartDateTime, occurrences.get(0));
  }

  /**
   * Tests generating occurrences when the until date is the same as the firstStart's date
   * and firstStart matches a repeat day.
   */
  @Test
  public void testGenerateOccurrencesUntilDateIsSameAsFirstStartDateAndMatchesDay() {
    Set<DayOfWeek> days = EnumSet.of(DayOfWeek.SATURDAY);
    LocalDateTime firstStartDateTime =
            LocalDateTime.of(2025, 6, 7, 11, 0); // Saturday
    LocalDate untilDate = firstStartDateTime.toLocalDate();
    RecurrenceRule rule = new RecurrenceRule(days, untilDate);

    List<LocalDateTime> occurrences = rule.generateOccurrences(firstStartDateTime);
    assertEquals(1, occurrences.size());
    assertEquals(firstStartDateTime, occurrences.get(0));
  }

  /**
   * Tests generating occurrences when until date is same as firstStart's date
   * but firstStart does not match a repeat day.
   */
  @Test
  public void testGenerateOccurrencesUntilDateIsSameAsFirstStartDateDoesNotMatchDay() {
    Set<DayOfWeek> days = EnumSet.of(DayOfWeek.SUNDAY);
    LocalDateTime firstStartDateTime =
            LocalDateTime.of(2025, 6, 7, 11, 0); // Saturday
    LocalDate untilDate = firstStartDateTime.toLocalDate();
    RecurrenceRule rule = new RecurrenceRule(days, untilDate);

    List<LocalDateTime> occurrences = rule.generateOccurrences(firstStartDateTime);
    assertEquals(1, occurrences.size());
    assertEquals(firstStartDateTime, occurrences.get(0));
  }

  /**
   * Tests that only firstStart is generated if the until date is before the firstStart's date.
   */
  @Test
  public void testGenerateOccurrencesUntilDateIsBeforeFirstStartDate() {
    Set<DayOfWeek> days = EnumSet.of(DayOfWeek.MONDAY);
    LocalDateTime firstStartDateTime =
            LocalDateTime.of(2025, 6, 9, 10, 0); // Monday
    LocalDate untilDate = LocalDate.of(2025, 6, 2); // monday, but before
    RecurrenceRule rule = new RecurrenceRule(days, untilDate);

    List<LocalDateTime> occurrences = rule.generateOccurrences(firstStartDateTime);
    assertEquals(1, occurrences.size());
    assertEquals(firstStartDateTime, occurrences.get(0));
  }

  /**
   * Tests generating occurrences for a count-based rule with multiple repeat days per week.
   */
  @Test
  public void testGenerateOccurrencesMultipleDaysOfWeekCountBased() {
    Set<DayOfWeek> days = EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY);
    int additionalOccurrences = 3;
    RecurrenceRule rule = new RecurrenceRule(days, additionalOccurrences);
    LocalDateTime firstStartDateTime =
            LocalDateTime.of(2025, 6, 1, 9, 0); // Sunday

    List<LocalDateTime> occurrences = rule.generateOccurrences(firstStartDateTime);

    assertEquals(1 + additionalOccurrences, occurrences.size());
    assertEquals(firstStartDateTime, occurrences.get(0)); // Sun 2025-06-01 09:00
    assertEquals(LocalDateTime.of(2025, 6, 3, 9, 0),
            occurrences.get(1));  // Tue
    assertEquals(LocalDateTime.of(2025, 6, 5, 9, 0),
            occurrences.get(2));  // Thu
    assertEquals(LocalDateTime.of(2025, 6, 10, 9, 0),
            occurrences.get(3)); // Tue
  }

  /**
   * Tests generating occurrences for an until date rule with multiple repeat days per week.
   */
  @Test
  public void testGenerateOccurrencesMultipleDaysOfWeekUntilDateBased() {
    Set<DayOfWeek> days = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
    LocalDate untilDate = LocalDate.of(2025, 6, 13); // Friday
    RecurrenceRule rule = new RecurrenceRule(days, untilDate);
    LocalDateTime firstStartDateTime =
            LocalDateTime.of(2025, 6, 2, 17, 0); // Monday

    List<LocalDateTime> occurrences = rule.generateOccurrences(firstStartDateTime);

    // Expected: 06-02 (Mon, start), 06-06 (Fri), 06-09 (Mon), 06-13 (Fri)
    assertEquals(4, occurrences.size());
    assertEquals(firstStartDateTime, occurrences.get(0)); // Mon
    assertEquals(LocalDateTime.of(2025, 6, 6, 17, 0),
            occurrences.get(1));  // Fri
    assertEquals(LocalDateTime.of(2025, 6, 9, 17, 0),
            occurrences.get(2));  // Mon
    assertEquals(LocalDateTime.of(2025, 6, 13, 17, 0),
            occurrences.get(3)); // Fri
  }

  // Iterator Tests

  /**
   * Tests the basic functionality of the iterator for a count based rule.
   */
  @Test
  public void testIteratorCountBased() {
    Set<DayOfWeek> days = EnumSet.of(DayOfWeek.FRIDAY);
    int count = 2;
    RecurrenceRule rule = new RecurrenceRule(days, count);
    LocalDate startDate = LocalDate.of(2025, 6, 1); // Sunday

    Iterator<LocalDate> iter = rule.iterator(startDate);
    assertTrue(iter.hasNext());
    assertEquals(LocalDate.of(2025, 6, 6), iter.next()); // First
    assertTrue(iter.hasNext());
    assertEquals(LocalDate.of(2025, 6, 13), iter.next());// Second
    assertFalse(iter.hasNext());

    boolean exceptionThrown = false;
    try {
      iter.next();
    } catch (NoSuchElementException e) {
      exceptionThrown = true;
    }
    assertTrue("Expected NoSuchElementException to be thrown by iter.next() when exhausted.",
            exceptionThrown);
  }

  /**
   * Tests the iterator for an until date rule.
   */
  @Test
  public void testIteratorUntilDateBased() {
    Set<DayOfWeek> days = EnumSet.of(DayOfWeek.MONDAY);
    LocalDate until = LocalDate.of(2025, 6, 10); // Tuesday
    RecurrenceRule rule = new RecurrenceRule(days, until);
    LocalDate startDate = LocalDate.of(2025, 6, 1); // Sunday

    Iterator<LocalDate> iter = rule.iterator(startDate);
    assertTrue(iter.hasNext());
    assertEquals(LocalDate.of(2025, 6, 2), iter.next()); // Monday
    assertTrue(iter.hasNext());
    assertEquals(LocalDate.of(2025, 6, 9), iter.next()); // Monday
    assertFalse(iter.hasNext()); // Next Monday is after until date

    boolean exceptionThrown = false;
    try {
      iter.next();
    } catch (NoSuchElementException e) {
      exceptionThrown = true;
    }
    assertTrue("Expected NoSuchElementException to be thrown by iter.next() when exhausted.",
            exceptionThrown);
  }

  /**
   * Tests iterator when no occurrences should be generated by the iterator.
   */
  @Test
  public void testIteratorNoAdditionalOccurrencesForCount() {
    Set<DayOfWeek> days = EnumSet.of(DayOfWeek.MONDAY);
    // RecurrenceRule with count=1 means one additional occurrence via iterator.
    RecurrenceRule rule = new RecurrenceRule(days, 1);
    LocalDate startDate = LocalDate.of(2025, 6, 1); // Sunday
    Iterator<LocalDate> iter = rule.iterator(startDate);
    assertTrue(iter.hasNext());
    assertEquals(LocalDate.of(2025, 6, 2), iter.next()); // One additional
    assertFalse(iter.hasNext());
  }

  /**
   * Tests iterator when until date is before any possible next occurrence.
   */
  @Test
  public void testIteratorNoOccurrencesForUntil() {
    Set<DayOfWeek> days = EnumSet.of(DayOfWeek.MONDAY);
    LocalDate until = LocalDate.of(2025, 6, 1); // Sunday
    RecurrenceRule rule = new RecurrenceRule(days, until);
    LocalDate startDate = LocalDate.of(2025, 6, 1); // Sunday
    Iterator<LocalDate> iter = rule.iterator(startDate);
    assertFalse(iter.hasNext());
  }
}