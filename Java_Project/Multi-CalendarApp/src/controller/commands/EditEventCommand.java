package calendarapp.controller.commands;

import calendarapp.model.CalendarModel;
import calendarapp.model.Event;
import calendarapp.model.EventId;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;

/**
 * Command to edit a specific event by ID and change one of its properties.
 *
 * <p>This constructor accepts:
 * - subject: the event’s subject
 * - startStr: the event’s start date-time string
 * - endStr: the event’s end date-time string
 * - property: the property to change
 * - newValue: the new value for that property
 * </p>
 */
public class EditEventCommand implements CalendarCommand {

  private final String subject;
  private final String startStr;
  private final String endStr;
  private final String property;
  private final String newValue;

  /**
   * Constructs an EditEventCommand with five parameters.
   *
   * @param subject  the subject of the event to edit
   * @param startStr the start date-time of the event to edit
   * @param endStr   the end date-time of the event to edit
   * @param property the property to change
   * @param newValue the new value for that property
   */
  public EditEventCommand(String subject,
                          String startStr,
                          String endStr,
                          String property,
                          String newValue) {
    this.subject = subject;
    this.startStr = startStr;
    this.endStr = endStr;
    this.property = property;
    this.newValue = newValue;
  }

  @Override
  public String execute(CalendarModel model) {
    try {
      LocalDateTime startDT = LocalDateTime.parse(startStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      LocalDateTime endDT = LocalDateTime.parse(endStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      EventId id = new EventId(subject, startDT, endDT);
      Event oldEvent = model.getEventById(id);
      String oldSubject = oldEvent.getSubject();
      LocalDateTime oldStart = oldEvent.getStart();
      LocalDateTime oldEnd = oldEvent.getEnd();
      String oldDesc = oldEvent.getDescription();
      String oldLoc = oldEvent.getLocation();
      boolean oldPub = oldEvent.isPublic();


      Event modified;
      switch (property.toLowerCase()) {
        case "subject":
          modified = new Event(newValue, oldStart, oldEnd, oldDesc, oldLoc, oldPub);
          model.editEvent(id, modified);
          return String.format("[OK] Updated 'subject' (%s -> %s)", oldSubject, newValue);

        case "start":
          LocalDateTime newStart = LocalDateTime.parse(newValue,
                  DateTimeFormatter.ISO_LOCAL_DATE_TIME);
          modified = new Event(oldSubject, newStart, oldEnd, oldDesc, oldLoc, oldPub);
          model.editEvent(id, modified);
          return String.format("[OK] Updated 'start' (%s -> %s)",
                  oldStart.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                  newValue);

        case "end":
          LocalDateTime newEnd = LocalDateTime.parse(newValue,
                  DateTimeFormatter.ISO_LOCAL_DATE_TIME);
          modified = new Event(oldSubject, oldStart, newEnd, oldDesc, oldLoc, oldPub);
          model.editEvent(id, modified);
          return String.format("[OK] Updated 'end' (%s -> %s)",
                  oldEnd.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                  newValue);

        case "description":
          modified = new Event(oldSubject, oldStart, oldEnd, newValue, oldLoc, oldPub);
          model.editEvent(id, modified);
          return String.format("[OK] Updated 'description' (%s -> %s)", oldDesc, newValue);

        case "location":
          modified = new Event(oldSubject, oldStart, oldEnd, oldDesc, newValue, oldPub);
          model.editEvent(id, modified);
          return String.format("[OK] Updated 'location' (%s -> %s)", oldLoc, newValue);

        case "status":
          boolean newPub = newValue.equalsIgnoreCase("public");
          modified = new Event(oldSubject, oldStart, oldEnd, oldDesc, oldLoc, newPub);
          model.editEvent(id, modified);
          String oldStatusStr = oldPub ? "public" : "private";
          return String.format("[OK] Updated 'status' (%s -> %s)", oldStatusStr, newValue);

        default:
          return "[ERROR] Unsupported property: " + property;
      }
    } catch (DateTimeParseException e) {
      return "[ERROR] Invalid datetime format. Use 'YYYY-MM-DDTHH:MM'.";
    } catch (NoSuchElementException e) {
      return "[ERROR] No such event found to edit.";
    }
  }
}