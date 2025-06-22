package calendarapp.controller.commands;


import calendarapp.model.CalendarManagerImpl;
import calendarapp.model.MultiCalendarModel;
import calendarapp.model.CalendarModel;

import java.time.LocalDateTime;


/**
 * Command to copy a single event from the active calendar
 * to a target calendar at a new start time.
 */

public class CopyEventCommand implements CalendarCommand {
  private final String subject;
  private final LocalDateTime srcStart;
  private final String targetCal;
  private final LocalDateTime destStart;


  /**
   * Creates a command to copy a single event identified by its subject and original start time
   * from the active calendar to a specified target calendar at a new start time.
   *
   * @param subject   the title of the event to copy
   * @param srcStart  the original start date-time of the event in the active calendar
   * @param targetCal the name of the calendar to which the event will be copied
   * @param destStart the new start date-time for the copied event in the target calendar
   */

  public CopyEventCommand(String subject,
                          LocalDateTime srcStart,
                          String targetCal,
                          LocalDateTime destStart) {
    this.subject = subject;
    this.srcStart = srcStart;
    this.targetCal = targetCal;
    this.destStart = destStart;
  }

  @Override
  public String execute(CalendarModel ignored) {
    MultiCalendarModel mgr = (MultiCalendarModel) ignored;


    CalendarModel target = null;
    try {


      target = ((CalendarManagerImpl) mgr).getCalendarByName(targetCal);

      if (target == null) {
        return String.format("[ERROR] No calendar named '%s'.", targetCal);
      }
    } catch (Exception e) {
      return String.format("[ERROR] No calendar named '%s'.", targetCal);
    }


    boolean duplicate = target.getEventsOn(destStart.toLocalDate()).stream()
            .anyMatch(e -> e.getSubject().equals(subject)
                    && e.getStart().equals(destStart));
    if (duplicate) {
      return String.format("[ERROR] Event '%s' already exists.", subject);
    }


    boolean exists = mgr.getActiveCalendar().getEventsOn(srcStart.toLocalDate()).stream()
            .anyMatch(e -> e.getSubject().equals(subject)
                    && e.getStart().equals(srcStart));
    if (!exists) {
      return String.format("[ERROR] No event named '%s' starting at %s.", subject, srcStart);
    }


    int count = mgr.copyEvent(subject, srcStart, targetCal, destStart);
    if (count == 1) {

      return String.format(
              "[OK] Event '%s' copied to calendar '%s' at %s.",
              subject, targetCal, destStart
      );
    } else {

      return "[ERROR] Unknown error copying event.";
    }
  }

}
