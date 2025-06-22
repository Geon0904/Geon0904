package calendarapp.controller.commands;


import calendarapp.model.MultiCalendarModel;
import calendarapp.model.CalendarModel;

import java.time.LocalDate;

import java.util.NoSuchElementException;

/**
 * Command to copy all events on one date from the active calendar
 * to a target calendar preserving times but converting timezones.
 */
public class CopyEventsCommand implements CalendarCommand {
  private final LocalDate srcDate;
  private final String targetCal;
  private final LocalDate destDate;


  /**
   * Creates a command to copy all events occurring on a specific source date
   * from the active calendar to a specified target calendar.
   *
   * @param srcDate   the date of the events to copy from the active calendar
   * @param targetCal the name of the calendar to which events will be copied
   * @param destDate  the date on which the copied events will be placed in the target calendar
   */
  public CopyEventsCommand(LocalDate srcDate,
                           String targetCal,
                           LocalDate destDate) {
    this.srcDate = srcDate;
    this.targetCal = targetCal;
    this.destDate = destDate;
  }

  @Override
  public String execute(CalendarModel ignored) {
    MultiCalendarModel mgr = (MultiCalendarModel) ignored;
    try {
      int count = mgr.copyEventsOn(srcDate, targetCal, destDate);
      return String.format(
              "[OK] Copied %d events from %s to calendar '%s' on %s",
              count, srcDate, targetCal, destDate
      );
    } catch (NoSuchElementException e) {
      return "[ERROR] " + e.getMessage();
    } catch (Exception e) {
      return "[ERROR] " + e.getMessage();
    }
  }
}
