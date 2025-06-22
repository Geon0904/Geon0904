package calendarapp.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for handling date and time parsing and formatting.
 */
public class DateTimeUtils {

  private static final DateTimeFormatter DATE_TIME_FORMATTER
          = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  private static final DateTimeFormatter DATE_FORMATTER
          = DateTimeFormatter.ISO_LOCAL_DATE;


  /**
   * Parses a string into a LocalDateTime.
   *
   * @param input the string to parse
   * @return the corresponding LocalDateTime
   * @throws IllegalArgumentException if the input format is invalid
   */
  public static LocalDateTime parseDateTime(String input) {
    try {
      return LocalDateTime.parse(input, DATE_TIME_FORMATTER);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid datetime format. Use 'YYYY-MM-DDTHH:MM'.");
    }
  }


  /**
   * Parses a string into a LocalDate.
   *
   * @param input the string to parse
   * @return the corresponding LocalDate
   * @throws IllegalArgumentException if the input format is invalid
   */
  public static LocalDate parseDate(String input) {
    try {
      return LocalDate.parse(input, DATE_FORMATTER);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date format. Use 'YYYY-MM-DD'.");
    }
  }


  /**
   * Formats a LocalDateTime as a string.
   *
   * @param dt the LocalDateTime to format
   * @return formatted string
   */
  public static String formatDateTime(LocalDateTime dt) {
    return dt.format(DATE_TIME_FORMATTER);
  }

  /**
   * Formats a LocalDate as a string.
   *
   * @param date the LocalDate to format
   * @return formatted string
   */
  public static String formatDate(LocalDate date) {
    return date.format(DATE_FORMATTER);
  }


  //Prevent instantiation
  private DateTimeUtils() {
    throw new AssertionError("Cannot instantiate utility class.");
  }
}
