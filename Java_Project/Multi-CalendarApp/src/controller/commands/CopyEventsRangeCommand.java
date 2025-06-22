
package calendarapp.controller.commands;


import calendarapp.model.MultiCalendarModel;
import calendarapp.model.CalendarModel;

import java.time.LocalDate;
import java.util.NoSuchElementException;

/**
 * Command to copy all events in a date interval from the active calendar to a target calendar,
 * shifting the interval to start at a new date.
 */
public class CopyEventsRangeCommand implements CalendarCommand {
  private final LocalDate srcStart;
  private final LocalDate srcEnd;
  private final String targetCal;
  private final LocalDate destStart;


  /**
   * A command that copies all events occurring between two dates from the active calendar
   * to a specified target calendar, starting at a given destination date.
   *
   * @param srcStart  the start date of the source event range
   * @param srcEnd    the end date of the source event range
   * @param targetCal the name of the calendar to which events will be copied
   * @param destStart the start date in the target calendar where the first copied event will
   *                  begin
   */
  public CopyEventsRangeCommand(LocalDate srcStart,
                                LocalDate srcEnd,
                                String targetCal,
                                LocalDate destStart) {
    this.srcStart = srcStart;
    this.srcEnd = srcEnd;
    this.targetCal = targetCal;
    this.destStart = destStart;
  }


  @Override
  public String execute(CalendarModel ignored) {
    MultiCalendarModel mgr = (MultiCalendarModel) ignored;
    try {
      int count = mgr.copyEventsBetween(srcStart, srcEnd, targetCal, destStart);
      return String.format(
              "[OK] Copied %d events from %s–%s to calendar '%s' starting %s",
              count, srcStart, srcEnd, targetCal, destStart
      );
    } catch (NoSuchElementException e) {
      return "[ERROR] " + e.getMessage();
    } catch (Exception e) {
      return "[ERROR] " + e.getMessage();
    }
  }
}
