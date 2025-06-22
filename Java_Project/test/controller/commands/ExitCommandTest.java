package controller.commands;

import calendarapp.controller.commands.ExitCommand;
import calendarapp.model.CalendarImpl;
import calendarapp.model.CalendarModel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the ExitCommand class.
 */
public class ExitCommandTest {

  //Check always return "[OK] Exiting."
  @Test
  public void testExecuteReturnsExitMessage() {
    CalendarModel dummy = new CalendarImpl();
    ExitCommand cmd = new ExitCommand();
    String result = cmd.execute(dummy);
    assertEquals("[OK] Exiting.", result);
  }

  //Check if model is null, then always return "[OK] Exiting."
  @Test
  public void testExecuteWithNullModel() {
    ExitCommand cmd = new ExitCommand();
    String result = cmd.execute(null);
    assertEquals("[OK] Exiting.", result);
  }

}
