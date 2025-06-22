package calendarapp.controller.commands;

import calendarapp.model.CalendarModel;

/**
 * A no-op command that signals the application to exit.
 */
public class ExitCommand implements CalendarCommand {

  /**
   * Returns a standard exit message. The CalendarModel is not used here.
   *
   * @param model the CalendarModel (unused)
   * @return a string indicating successful exit
   */
  @Override
  public String execute(CalendarModel model) {
    return "[OK] Exiting.";
  }
}
