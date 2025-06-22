package calendarapp.controller;

import calendarapp.controller.commands.CalendarCommand;
import calendarapp.model.MultiCalendarModel;
import calendarapp.model.CalendarModel;

/**
 * Controller to handle execution of calendar commands.
 * Now supports both single- and multi-calendar models.
 */
public class CalendarController {
  private final MultiCalendarModel model;

  /**
   * Constructs a controller with the given multi-calendar model.
   *
   * @param model the multi-calendar model to operate on
   */
  public CalendarController(MultiCalendarModel model) {
    this.model = model;
  }

  /**
   * Executes the given command against the current model.
   *
   * @param cmd the parsed CalendarCommand
   * @return the result message from the command
   */
  public String handleCommand(CalendarCommand cmd) {
    if (cmd == null) {
      throw new IllegalArgumentException("Command cannot be null");
    }
    try {
      String result = cmd.execute((CalendarModel) model);
      return (result == null) ? "" : result;

    } catch (Exception e) {
      String msg = e.getMessage();
      if (msg != null && msg.startsWith("[ERROR]")) {
        return msg;
      }
      return "[ERROR] " + msg;
    }
  }

}
