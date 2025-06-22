package calendarapp.controller.commands;


import calendarapp.exceptions.DuplicateEventException;
import calendarapp.model.CalendarModel;
import calendarapp.model.MultiCalendarModel;

import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;

/**
 * Command to edit a calendar's property.
 */
public class EditCalendarCommand implements CalendarCommand {
  private final String name;
  private final String property;
  private final String newValue;

  /**
   * Constructs the EditCalendarCommand.
   *
   * @param name     the existing calendar name
   * @param property either "name" or "timezone"
   * @param newValue the new value for that property
   */
  public EditCalendarCommand(String name, String property, String newValue) {
    this.name = name;
    this.property = property.toLowerCase();
    this.newValue = newValue;
  }

  @Override
  public String execute(CalendarModel ignored) {
    MultiCalendarModel mgr = (MultiCalendarModel) ignored;
    try {
      switch (property) {
        case "name":
          mgr.editCalendar(name, "name", newValue);
          return String.format("[OK] Calendar renamed '%s' → '%s'", name, newValue);

        case "timezone":
          ZoneId tz = ZoneId.of(newValue);
          mgr.editCalendar(name, "timezone", tz.toString());
          return String.format("[OK] Calendar '%s' timezone changed to %s", name, tz);

        default:
          return "[ERROR] Unsupported calendar property: " + property;
      }

    } catch (DateTimeParseException e) {
      return "[ERROR] Invalid timezone format: " + newValue;

    } catch (NoSuchElementException e) {
      return "[ERROR] No calendar named '" + name + "'";

    } catch (DuplicateEventException e) {
      return "[ERROR] " + e.getMessage();
    }
  }

}
