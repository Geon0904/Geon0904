package calendarapp.controller.commands;


import calendarapp.exceptions.DuplicateEventException;
import calendarapp.model.MultiCalendarModel;


import java.time.ZoneId;

/**
 * Command to create a new named calendar with a specific timezone.
 */
public class CreateCalendarCommand implements CalendarCommand {

  private final String name;
  private final ZoneId tz;

  /**
   * Constructs the CreateCalendarCommand.
   *
   * @param name the unique name of the new calendar
   * @param tz   the timezone for the new calendar
   */
  public CreateCalendarCommand(String name, ZoneId tz) {
    this.name = name;
    this.tz = tz;
  }

  /**
   * Executes the command by casting the given model to MultiCalendarModel
   * and invoking createCalendar.
   *
   * @param model the calendar manager
   * @return a success or error message
   */
  @Override
  public String execute(calendarapp.model.CalendarModel model) {
    MultiCalendarModel mgr = (MultiCalendarModel) model;
    try {
      mgr.createCalendar(name, tz);
      return String.format("[OK] Created calendar '%s' with timezone %s", name, tz);
    } catch (DuplicateEventException e) {
      return "[ERROR] " + e.getMessage();
    }
  }
}
