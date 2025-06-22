package integration;

import calendarapp.model.CalendarImpl;
import calendarapp.model.CalendarModel;
import calendarapp.model.Event;
import calendarapp.view.InteractiveView;
import calendarapp.exceptions.DuplicateEventException;
import calendarapp.model.EventId;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;


/**
 * Tests simulating interactive mode behavior with a simplified command processor and view.
 */
public class InteractiveModeTest {

  private final InputStream originalSystemIn = System.in;
  private final PrintStream originalSystemOut = System.out;
  private ByteArrayOutputStream systemOutCaptured;
  private CalendarModel calendarModel;

  // Test helper class to simulate a simplified command processor
  static class TestInteractiveCommandProcessor {
    private final CalendarModel model;
    private final InteractiveView view;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public TestInteractiveCommandProcessor(CalendarModel model, InteractiveView view) {
      this.model = model;
      this.view = view;
    }

    public void processCommand(String commandStr) {
      if (commandStr == null || commandStr.isBlank()) {
        view.render("[ERROR] Empty command received.");
        return;
      }
      String[] parts = commandStr.trim().split("\\s+");
      String cmd = parts[0].toLowerCase();

      try {
        // Simplified: create_event Subject YYYY-MM-DDTHH:MM:SS YYYY-MM-DDTHH:MM:SS
        if ("create_event".equals(cmd) && parts.length == 4) {
          String subject = parts[1];
          LocalDateTime start = LocalDateTime.parse(parts[2], FMT);
          LocalDateTime end = LocalDateTime.parse(parts[3], FMT);
          model.addEvent(new Event(subject, start, end));
          view.render("[OK] Event '" + subject + "' created.");
        } else if ("show_status_at".equals(cmd) && parts.length == 2) {
          LocalDateTime dateTime = LocalDateTime.parse(parts[1], FMT);
          boolean busy = model.isBusyAt(dateTime);
          String statusMsg = "Status at " + parts[1] + (busy ? ": Busy" : ": Available");
          view.render(statusMsg);
        } else {
          view.render("[ERROR] Unknown or invalid command: " + commandStr);
        }
      } catch (DateTimeParseException e) {
        view.render("[ERROR] Invalid date/time format: " + commandStr);
      } catch (DuplicateEventException e) {
        view.render("[ERROR] " + e.getMessage());
      } catch (IllegalArgumentException e) {
        view.render("[ERROR] " + e.getMessage());
      } catch (Exception e) {
        view.render("[ERROR] Unexpected error: " + e.getMessage());
      }
    }
  }

  // Test helper class to simulate the interactive application loop
  static class InteractiveAppSimulator {
    private final TestInteractiveCommandProcessor processor;
    private final InteractiveView view;

    public InteractiveAppSimulator(TestInteractiveCommandProcessor processor,
                                   InteractiveView view) {
      this.processor = processor;
      this.view = view;
    }

    public void run() {
      String command;
      while (true) {
        try {
          command = view.readCommand();
          if ("exit".equalsIgnoreCase(command.trim())) {
            view.render("Application exiting.");
            break;
          }
          processor.processCommand(command);
        } catch (NoSuchElementException e) {
          view.render("Error: Input stream closed.");
          break;
        }
      }
    }
  }

  /**
   * Sets up output stream capture and initializes the calendar model.
   */
  @Before
  public void setUp() {
    systemOutCaptured = new ByteArrayOutputStream();
    System.setOut(new PrintStream(systemOutCaptured));
    calendarModel = new CalendarImpl();
  }

  private void provideInput(String data) {
    ByteArrayInputStream testIn = new ByteArrayInputStream(data.getBytes());
    System.setIn(testIn);
  }

  /**
   * Restores original System.in and System.out.
   */
  @After
  public void tearDown() {
    System.setIn(originalSystemIn);
    System.setOut(originalSystemOut);
  }

  /**
   * Tests basic rendering and command reading functionality of the InteractiveView.
   */
  @Test
  public void interactiveViewRenderAndRead() {
    provideInput("test command" + System.lineSeparator());
    InteractiveView view = new InteractiveView();
    view.render("Render test.");
    String cmd = view.readCommand();

    assertEquals("test command", cmd);
    String output = systemOutCaptured.toString();
    assertTrue(output.contains("Render test." + System.lineSeparator()));
    assertTrue(output.endsWith("> "));
  }

  /**
   * Tests processing a single valid 'create_event' command followed by 'exit'.
   */
  @Test
  public void singleValidCommandThenExit() {
    String inputs = "create_event Breakfast 2025-11-05T10:00:00 2025-11-05T11:00:00"
            + System.lineSeparator() + "exit" + System.lineSeparator();
    provideInput(inputs);
    InteractiveView view = new InteractiveView();
    TestInteractiveCommandProcessor proc = new TestInteractiveCommandProcessor(calendarModel, view);
    InteractiveAppSimulator appSim = new InteractiveAppSimulator(proc, view);
    appSim.run();

    String output = systemOutCaptured.toString();
    assertTrue(output.contains("> [OK] Event 'Breakfast' created."));
    assertTrue(output.contains("> Application exiting."));
  }

  /**
   * Tests how the system handles an unknown command followed by 'exit'.
   */
  @Test
  public void invalidCommandSyntaxThenExit() {
    String inputs = "invalid_command_here" + System.lineSeparator() +
            "exit" + System.lineSeparator();
    provideInput(inputs);
    InteractiveView view = new InteractiveView();
    TestInteractiveCommandProcessor proc = new TestInteractiveCommandProcessor(calendarModel, view);
    InteractiveAppSimulator appSim = new InteractiveAppSimulator(proc, view);
    appSim.run();

    String output = systemOutCaptured.toString();
    String errorMsg = "> [ERROR] Unknown or invalid command: invalid_command_here";
    assertTrue(output.contains(errorMsg));
    assertTrue(output.contains("> Application exiting."));
  }

  /**
   * Tests handling of a command that causes a DuplicateEventException before exiting.
   */
  @Test
  public void commandCausingDuplicateEventErrorThenExit() {
    String eventName = "DuplicateEventTest"; // Specific name for clarity
    String eventTime = "2025-11-05T10:00:00 2025-11-05T11:00:00";
    String createCmd = "create_event " + eventName + " " + eventTime;

    String inputs = createCmd + System.lineSeparator() + // First creation
            createCmd + System.lineSeparator() + // Attempt duplicate
            "exit" + System.lineSeparator();
    provideInput(inputs);
    InteractiveView view = new InteractiveView();
    TestInteractiveCommandProcessor proc = new TestInteractiveCommandProcessor(calendarModel, view);
    InteractiveAppSimulator appSim = new InteractiveAppSimulator(proc, view);
    appSim.run();
    String output = systemOutCaptured.toString();
    assertTrue(output.contains("> [OK] Event '" + eventName + "' created."));
    String expectedErrorMsg = String.format("Event '%s' already exists.", eventName);
    assertTrue(output.contains("> [ERROR] " + expectedErrorMsg));
    assertTrue(output.contains("> Application exiting."));
  }

  /**
   * Tests the system's response to an empty input line followed by 'exit'.
   */
  @Test
  public void emptyInputLineThenExit() {
    String inputs = "" + System.lineSeparator() + // Represents an empty line
            "exit" + System.lineSeparator();
    provideInput(inputs);
    InteractiveView view = new InteractiveView();
    TestInteractiveCommandProcessor proc = new TestInteractiveCommandProcessor(calendarModel, view);
    InteractiveAppSimulator appSim = new InteractiveAppSimulator(proc, view);
    appSim.run();

    String output = systemOutCaptured.toString();
    assertTrue(output.contains("> [ERROR] Empty command received."));
    assertTrue(output.contains("> Application exiting."));
  }

  /**
   * Tests behavior when the input stream is closed prematurely (no 'exit' command).
   */
  @Test
  public void noInputBeforeExitLeadsToInputStreamClosed() {
    String inputs = ""; // No input
    provideInput(inputs);
    InteractiveView view = new InteractiveView();
    TestInteractiveCommandProcessor proc = new TestInteractiveCommandProcessor(calendarModel, view);
    InteractiveAppSimulator appSim = new InteractiveAppSimulator(proc, view);
    appSim.run();

    String output = systemOutCaptured.toString();
    assertTrue(output.contains("> Error: Input stream closed."));
  }

  /**
   * Tests that creating two overlapping events (different subjects) is successful.
   */
  @Test
  public void createOverlappingEventsSuccessfully() {
    String cmd1 = "create_event Breakfast 2025-11-10T10:00:00 2025-11-10T12:00:00";
    String cmd2 = "create_event Lunch 2025-11-10T11:00:00 2025-11-10T13:00:00";
    String inputs = cmd1 + System.lineSeparator() + cmd2 + System.lineSeparator()
            + "exit" + System.lineSeparator();
    provideInput(inputs);
    InteractiveView view = new InteractiveView();
    TestInteractiveCommandProcessor proc = new TestInteractiveCommandProcessor(calendarModel, view);
    InteractiveAppSimulator appSim = new InteractiveAppSimulator(proc, view);
    appSim.run();

    String output = systemOutCaptured.toString();
    assertTrue(output.contains("> [OK] Event 'Breakfast' created."));
    assertTrue(output.contains("> [OK] Event 'Lunch' created."));
    assertTrue(output.contains("> Application exiting."));

    LocalDateTime s1 = LocalDateTime.parse("2025-11-10T10:00:00");
    LocalDateTime e1 = LocalDateTime.parse("2025-11-10T12:00:00");
    LocalDateTime s2 = LocalDateTime.parse("2025-11-10T11:00:00");
    LocalDateTime e2 = LocalDateTime.parse("2025-11-10T13:00:00");
    assertNotNull(calendarModel.getEventById(new EventId("Breakfast", s1, e1)));
    assertNotNull(calendarModel.getEventById(new EventId("Lunch", s2, e2)));
  }

  /**
   * Tests that 'show_status_at' on an empty calendar reports Available.
   */
  @Test
  public void showStatusWhenNoEventsIsAvailable() {
    String inputs = "show_status_at 2025-11-05T10:00:00" + System.lineSeparator()
            + "exit" + System.lineSeparator();
    provideInput(inputs);
    InteractiveView view = new InteractiveView();
    TestInteractiveCommandProcessor proc = new TestInteractiveCommandProcessor(calendarModel, view);
    InteractiveAppSimulator appSim = new InteractiveAppSimulator(proc, view);
    appSim.run();

    String output = systemOutCaptured.toString();
    assertTrue(output.contains("> Status at 2025-11-05T10:00:00: Available"));
    assertTrue(output.contains("> Application exiting."));
  }


  /**
   * Tests that 'show_status_at' with invalid datetime format returns an error.
   */
  @Test
  public void showStatusWithInvalidDateTimeFormatThenExit() {
    String raw = "show_status_at 2025-11-05 10:00:00";
    String inputs = raw + System.lineSeparator()
            + "exit" + System.lineSeparator();
    provideInput(inputs);
    InteractiveView view = new InteractiveView();
    TestInteractiveCommandProcessor proc = new TestInteractiveCommandProcessor(calendarModel, view);
    InteractiveAppSimulator appSim = new InteractiveAppSimulator(proc, view);
    appSim.run();

    String output = systemOutCaptured.toString();
    assertTrue(output.contains("> [ERROR] Unknown or invalid command: " + raw));
    assertTrue(output.contains("> Application exiting."));
  }


  /**
   * Tests that 'create_event' with invalid datetime strings returns an error.
   */
  @Test
  public void createEventWithInvalidDateTimeThenExit() {
    String raw = "create_event Meeting 2025-11-05 10:00:00 2025-11-05T11:00:00";
    String inputs = raw + System.lineSeparator() + "exit" + System.lineSeparator();
    provideInput(inputs);
    InteractiveView view = new InteractiveView();
    TestInteractiveCommandProcessor proc = new TestInteractiveCommandProcessor(calendarModel, view);
    InteractiveAppSimulator appSim = new InteractiveAppSimulator(proc, view);
    appSim.run();

    String output = systemOutCaptured.toString();
    assertTrue(output.contains("> [ERROR] Unknown or invalid command: " + raw));
    assertTrue(output.contains("> Application exiting."));
  }


  /**
   * Tests that 'create_event' with missing arguments returns an unknown/invalid command error.
   */
  @Test
  public void createEventMissingArgsThenExit() {
    String inputs = "create_event Lunch 2025-11-05T10:00:00" + System.lineSeparator()
            + "exit" + System.lineSeparator();
    provideInput(inputs);
    InteractiveView view = new InteractiveView();
    TestInteractiveCommandProcessor proc = new TestInteractiveCommandProcessor(calendarModel, view);
    InteractiveAppSimulator appSim = new InteractiveAppSimulator(proc, view);
    appSim.run();

    String output = systemOutCaptured.toString();
    assertTrue(output.contains("> [ERROR] Unknown or invalid command: "
            + "create_event Lunch 2025-11-05T10:00:00"));
    assertTrue(output.contains("> Application exiting."));
  }

  /**
   * Tests that whitespace-only input lines are treated as empty commands.
   */
  @Test
  public void whitespaceOnlyInputThenExit() {
    String inputs = "   " + System.lineSeparator()
            + "exit" + System.lineSeparator();
    provideInput(inputs);
    InteractiveView view = new InteractiveView();
    TestInteractiveCommandProcessor proc = new TestInteractiveCommandProcessor(calendarModel, view);
    InteractiveAppSimulator appSim = new InteractiveAppSimulator(proc, view);
    appSim.run();

    String output = systemOutCaptured.toString();
    assertTrue(output.contains("> [ERROR] Empty command received."));
    assertTrue(output.contains("> Application exiting."));
  }

  /**
   * Tests that 'create_event' subject with special characters is accepted.
   */
  @Test
  public void createEventWithSpecialCharactersInSubject() {
    String cmd = "create_event Special!@# 2025-11-05T10:00:00 2025-11-05T11:00:00";
    String inputs = cmd + System.lineSeparator() + "exit" + System.lineSeparator();
    provideInput(inputs);
    InteractiveView view = new InteractiveView();
    TestInteractiveCommandProcessor proc = new TestInteractiveCommandProcessor(calendarModel, view);
    InteractiveAppSimulator appSim = new InteractiveAppSimulator(proc, view);
    appSim.run();

    String output = systemOutCaptured.toString();
    assertTrue(output.contains("> [OK] Event 'Special!@#' created."));
    assertTrue(output.contains("> Application exiting."));

    LocalDateTime s = LocalDateTime.parse("2025-11-05T10:00:00");
    LocalDateTime e = LocalDateTime.parse("2025-11-05T11:00:00");
    assertNotNull(calendarModel.getEventById(new EventId("Special!@#", s, e)));
  }

  /**
   * Tests that 'create_event' with extra whitespace between tokens is accepted.
   */
  @Test
  public void createEventWithExtraWhitespaceThenExit() {
    String cmd = "create_event    Espresso   2025-11-05T10:00:00    2025-11-05T11:00:00";
    String inputs = cmd + System.lineSeparator() + "exit" + System.lineSeparator();
    provideInput(inputs);
    InteractiveView view = new InteractiveView();
    TestInteractiveCommandProcessor proc = new TestInteractiveCommandProcessor(calendarModel, view);
    InteractiveAppSimulator appSim = new InteractiveAppSimulator(proc, view);
    appSim.run();

    String output = systemOutCaptured.toString();
    assertTrue(output.contains("> [OK] Event 'Espresso' created."));
    assertTrue(output.contains("> Application exiting."));

    LocalDateTime s = LocalDateTime.parse("2025-11-05T10:00:00");
    LocalDateTime e = LocalDateTime.parse("2025-11-05T11:00:00");
    assertNotNull(calendarModel.getEventById(new EventId("Espresso", s, e)));
  }

  @Test
  public void createEventEndBeforeStartShowsError() {
    String cmd = "create_event Reversed 2025-11-05T11:00:00 2025-11-05T10:00:00";
    String inputs = cmd + System.lineSeparator() + "exit" + System.lineSeparator();
    provideInput(inputs);
    InteractiveView view = new InteractiveView();
    TestInteractiveCommandProcessor proc = new TestInteractiveCommandProcessor(calendarModel, view);
    InteractiveAppSimulator appSim = new InteractiveAppSimulator(proc, view);
    appSim.run();
    String output = systemOutCaptured.toString();
    assertTrue(output.contains("> [ERROR]"));
  }

  @Test
  public void commandWithUppercaseAccepted() {
    String cmd = "CREATE_EVENT Dinner 2025-12-01T18:00:00 2025-12-01T19:00:00";
    String inputs = cmd + System.lineSeparator() + "EXIT" + System.lineSeparator();
    provideInput(inputs);
    InteractiveView view = new InteractiveView();
    TestInteractiveCommandProcessor proc = new TestInteractiveCommandProcessor(calendarModel, view);
    InteractiveAppSimulator appSim = new InteractiveAppSimulator(proc, view);
    appSim.run();

    String output = systemOutCaptured.toString();
    assertTrue(output.contains("> [OK] Event 'Dinner' created."));
    assertTrue(output.contains("> Application exiting."));

    LocalDateTime s = LocalDateTime.parse("2025-12-01T18:00:00");
    LocalDateTime e = LocalDateTime.parse("2025-12-01T19:00:00");
    assertNotNull(calendarModel.getEventById(new EventId("Dinner", s, e)));
  }

  @Test
  public void showStatusWithNoArgsReturnsError() {
    String raw = "show_status_at";
    String inputs = raw + System.lineSeparator() + "exit" + System.lineSeparator();
    provideInput(inputs);
    InteractiveView view = new InteractiveView();
    TestInteractiveCommandProcessor proc = new TestInteractiveCommandProcessor(calendarModel, view);
    InteractiveAppSimulator appSim = new InteractiveAppSimulator(proc, view);
    appSim.run();
    String output = systemOutCaptured.toString();
    assertTrue(output.contains("> [ERROR] Unknown or invalid command: " + raw));
    assertTrue(output.contains("> Application exiting."));
  }

  @Test
  public void showStatusWithTooManyArgsReturnsError() {
    String raw = "show_status_at 2025-11-01T10:00:00 extraArg";
    String inputs = raw + System.lineSeparator() + "exit" + System.lineSeparator();
    provideInput(inputs);
    InteractiveView view = new InteractiveView();
    TestInteractiveCommandProcessor proc = new TestInteractiveCommandProcessor(calendarModel, view);
    InteractiveAppSimulator appSim = new InteractiveAppSimulator(proc, view);
    appSim.run();
    String output = systemOutCaptured.toString();
    assertTrue(output.contains("> [ERROR] Unknown or invalid command: " + raw));
    assertTrue(output.contains("> Application exiting."));
  }

  @Test
  public void createEventWithEmptySubjectShowsError() {
    String cmd = "create_event  2025-11-20T09:00:00 2025-11-20T10:00:00"; // subject missing
    String inputs = cmd + System.lineSeparator() + "exit" + System.lineSeparator();
    provideInput(inputs);
    InteractiveView view = new InteractiveView();
    TestInteractiveCommandProcessor proc = new TestInteractiveCommandProcessor(calendarModel, view);
    InteractiveAppSimulator appSim = new InteractiveAppSimulator(proc, view);
    appSim.run();
    String output = systemOutCaptured.toString();
    assertTrue(output.contains("> [ERROR] Unknown or invalid command: " + cmd));
    assertTrue(output.contains("> Application exiting."));
  }

  @Test
  public void createEventWithWhitespaceOnlySubjectShowsError() {
    String cmd = "create_event   \"   \" 2025-11-20T09:00:00 2025-11-20T10:00:00";
    String inputs = cmd + System.lineSeparator() + "exit" + System.lineSeparator();
    provideInput(inputs);
    InteractiveView view = new InteractiveView();
    TestInteractiveCommandProcessor proc = new TestInteractiveCommandProcessor(calendarModel, view);
    InteractiveAppSimulator appSim = new InteractiveAppSimulator(proc, view);
    appSim.run();
    String output = systemOutCaptured.toString();
    assertTrue(output.contains("[ERROR]"));
  }


  @Test
  public void createEventSameSubjectDifferentTimesAllowed() {
    String cmd1 = "create_event Math 2025-11-30T10:00:00 2025-11-30T11:00:00";
    String cmd2 = "create_event Math 2025-11-30T12:00:00 2025-11-30T13:00:00";
    String inputs = cmd1 + System.lineSeparator() + cmd2 + System.lineSeparator()
            + "exit" + System.lineSeparator();
    provideInput(inputs);
    InteractiveView view = new InteractiveView();
    TestInteractiveCommandProcessor proc = new TestInteractiveCommandProcessor(calendarModel, view);
    InteractiveAppSimulator appSim = new InteractiveAppSimulator(proc, view);
    appSim.run();

    String output = systemOutCaptured.toString();
    assertTrue(output.contains("> [OK] Event 'Math' created."));
    int count = output.split("> \\[OK\\] Event 'Math' created\\.").length - 1;
    assertEquals(2, count);
    assertTrue(output.contains("> Application exiting."));

    LocalDateTime s1 = LocalDateTime.parse("2025-11-30T10:00:00");
    LocalDateTime e1 = LocalDateTime.parse("2025-11-30T11:00:00");
    LocalDateTime s2 = LocalDateTime.parse("2025-11-30T12:00:00");
    LocalDateTime e2 = LocalDateTime.parse("2025-11-30T13:00:00");
    assertNotNull(calendarModel.getEventById(new EventId("Math", s1, e1)));
    assertNotNull(calendarModel.getEventById(new EventId("Math", s2, e2)));
  }

  @Test
  public void createEventWithTabAndSpacesAccepted() {
    String cmd = "   create_event\tTest\t2025-11-28T08:00:00\t2025-11-28T09:00:00   ";
    String inputs = cmd + System.lineSeparator() + "exit" + System.lineSeparator();
    provideInput(inputs);
    InteractiveView view = new InteractiveView();
    TestInteractiveCommandProcessor proc = new TestInteractiveCommandProcessor(calendarModel, view);
    InteractiveAppSimulator appSim = new InteractiveAppSimulator(proc, view);
    appSim.run();

    String output = systemOutCaptured.toString();
    assertTrue(output.contains("> [OK] Event 'Test' created."));
    assertTrue(output.contains("> Application exiting."));

    LocalDateTime s = LocalDateTime.parse("2025-11-28T08:00:00");
    LocalDateTime e = LocalDateTime.parse("2025-11-28T09:00:00");
    assertNotNull(calendarModel.getEventById(new EventId("Test", s, e)));
  }


  @Test
  public void createEventWithVeryLongSubject() {
    String longSubj = "S".repeat(300);
    String cmd = "create_event " + longSubj + " 2025-12-02T09:00:00 2025-12-02T10:00:00";
    String inputs = cmd + System.lineSeparator() + "exit" + System.lineSeparator();
    provideInput(inputs);
    InteractiveView view = new InteractiveView();
    TestInteractiveCommandProcessor proc = new TestInteractiveCommandProcessor(calendarModel, view);
    InteractiveAppSimulator appSim = new InteractiveAppSimulator(proc, view);
    appSim.run();
    String output = systemOutCaptured.toString();
    assertTrue(output.contains(longSubj));
  }

}
