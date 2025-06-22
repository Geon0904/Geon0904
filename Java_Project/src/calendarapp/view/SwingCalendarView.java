package calendarapp.view;

import calendarapp.controller.CalendarGuiController;
import calendarapp.model.Event;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.TimeZone;

/**
 * Swing GUI for a multi-calendar scheduling app.
 * Supports: calendar create/switch, event add/edit/delete, schedule view,
 * error handling, busy check.
 */
public class SwingCalendarView extends JFrame implements View {
  private final CalendarGuiController controller;
  private final JLabel statusLabel;
  private final JTextField subjectField;
  private final JTextField descField;
  private final JSpinner dateSpinner;
  private final JSpinner startSpinner;
  private final JSpinner endSpinner;
  private final JSpinner scheduleDateSpinner;
  private final JSpinner statusTimeSpinner;
  private final JTextArea eventListArea;
  private final JComboBox<String> calendarDropdown;
  private final JButton deleteButton = new JButton("Delete Event");
  private Event selectedEvent = null;

  /**
   * Constructs the main calendar GUI window.
   *
   * @param controller the CalendarGuiController to interact with model
   */
  public SwingCalendarView(CalendarGuiController controller) {
    super("Calendar App - GUI");
    this.controller = controller;

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(660, 570);
    setLayout(new BorderLayout(10, 10));
    setLocationRelativeTo(null);

    // Status label
    statusLabel = new JLabel("Ready.");
    statusLabel.setBorder(new EmptyBorder(3, 3, 3, 3));

    // Calendar select/create panel
    JPanel calPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    calPanel.add(new JLabel("Calendar:"));
    calendarDropdown = new JComboBox<>();
    JButton createCalButton = new JButton("New Calendar");
    calPanel.add(calendarDropdown);
    calPanel.add(createCalButton);
    updateCalendarDropdown();

    calendarDropdown.addActionListener(e -> {
      String sel = (String) calendarDropdown.getSelectedItem();
      if (sel != null && !sel.equals(controller.getActiveCalendarName())) {
        String err = controller.useCalendar(sel);
        if (err == null) {
          statusLabel.setText("Switched to: " + sel);
          refreshSchedule();
        } else {
          statusLabel.setText("[ERROR] " + err);
        }
      }
    });

    createCalButton.addActionListener(e -> {
      JTextField nameField = new JTextField(10);
      JComboBox<String> tzBox = new JComboBox<>(ZoneId.getAvailableZoneIds()
              .stream().sorted().toArray(String[]::new));
      tzBox.setSelectedItem(TimeZone.getDefault().getID());

      JPanel panel = new JPanel();
      panel.add(new JLabel("Name:"));
      panel.add(nameField);
      panel.add(new JLabel("TimeZone:"));
      panel.add(tzBox);
      int res = JOptionPane.showConfirmDialog(this, panel,
              "Create Calendar", JOptionPane.OK_CANCEL_OPTION);

      if (res == JOptionPane.OK_OPTION) {
        String name = nameField.getText().trim();
        String tz = (String) tzBox.getSelectedItem();
        if (name.isEmpty() || tz == null) {
          statusLabel.setText("[ERROR] Name/timezone required");
          return;
        }

        String err = controller.createCalendar(name, ZoneId.of(tz));

        if (err == null) {
          updateCalendarDropdown();
          calendarDropdown.setSelectedItem(name);
          statusLabel.setText("Created and switched to: " + name);
          refreshSchedule();
        } else {
          statusLabel.setText("[ERROR] " + err);
        }
      }
    });

    // Add Event Panel
    JPanel addPanel = new JPanel(new GridBagLayout());
    addPanel.setBorder(BorderFactory.createTitledBorder("Add Event"));
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(2, 2, 2, 2);
    c.fill = GridBagConstraints.HORIZONTAL;

    // Subject
    c.gridx = 0;
    c.gridy = 0;
    addPanel.add(new JLabel("Subject:"), c);
    subjectField = new JTextField(10);
    c.gridx = 1;
    addPanel.add(subjectField, c);

    // Description
    c.gridx = 0;
    c.gridy = 1;
    addPanel.add(new JLabel("Description:"), c);
    descField = new JTextField(15);
    c.gridx = 1;
    addPanel.add(descField, c);

    // Date picker
    c.gridx = 0;
    c.gridy = 2;
    addPanel.add(new JLabel("Date:"), c);
    dateSpinner = new JSpinner(new SpinnerDateModel());
    dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
    c.gridx = 1;
    addPanel.add(dateSpinner, c);

    // Start Time picker
    c.gridx = 0;
    c.gridy = 3;
    addPanel.add(new JLabel("Start Time:"), c);
    startSpinner = new JSpinner(new SpinnerDateModel());
    startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "HH:mm"));
    c.gridx = 1;
    addPanel.add(startSpinner, c);

    // End Time picker
    c.gridx = 0;
    c.gridy = 4;
    addPanel.add(new JLabel("End Time:"), c);
    endSpinner = new JSpinner(new SpinnerDateModel());
    endSpinner.setEditor(new JSpinner.DateEditor(endSpinner, "HH:mm"));
    c.gridx = 1;
    addPanel.add(endSpinner, c);

    // Add button (now local variable)
    c.gridx = 0;
    c.gridy = 5;
    c.gridwidth = 2;
    JButton addButton = new JButton("Add Event");
    addPanel.add(addButton, c);

    c.gridy = 6;
    addPanel.add(deleteButton, c);
    deleteButton.setEnabled(false);

    // Schedule View
    JPanel schedulePanel = new JPanel(new BorderLayout(3, 3));
    schedulePanel.setBorder(BorderFactory.createTitledBorder("Schedule View"));

    JPanel dateSelPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(2, 2, 2, 2);
    gbc.gridy = 0;


    gbc.gridx = 0;
    dateSelPanel.add(new JLabel("Show events from:"), gbc);


    gbc.gridx = 1;
    scheduleDateSpinner = new JSpinner(new SpinnerDateModel());
    scheduleDateSpinner.setEditor(new JSpinner.DateEditor(scheduleDateSpinner,
            "yyyy-MM-dd"));
    scheduleDateSpinner.setPreferredSize(new java.awt.Dimension(100, 25));
    dateSelPanel.add(scheduleDateSpinner, gbc);


    gbc.gridx = 2;
    dateSelPanel.add(new JLabel("at"), gbc);


    gbc.gridx = 3;
    statusTimeSpinner = new JSpinner(new SpinnerDateModel());
    statusTimeSpinner.setEditor(new JSpinner.DateEditor(statusTimeSpinner,
            "HH:mm"));
    statusTimeSpinner.setValue(java.sql.Time.valueOf(LocalTime.of(8, 0)));
    statusTimeSpinner.setPreferredSize(new java.awt.Dimension(80, 25));
    dateSelPanel.add(statusTimeSpinner, gbc);


    gbc.gridx = 4;
    JButton showStatusBtn = new JButton("Show Status");
    dateSelPanel.add(showStatusBtn, gbc);


    gbc.gridx = 5;
    JButton refreshButton = new JButton("Refresh");
    dateSelPanel.add(refreshButton, gbc);


    schedulePanel.add(dateSelPanel, BorderLayout.NORTH);


    eventListArea = new JTextArea(12, 42);
    eventListArea.setEditable(false);
    eventListArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
    JScrollPane scroll = new JScrollPane(eventListArea);
    schedulePanel.add(scroll, BorderLayout.CENTER);


    // Layout
    JPanel left = new JPanel(new BorderLayout());
    left.add(calPanel, BorderLayout.NORTH);
    left.add(addPanel, BorderLayout.CENTER);

    add(left, BorderLayout.WEST);
    add(schedulePanel, BorderLayout.CENTER);
    add(statusLabel, BorderLayout.SOUTH);

    // Button Listeners
    addButton.addActionListener(e -> {
      addEventAction();
    });
    refreshButton.addActionListener(e -> {
      refreshSchedule();
    });

    showStatusBtn.addActionListener(e -> {
      LocalDate date = ((java.util.Date) scheduleDateSpinner.getValue()).toInstant()
              .atZone(ZoneId.systemDefault()).toLocalDate();
      LocalTime time = ((java.util.Date) statusTimeSpinner.getValue()).toInstant()
              .atZone(ZoneId.systemDefault()).toLocalTime();
      LocalDateTime dt = LocalDateTime.of(date, time);

      String result = controller.checkBusy(dt);
      JOptionPane.showMessageDialog(this, result, "Status at " + dt,
              JOptionPane.INFORMATION_MESSAGE);
      statusLabel.setText(result);
    });

    deleteButton.addActionListener(e -> {
      if (selectedEvent == null) {
        JOptionPane.showMessageDialog(this,
                "No event selected for deletion.", "Delete Error",
                JOptionPane.ERROR_MESSAGE);
        return;
      }
      int confirm = JOptionPane.showConfirmDialog(this,
              "Delete selected event?\n" + selectedEvent.getSubject() + " " +
                      selectedEvent.getStart().toLocalDate() + " " +
                      selectedEvent.getStart().toLocalTime() + " ~ " +
                      selectedEvent.getEnd().toLocalTime(),
              "Confirm Delete", JOptionPane.YES_NO_OPTION);
      if (confirm == JOptionPane.YES_OPTION) {
        String result = controller.deleteEvent(selectedEvent);
        if (result.startsWith("[OK]")) {
          statusLabel.setText("Event deleted.");
          selectedEvent = null;
          deleteButton.setEnabled(false);
          refreshSchedule();
        } else {
          statusLabel.setText(result);
          JOptionPane.showMessageDialog(this, result, "Delete Event Error",
                  JOptionPane.ERROR_MESSAGE);
        }
      }
    });

    // Event editing/deleting
    eventListArea.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          int row = eventListArea.viewToModel2D(e.getPoint());
          String line = getEventLineAtCaret(row);
          if (line != null && line.startsWith("• ")) {
            Event ev = parseEventLineToEvent(line);
            if (ev != null) {
              selectedEvent = ev;
              deleteButton.setEnabled(true);
              Object[] options = {"Edit", "Delete", "Cancel"};
              int choice = JOptionPane.showOptionDialog(SwingCalendarView.this,
                      "Choose an action for this event:",
                      "Event Options",
                      JOptionPane.YES_NO_CANCEL_OPTION,
                      JOptionPane.QUESTION_MESSAGE,
                      null, options, options[2]);
              if (choice == 0) {
                editEventDialog(line);
              } else if (choice == 1) {
                deleteButton.doClick();
              }
            }
          }
        }
      }
    });

    // Init: Set pickers to now/today
    LocalDate now = LocalDate.now();
    dateSpinner.setValue(java.sql.Date.valueOf(now));
    scheduleDateSpinner.setValue(java.sql.Date.valueOf(now));
    statusTimeSpinner.setValue(java.sql.Time.valueOf(LocalTime.of(8, 0)));
    startSpinner.setValue(java.sql.Time.valueOf(LocalTime.of(8, 0)));
    endSpinner.setValue(java.sql.Time.valueOf(LocalTime.of(9, 0)));

    refreshSchedule();
    setVisible(true);
  }

  // Updates the calendar dropdown list to match manager state.
  private void updateCalendarDropdown() {
    List<String> names = controller.getCalendarNames();
    calendarDropdown.removeAllItems();
    for (String n : names) {
      calendarDropdown.addItem(n);
    }
    calendarDropdown.setSelectedItem(controller.getActiveCalendarName());
  }

  // Handler for the add event button.
  private void addEventAction() {
    try {
      String subject = subjectField.getText().trim();
      if (subject.isEmpty()) {
        throw new IllegalArgumentException("Subject is required.");
      }
      String desc = descField.getText().trim();

      LocalDate date = ((java.util.Date) dateSpinner.getValue()).toInstant()
              .atZone(ZoneId.systemDefault()).toLocalDate();

      LocalTime startTime = ((java.util.Date) startSpinner.getValue()).toInstant()
              .atZone(ZoneId.systemDefault()).toLocalTime();

      LocalTime endTime = ((java.util.Date) endSpinner.getValue()).toInstant()
              .atZone(ZoneId.systemDefault()).toLocalTime();

      if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
        throw new IllegalArgumentException("End time must be after start time.");
      }

      LocalDateTime startDT = LocalDateTime.of(date, startTime);
      LocalDateTime endDT = LocalDateTime.of(date, endTime);

      String result = controller.addEvent(subject, startDT, endDT, desc);

      if (result.startsWith("[OK]")) {
        statusLabel.setText("Event added!");
        subjectField.setText("");
        descField.setText("");
        refreshSchedule();
      } else {
        statusLabel.setText(result);
        JOptionPane.showMessageDialog(this, result, "Add Event Error",
                JOptionPane.ERROR_MESSAGE);
      }

    } catch (Exception ex) {
      String msg = ex.getMessage() != null ? ex.getMessage() : "Unknown error";
      statusLabel.setText(msg);
      JOptionPane.showMessageDialog(this, msg, "Add Event Error",
              JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Shows a dialog to edit the double-clicked event line.
   *
   * @param line formatted event line
   */
  private void editEventDialog(String line) {
    try {
      String trimmed = line.substring(2).trim();
      String[] split = trimmed.split(":", 2);
      String subject = split[0].trim();
      String[] rest = split[1].trim().split("—", 2);
      String timePart = rest[0].trim();
      String desc = rest.length > 1 ? rest[1].trim() : "";
      String[] timeSplit = timePart.split("~");
      String[] dateStart = timeSplit[0].trim().split(" ");
      LocalDate date = LocalDate.parse(dateStart[0]);
      LocalTime startTime = LocalTime.parse(dateStart[1]);
      LocalTime endTime = LocalTime.parse(timeSplit[1].trim());
      LocalDateTime startDT = LocalDateTime.of(date, startTime);
      LocalDateTime endDT = LocalDateTime.of(date, endTime);

      List<Event> events = controller.getEventsOn(date);
      Event ev = events.stream()
              .filter(e -> e.getSubject().equals(subject)
                      && e.getStart().equals(startDT)
                      && e.getEnd().equals(endDT))
              .findFirst().orElse(null);
      if (ev == null) {
        return;
      }

      JTextField subjectEdit = new JTextField(ev.getSubject(), 10);
      JTextField descEdit = new JTextField(ev.getDescription(), 15);
      JSpinner startSpin = new JSpinner(new SpinnerDateModel());
      JSpinner endSpin = new JSpinner(new SpinnerDateModel());
      startSpin.setEditor(new JSpinner.DateEditor(startSpin, "HH:mm"));
      endSpin.setEditor(new JSpinner.DateEditor(endSpin, "HH:mm"));
      startSpin.setValue(java.sql.Time.valueOf(ev.getStart().toLocalTime()));
      endSpin.setValue(java.sql.Time.valueOf(ev.getEnd().toLocalTime()));

      JPanel panel = new JPanel(new GridLayout(0, 2));
      panel.add(new JLabel("Subject:"));
      panel.add(subjectEdit);
      panel.add(new JLabel("Description:"));
      panel.add(descEdit);
      panel.add(new JLabel("Start Time:"));
      panel.add(startSpin);
      panel.add(new JLabel("End Time:"));
      panel.add(endSpin);

      int res = JOptionPane.showConfirmDialog(this, panel, "Edit Event",
              JOptionPane.OK_CANCEL_OPTION);
      if (res == JOptionPane.OK_OPTION) {
        String newSub = subjectEdit.getText().trim();
        String newDesc = descEdit.getText().trim();

        LocalTime newStart = ((java.util.Date) startSpin.getValue()).toInstant()
                .atZone(ZoneId.systemDefault()).toLocalTime();

        LocalTime newEnd = ((java.util.Date) endSpin.getValue()).toInstant()
                .atZone(ZoneId.systemDefault()).toLocalTime();

        if (newEnd.isBefore(newStart) || newEnd.equals(newStart)) {
          JOptionPane.showMessageDialog(this,
                  "End time must be after start time.", "Edit Error",
                  JOptionPane.ERROR_MESSAGE);
          return;
        }

        LocalDateTime newStartDT = LocalDateTime.of(date, newStart);
        LocalDateTime newEndDT = LocalDateTime.of(date, newEnd);
        String result = controller.editEvent(ev, newSub, newStartDT, newEndDT, newDesc);

        if (result.startsWith("[OK]")) {
          statusLabel.setText("Event updated!");
          refreshSchedule();
        } else {
          statusLabel.setText(result);
          JOptionPane.showMessageDialog(this, result, "Edit Event Error",
                  JOptionPane.ERROR_MESSAGE);
        }
      }
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, ex.getMessage(), "Edit Event Error",
              JOptionPane.ERROR_MESSAGE);
    }
  }

  // Finds the event line at a given caret position.
  private String getEventLineAtCaret(int pos) {
    String[] lines = eventListArea.getText().split("\n");
    int total = 0;
    for (String l : lines) {
      total += l.length() + 1;
      if (pos < total) {
        return l;
      }
    }
    return null;
  }

  // Parses an event line into an Event, if possible.
  private Event parseEventLineToEvent(String line) {
    try {
      String trimmed = line.substring(2).trim();
      String[] split = trimmed.split(":", 2);
      String subject = split[0].trim();
      String[] rest = split[1].trim().split("—", 2);
      String timePart = rest[0].trim();
      String desc = rest.length > 1 ? rest[1].trim() : "";
      String[] timeSplit = timePart.split("~");
      String[] dateStart = timeSplit[0].trim().split(" ");
      LocalDate date = LocalDate.parse(dateStart[0]);
      LocalTime startTime = LocalTime.parse(dateStart[1]);
      LocalTime endTime = LocalTime.parse(timeSplit[1].trim());
      LocalDateTime startDT = LocalDateTime.of(date, startTime);
      LocalDateTime endDT = LocalDateTime.of(date, endTime);

      List<Event> events = controller.getEventsOn(date);
      for (Event ev : events) {
        if (ev.getSubject().equals(subject) &&
                ev.getStart().equals(startDT) &&
                ev.getEnd().equals(endDT)) {
          return ev;
        }
      }
      return null;
    } catch (Exception ex) {
      return null;
    }
  }

  // Handler for the refresh schedule button.
  private void refreshSchedule() {
    try {
      LocalDate start = ((java.util.Date) scheduleDateSpinner.getValue()).toInstant()
              .atZone(ZoneId.systemDefault()).toLocalDate();

      List<Event> events = controller.getEventsFrom(start, 10);
      StringBuilder sb = new StringBuilder();
      if (events.isEmpty()) {
        sb.append("No events scheduled from ").append(start);
      } else {
        for (Event ev : events) {
          sb.append("• ")
                  .append(ev.getSubject())
                  .append(": ")
                  .append(ev.getStart().toLocalDate()).append(" ")
                  .append(ev.getStart().toLocalTime())
                  .append(" ~ ")
                  .append(ev.getEnd().toLocalTime());
          if (!ev.getDescription().isEmpty()) {
            sb.append(" — ").append(ev.getDescription());
          }
          sb.append("\n");
        }
      }
      eventListArea.setText(sb.toString());
      statusLabel.setText("Schedule updated.");
      selectedEvent = null;
      deleteButton.setEnabled(false);
    } catch (Exception ex) {
      String msg = ex.getMessage() != null ? ex.getMessage() : "Unknown error";
      statusLabel.setText(msg);
      JOptionPane.showMessageDialog(this, msg, "Schedule Error",
              JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * For View interface: show status.
   */
  @Override
  public void render(String message) {
    statusLabel.setText(message);
  }
}
