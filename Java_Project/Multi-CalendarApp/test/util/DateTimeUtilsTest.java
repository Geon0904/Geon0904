package calendarapp.util;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


/**
 * Unit tests for the DateTimeUtils class.
 */
public class DateTimeUtilsTest {

  //Parses a valid ISO date time string without seconds
  @Test
  public void testParseDateTimeValidMinutePrecision() {
    LocalDateTime dt = DateTimeUtils.parseDateTime("2025-07-04T09:00");
    assertEquals(LocalDateTime.of(2025, 7, 4, 9, 0), dt);
  }

  //Parses a valid ISO date time string including seconds
  @Test
  public void testParseDateTimeValidWithSeconds() {
    LocalDateTime dt = DateTimeUtils.parseDateTime("2025-07-04T09:00:15");
    assertEquals(LocalDateTime.of(2025, 7, 4, 9, 0, 15),
            dt);
  }

  //Rejects a date-time string missing the 'T' separator
  @Test(expected = IllegalArgumentException.class)
  public void testParseDateTimeInvalidMissingT() {
    DateTimeUtils.parseDateTime("2025-07-04 09:00");
  }

  //Returns the correct error message for a completely wrong date-time format
  @Test
  public void testParseDateTimeInvalidFormatMessage() {
    try {
      DateTimeUtils.parseDateTime("07/04/2025 09:00");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals("Invalid datetime format. Use 'YYYY-MM-DDTHH:MM'.", e.getMessage());
    }
  }

  //Rejects a date-time string with an invalid month (13)
  @Test(expected = IllegalArgumentException.class)
  public void testParseDateTimeInvalidMonth() {
    DateTimeUtils.parseDateTime("2025-13-01T09:00");
  }

  //Throws NullPointerException when input is null
  @Test(expected = NullPointerException.class)
  public void testParseDateTimeNullInput() {
    DateTimeUtils.parseDateTime(null);
  }

  //Parses a valid ISO date string, including leap day in a leap year
  @Test
  public void testParseDateValidIso() {
    LocalDate d = DateTimeUtils.parseDate("2024-02-29");
    assertEquals(LocalDate.of(2024, 2, 29), d);
  }

  //Rejects a date string in non-ISO format
  @Test(expected = IllegalArgumentException.class)
  public void testParseDateInvalidFormat() {
    DateTimeUtils.parseDate("02-29-2024");
  }

  //Rejects a non-existent leap day (2023 is not a leap year)
  @Test(expected = IllegalArgumentException.class)
  public void testParseDateInvalidLeapDay() {
    DateTimeUtils.parseDate("2023-02-29");
  }

  //Throws NullPointerException when date input is null
  @Test(expected = NullPointerException.class)
  public void testParseDateNullInput() {
    DateTimeUtils.parseDate(null);
  }

  //Formats a LocalDateTime including seconds correctly
  @Test
  public void testFormatDateTime() {
    LocalDateTime dt = LocalDateTime.of(2025, 7, 4, 9, 0,
            15);
    String formatted = DateTimeUtils.formatDateTime(dt);
    assertEquals("2025-07-04T09:00:15", formatted);
  }

  //Formats a LocalDate correctly to ISO format
  @Test
  public void testFormatDate() {
    LocalDate d = LocalDate.of(2025, 12, 31);
    String formatted = DateTimeUtils.formatDate(d);
    assertEquals("2025-12-31", formatted);
  }


  @Test
  public void testParseDateTimeEndOfDay() {
    LocalDateTime dt = DateTimeUtils.parseDateTime("2025-12-31T23:59:59");
    assertEquals(LocalDateTime.of(2025, 12, 31, 23, 59,
            59), dt);
  }


  @Test
  public void testParseDateTimeStartOfDay() {
    LocalDateTime dt = DateTimeUtils.parseDateTime("2025-01-01T00:00:00");
    assertEquals(LocalDateTime.of(2025, 1, 1, 0, 0,
            0), dt);
  }


  @Test
  public void testFormatLeapDay() {
    LocalDate d = LocalDate.of(2024, 2, 29);
    assertEquals("2024-02-29", DateTimeUtils.formatDate(d));
  }


  @Test
  public void testParseDateMonthEnd() {
    assertEquals(LocalDate.of(2025, 4, 30),
            DateTimeUtils.parseDate("2025-04-30"));
    assertEquals(LocalDate.of(2025, 6, 30),
            DateTimeUtils.parseDate("2025-06-30"));
  }


  @Test(expected = IllegalArgumentException.class)
  public void testParseDateTimeWithWhitespace() {
    DateTimeUtils.parseDateTime(" 2025-07-04T09:00 ");
  }


  @Test
  public void testParseDateTimeWithMillis() {
    LocalDateTime dt = DateTimeUtils.parseDateTime("2025-07-04T09:00:00.123");

    assertEquals(LocalDateTime.of(2025, 7, 4, 9, 0, 0,
            123_000_000), dt);
  }


  @Test(expected = IllegalArgumentException.class)
  public void testParseDateTimeWithZone() {
    DateTimeUtils.parseDateTime("2025-07-04T09:00:00+09:00");
  }

  // formatDateTime(null) → NullPointerException
  @Test(expected = NullPointerException.class)
  public void testFormatDateTimeNull() {
    DateTimeUtils.formatDateTime(null);
  }

  // formatDate(null) → NullPointerException
  @Test(expected = NullPointerException.class)
  public void testFormatDateNull() {
    DateTimeUtils.formatDate(null);
  }


  @Test
  public void testExtremeYearDates() {
    assertEquals(LocalDate.of(1, 1, 1),
            DateTimeUtils.parseDate("0001-01-01"));
    assertEquals(LocalDate.of(9999, 12, 31),
            DateTimeUtils.parseDate("9999-12-31"));
  }

}
