package calendarapp.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for validating various user inputs.
 */
public class InputValidator {


  /**
   * Checks if the given string is a valid ISO_LOCAL_DATE_TIME.
   *
   * @param input the string to check
   * @return true if the string is a valid datetime, false otherwise
   */

  public static boolean isValidDateTime(String input) {
    try {
      LocalDateTime.parse(input, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      return true;
    } catch (DateTimeParseException e) {
      return false;
    }
  }

  /**
   * Checks if the given string is a valid ISO_LOCAL_DATE.
   *
   * @param input the string to check
   * @return true if the string is a valid date, false otherwise
   */

  public static boolean isValidDate(String input) {
    try {
      LocalDate.parse(input, DateTimeFormatter.ISO_LOCAL_DATE);
      return true;
    } catch (DateTimeParseException e) {
      return false;
    }
  }


  /**
   * Checks if the given string is a valid integer.
   *
   * @param input the string to check
   * @return true if the string can be parsed as an integer, false otherwise
   */
  public static boolean isValidInteger(String input) {
    try {
      Integer.parseInt(input);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }


  /**
   * Checks if the given string is a valid boolean.
   *
   * @param input the string to check
   * @return true if the string is "true" or "false", false otherwise
   */
  public static boolean isValidBoolean(String input) {
    return input.equalsIgnoreCase("true") || input.equalsIgnoreCase("false");
  }


  /**
   * Checks if the string only contains valid weekday characters.
   *
   * @param input the weekday string
   * @return true if the input matches weekday pattern, false otherwise
   */
  public static boolean isValidWeekdayPattern(String input) {
    return input.matches("[MTWRFSU]+");
  }

}
