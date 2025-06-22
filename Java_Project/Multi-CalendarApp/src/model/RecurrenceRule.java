package calendarapp.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Represents a recurrence rule for a series of calendar events.
 *   - repeatDays: set of weekdays on which to repeat
 *   - count: number of occurrences after the first
 *   - until: last date to repeat
 */
public class RecurrenceRule {
  private final Set<DayOfWeek> repeatDays;
  private final Integer count;
  private final LocalDate until;

  /**
   * Count-based constructor.
   *
   * @param repeatDays weekdays to repeat
   * @param count      positive integer number of additional occurrences
   */
  public RecurrenceRule(Set<DayOfWeek> repeatDays, int count) {
    if (repeatDays == null || repeatDays.isEmpty()) {
      throw new IllegalArgumentException("repeatDays must be non-empty");
    }
    if (count <= 0) {
      throw new IllegalArgumentException("Count must be positive");
    }
    this.repeatDays = Set.copyOf(repeatDays);
    this.count = count;
    this.until = null;
  }

  /**
   * Date-based constructor.
   *
   * @param repeatDays weekdays to repeat
   * @param until      final date (inclusive) to generate occurrences
   */
  public RecurrenceRule(Set<DayOfWeek> repeatDays, LocalDate until) {
    if (repeatDays == null || repeatDays.isEmpty()) {
      throw new IllegalArgumentException("repeatDays must be non-empty");
    }
    if (until == null) {
      throw new IllegalArgumentException("Until date must be non-null");
    }
    this.repeatDays = Set.copyOf(repeatDays);
    this.count = null;
    this.until = until;
  }

  /**
   * Generates a list of LocalDateTime occurrences for a series, given the first occurrence's start.
   * The returned list includes the first occurrence (firstStart) itself, then subsequent dates at
   * the same LocalTime up to count or until date.
   *
   * @param firstStart first event's LocalDateTime
   * @return list of LocalDateTime for each occurrence
   */
  public List<LocalDateTime> generateOccurrences(LocalDateTime firstStart) {
    if (firstStart == null) {
      throw new IllegalArgumentException("firstStart must be non-null");
    }
    LocalDate firstDate = firstStart.toLocalDate();
    List<LocalDateTime> occurrences = new ArrayList<>();

    occurrences.add(firstStart);


    RecurrenceIterator iter = new RecurrenceIterator(firstDate);
    while (iter.hasNext()) {
      LocalDate nextDate = iter.next();
      occurrences.add(LocalDateTime.of(nextDate, firstStart.toLocalTime()));
    }
    return occurrences;
  }

  /**
   * Returns an iterator over LocalDate values for subsequent occurrences.
   *
   * @param firstDate date of the first occurrence
   * @return Iterator over subsequent LocalDate occurrences
   */
  public Iterator<LocalDate> iterator(LocalDate firstDate) {
    return new RecurrenceIterator(firstDate);
  }

  private class RecurrenceIterator implements Iterator<LocalDate> {
    private final List<LocalDate> generatedDates;
    private int index = 0;

    RecurrenceIterator(LocalDate startDate) {
      generatedDates = new ArrayList<>();
      if (count != null) {

        int generated = 0;
        LocalDate current = startDate;
        while (generated < count) {
          current = current.plusDays(1);
          if (repeatDays.contains(current.getDayOfWeek())) {
            generatedDates.add(current);
            generated++;
          }
        }
      } else {

        LocalDate current = startDate.plusDays(1);
        while (!current.isAfter(until)) {
          if (repeatDays.contains(current.getDayOfWeek())) {
            generatedDates.add(current);
          }
          current = current.plusDays(1);
        }
      }
    }

    @Override
    public boolean hasNext() {
      return index < generatedDates.size();
    }

    @Override
    public LocalDate next() {
      if (!hasNext()) {
        throw new NoSuchElementException("No more recurrence dates");
      }
      return generatedDates.get(index++);
    }
  }
}
