package calendarapp.controller.commands;

import calendarapp.model.CalendarModel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;



/**
 * Command to check if the calendar is busy at a specific date-time.
 */
public class ShowStatusCommand implements calendarapp.controller.commands.CalendarCommand {

  private final String dateTimeStr;

  /**
   * Constructs the ShowStatusCommand.
   *
   * @param dateTimeStr the date-time string to check in format
   */
  public ShowStatusCommand(String dateTimeStr) {
    this.dateTimeStr = dateTimeStr;
  }


  @Override
  public String execute(CalendarModel model) {
    try {
      LocalDateTime dateTime = LocalDateTime.parse(
              dateTimeStr,
              DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      boolean isBusy = model.isBusyAt(dateTime);
      return isBusy
              ? String.format("[BUSY] %s", dateTime)
              : String.format("[AVAILABLE] %s", dateTime);
    } catch (DateTimeParseException e) {
      return "[ERROR] Invalid date-time format. Use 'YYYY-MM-DDTHH:MM'.";
    }
  }
}
