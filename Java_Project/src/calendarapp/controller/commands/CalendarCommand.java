package calendarapp.controller.commands;

import calendarapp.model.CalendarModel;



/**
 * Represents a command that can be executed on a calendar model.
 */

public interface CalendarCommand {
  String execute(CalendarModel model);
}
