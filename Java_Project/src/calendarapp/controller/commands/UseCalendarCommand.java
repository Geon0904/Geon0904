package calendarapp.controller.commands;

import calendarapp.controller.commands.CalendarCommand;
import calendarapp.model.MultiCalendarModel;

import java.util.NoSuchElementException;

/**
 * Command to switch the active calendar by name.
 */
public class UseCalendarCommand implements CalendarCommand {

  private final String name;

  /**
   * Constructs the UseCalendarCommand.
   *
   * @param name the name of the calendar to activate
   */
  public UseCalendarCommand(String name) {
    this.name = name;
  }

  /**
   * Executes the command by casting the model to MultiCalendarModel
   * and invoking useCalendar.
   *
   * @param model the calendar manager
   * @return a success or error message
   */

  @Override
  public String execute(calendarapp.model.CalendarModel model) {
    MultiCalendarModel mgr = (MultiCalendarModel) model;
    try {
      mgr.useCalendar(name);
      return String.format("[OK] Switched to calendar '%s'", name);
    } catch (NoSuchElementException e) {
      return "[ERROR] No calendar named '" + name + "'";
    }
  }
}
