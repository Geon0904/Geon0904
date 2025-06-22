package controller.commands;

import calendarapp.util.InputValidator;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;


/**
 * Unit tests for InputValidator.
 */
public class InputValidatorTest {

  //Tests that various date‐time strings are correctly recognized.
  @Test
  public void testValidDateTimeFormats() {

    assertTrue(InputValidator.isValidDateTime("2025-07-04T09:00"));
    assertTrue(InputValidator.isValidDateTime("2025-12-31T23:59"));

    assertTrue(InputValidator.isValidDateTime("2025-07-04T09:00:00"));
    assertTrue(InputValidator.isValidDateTime("1999-01-01T00:00:00"));

    assertFalse(InputValidator.isValidDateTime("2025/07/04 09:00"));

    assertFalse(InputValidator.isValidDateTime("2025-13-01T10:00"));
    assertFalse(InputValidator.isValidDateTime("2025-02-30T12:00"));

    assertFalse(InputValidator.isValidDateTime(""));
    assertFalse(InputValidator.isValidDateTime("   "));

    assertFalse(InputValidator.isValidDateTime("July 4 2025 09:00"));
  }


  //Tests that various date strings are correctly recognized.
  @Test
  public void testValidDateFormats() {
    assertTrue(InputValidator.isValidDate("2025-07-04"));
    assertTrue(InputValidator.isValidDate("1999-12-31"));

    assertFalse(InputValidator.isValidDate("07/04/2025"));
    assertFalse(InputValidator.isValidDate("2025-7-4"));

    assertFalse(InputValidator.isValidDate("2025-13-01"));
    assertFalse(InputValidator.isValidDate("2025-02-30"));

    assertFalse(InputValidator.isValidDate(""));
    assertFalse(InputValidator.isValidDate("   "));

    assertFalse(InputValidator.isValidDate("July 4, 2025"));
  }


  //Tests integer validation, including negative, zero, positive, and non-integer inputs
  @Test
  public void testValidInteger() {
    assertTrue(InputValidator.isValidInteger("0"));
    assertTrue(InputValidator.isValidInteger("123"));
    assertTrue(InputValidator.isValidInteger("-456"));

    assertFalse(InputValidator.isValidInteger("3.14"));
    assertFalse(InputValidator.isValidInteger("abc"));
    assertFalse(InputValidator.isValidInteger(""));
    assertFalse(InputValidator.isValidInteger(" "));
  }


  //Tests boolean validation
  @Test
  public void testValidBoolean() {
    assertTrue(InputValidator.isValidBoolean("true"));
    assertTrue(InputValidator.isValidBoolean("True"));
    assertTrue(InputValidator.isValidBoolean("FALSE"));

    assertFalse(InputValidator.isValidBoolean("yes"));
    assertFalse(InputValidator.isValidBoolean("0"));
    assertFalse(InputValidator.isValidBoolean(""));
    assertFalse(InputValidator.isValidBoolean(" maybe "));
  }

  //Tests weekday‐pattern validation
  @Test
  public void testValidWeekdayPattern() {

    assertTrue(InputValidator.isValidWeekdayPattern("MTWRF"));
    assertTrue(InputValidator.isValidWeekdayPattern("SU"));
    assertTrue(InputValidator.isValidWeekdayPattern("M"));

    assertFalse(InputValidator.isValidWeekdayPattern("mtw"));
    assertFalse(InputValidator.isValidWeekdayPattern("mwf"));

    assertFalse(InputValidator.isValidWeekdayPattern("MTXRF"));

    assertFalse(InputValidator.isValidWeekdayPattern(""));
    assertFalse(InputValidator.isValidWeekdayPattern(" "));

    assertFalse(InputValidator.isValidWeekdayPattern("Mon"));
  }
}
