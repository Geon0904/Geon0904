package integration;

import calendarapp.model.CalendarImpl;
import calendarapp.model.CalendarModel;
import calendarapp.model.Event;
import calendarapp.view.HeadlessView;
import calendarapp.exceptions.DuplicateEventException;
import calendarapp.model.EventId;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;


/**
 * Tests simulating headless mode execution using a simplified command processor and view.
 */
public class HeadlessExecutionTest {

  private HeadlessView headlessView;
  private CalendarModel calendarModel;
  private final PrintStream originalSystemOut = System.out;
  private ByteArrayOutputStream systemOutContent;

  // Test helper class for simplified command processing
  static class TestCommandProcessor {
    private CalendarModel model;
    private HeadlessView view;
    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DF = DateTimeFormatter.ISO_LOCAL_DATE;


    public TestCommandProcessor(CalendarModel model, HeadlessView view) {
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
        if ("create_event".equals(cmd) && parts.length == 4) {
          String subject = parts[1];
          LocalDateTime start = LocalDateTime.parse(parts[2], DTF);
          LocalDateTime end = LocalDateTime.parse(parts[3], DTF);
          model.addEvent(new Event(subject, start, end));
          view.render("[OK] Event '" + subject + "' created.");
        } else if ("print_events_on".equals(cmd) && parts.length == 2) {
          LocalDate date = LocalDate.parse(parts[1], DF);
          String msg = "[INFO] Events on " + parts[1] + ": "
                  + model.getEventsOn(date).size() + " found.";
          view.render(msg);
        } else {
          view.render("[ERROR] Unknown or invalid command: " + commandStr);
        }
      } catch (DateTimeParseException e) {
        view.render("[ERROR] Invalid date/time format in command: " + commandStr);
      } catch (DuplicateEventException e) {
        view.render("[ERROR] " + e.getMessage());
      } catch (IllegalArgumentException e) {
        view.render("[ERROR] " + e.getMessage());
      } catch (Exception e) {
        view.render("[ERROR] Unexpected error processing command: " + e.getMessage());
      }
    }
  }

  // Test helper class for running the application in a simulated headless mode
  static class HeadlessAppRunner {
    public static void run(Path commandFile, TestCommandProcessor processor,
                           HeadlessView view) throws IOException {
      List<String> commands = Files.readAllLines(commandFile);
      boolean exitFound = false;
      if (commands.isEmpty()) {
        System.out.println("Error: Command file must end with an 'exit' command.");
        return;
      }

      for (String command : commands) {
        if ("exit".equalsIgnoreCase(command.trim())) {
          exitFound = true;
          view.render("Application exiting.");
          break;
        }
        processor.processCommand(command);
      }

      if (!exitFound) {
        System.out.println("Error: Command file must end with an 'exit' command.");
      }
    }
  }

  /**
   * Sets up view, model, and output stream capture.
   */
  @Before
  public void setUp() {
    headlessView = new HeadlessView();
    calendarModel = new CalendarImpl();
    systemOutContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(systemOutContent));
  }

  /**
   * Restores original System.out.
   */
  @After
  public void tearDown() {
    System.setOut(originalSystemOut);
  }

  private Path createTempCommandFile(String... commands) throws IOException {
    Path tempFile = Files.createTempFile("headless_cmds_", ".txt");
    Files.write(tempFile, Arrays.asList(commands));
    tempFile.toFile().deleteOnExit(); // Ensure cleanup
    return tempFile;
  }

  /**
   * Tests processing of a valid command file that correctly ends with an 'exit' command.
   */
  @Test
  public void validCommandsEndWithExit() throws IOException {
    Path file = createTempCommandFile(
            "create_event Breakfast 2025-07-20T10:00:00 2025-07-20T11:00:00",
            "print_events_on 2025-07-20",
            "exit"
    );
    TestCommandProcessor proc = new TestCommandProcessor(calendarModel, headlessView);
    HeadlessAppRunner.run(file, proc, headlessView);

    List<String> logs = headlessView.getLogs();
    assertTrue(logs.contains("[OK] Event 'Breakfast' created."));
    assertTrue(logs.contains("[INFO] Events on 2025-07-20: 1 found."));
    assertTrue(logs.contains("Application exiting."));
    assertEquals("Console output should be empty if exit is found",
            "", systemOutContent.toString().trim());
  }

  /**
   * Tests a command file with an invalid command, then a valid one, then 'exit'.
   */
  @Test
  public void fileWithInvalidCommandThenValidThenExit() throws IOException {
    Path file = createTempCommandFile(
            "create_event Lunch 2025-08-01T09:00:00 2025-08-01T10:00:00",
            "some_invalid_command_format",
            "print_events_on 2025-08-01",
            "exit"
    );
    TestCommandProcessor proc = new TestCommandProcessor(calendarModel, headlessView);
    HeadlessAppRunner.run(file, proc, headlessView);

    List<String> logs = headlessView.getLogs();
    assertTrue(logs.contains("[OK] Event 'Lunch' created."));
    String errStr = "[ERROR] Unknown or invalid command: some_invalid_command_format";
    assertTrue(logs.stream().anyMatch(s -> s.contains(errStr)));
    assertTrue(logs.contains("[INFO] Events on 2025-08-01: 1 found."));
    assertTrue(logs.contains("Application exiting."));
  }

  /**
   * Tests error message to console if the command file does not end with 'exit'.
   */
  @Test
  public void fileMissingExitCommandPrintsErrorToConsole() throws IOException {
    Path file = createTempCommandFile(
            "create_event Dinner 2025-09-10T14:00:00 2025-09-10T15:00:00"
    ); // No exit command
    TestCommandProcessor proc = new TestCommandProcessor(calendarModel, headlessView);
    HeadlessAppRunner.run(file, proc, headlessView);

    String consoleMsg = "Error message for missing exit not found in console output.";
    assertTrue(consoleMsg, systemOutContent.toString()
            .contains("Error: Command file must end with an 'exit' command."));
    List<String> logs = headlessView.getLogs();
    assertTrue(logs.contains("[OK] Event 'Dinner' created."));
    assertFalse(logs.contains("Application exiting."));
  }

  /**
   * Tests behavior with an empty command file; expects missing exit error.
   */
  @Test
  public void emptyCommandFile() throws IOException {
    Path file = createTempCommandFile(); // Creates an empty file
    TestCommandProcessor proc = new TestCommandProcessor(calendarModel, headlessView);
    HeadlessAppRunner.run(file, proc, headlessView);

    assertTrue("Logs should be empty as no commands were processed.",
            headlessView.getLogs().isEmpty());
    String consoleMsg = "Error message for missing exit not found for empty file.";
    assertTrue(consoleMsg, systemOutContent.toString()
            .contains("Error: Command file must end with an 'exit' command."));
  }

  /**
   * Tests processing a command file that contains only the 'exit' command.
   */
  @Test
  public void fileWithOnlyExitCommand() throws IOException {
    Path file = createTempCommandFile("exit");
    TestCommandProcessor proc = new TestCommandProcessor(calendarModel, headlessView);
    HeadlessAppRunner.run(file, proc, headlessView);

    List<String> logs = headlessView.getLogs();
    assertEquals(1, logs.size());
    assertEquals("Application exiting.", logs.get(0));
    assertEquals("Console output should be empty as exit command was found.",
            "", systemOutContent.toString().trim());
  }

  /**
   * Tests that model exceptions (like DuplicateEventException) are caught and logged.
   */
  @Test
  public void commandCausingDuplicateExceptionIsLogged() throws IOException {
    String eventName = "DuplicateEventTest"; // Specific name
    Path file = createTempCommandFile(
            "create_event " + eventName + " 2025-10-01T10:00:00 2025-10-01T11:00:00",
            "create_event " + eventName + " 2025-10-01T10:00:00 2025-10-01T11:00:00",
            "exit"
    );
    TestCommandProcessor proc = new TestCommandProcessor(calendarModel, headlessView);
    HeadlessAppRunner.run(file, proc, headlessView);

    List<String> logs = headlessView.getLogs();
    assertTrue(logs.contains("[OK] Event '" + eventName + "' created."));
    String expectedErrorMsg = String.format("Event '%s' already exists.", eventName);
    assertTrue(logs.stream().anyMatch(s -> s.contains("[ERROR] " + expectedErrorMsg)));
    assertTrue(logs.contains("Application exiting."));
  }

  /**
   * Verifies that the list returned by getLogs() from HeadlessView is unmodifiable.
   */
  @Test(expected = UnsupportedOperationException.class)
  public void getLogsIsUnmodifiable() {
    headlessView.render("Log 1");
    List<String> logs = headlessView.getLogs();
    logs.add("Attempt to modify"); // Should throw
    fail("Expected UnsupportedOperationException when trying to modify logs list.");
  }

  /**
   * Tests that the clear() method in HeadlessView correctly empties the logs.
   */
  @Test
  public void clearLogsWorks() {
    headlessView.render("Log A");
    assertFalse(headlessView.getLogs().isEmpty());
    headlessView.clear();
    assertTrue(headlessView.getLogs().isEmpty());
  }

  /**
   * Tests creating two overlapping events (different subjects) in headless mode.
   */
  @Test
  public void createOverlappingEventsSuccessfullyHeadless() throws IOException {
    Path file = createTempCommandFile(
            "create_event Breakfast 2025-11-10T10:00:00 2025-11-10T12:00:00",
            "create_event Lunch 2025-11-10T11:00:00 2025-11-10T13:00:00", // Overlaps
            "exit"
    );
    TestCommandProcessor proc = new TestCommandProcessor(calendarModel, headlessView);
    HeadlessAppRunner.run(file, proc, headlessView);

    List<String> logs = headlessView.getLogs();
    assertTrue(logs.contains("[OK] Event 'Breakfast' created."));
    assertTrue(logs.contains("[OK] Event 'Lunch' created."));
    assertTrue(logs.contains("Application exiting."));
    assertEquals("", systemOutContent.toString().trim());

    LocalDateTime s1 = LocalDateTime.parse("2025-11-10T10:00:00");
    LocalDateTime e1 = LocalDateTime.parse("2025-11-10T12:00:00");
    LocalDateTime s2 = LocalDateTime.parse("2025-11-10T11:00:00");
    LocalDateTime e2 = LocalDateTime.parse("2025-11-10T13:00:00");
    assertNotNull(calendarModel.getEventById(new EventId("Breakfast", s1, e1)));
    assertNotNull(calendarModel.getEventById(new EventId("Lunch", s2, e2)));
  }

  @Test
  public void createEventWithEndBeforeStartLogsError() throws IOException {
    Path file = createTempCommandFile(
            "create_event Wrong 2025-07-20T11:00:00 2025-07-20T10:00:00",
            "exit"
    );
    TestCommandProcessor proc = new TestCommandProcessor(calendarModel, headlessView);
    HeadlessAppRunner.run(file, proc, headlessView);
    assertTrue(headlessView.getLogs().stream().anyMatch(s -> s.contains("[ERROR]")));
  }

  @Test
  public void commandsWithUppercaseOrMixedCaseAccepted() throws IOException {
    Path file = createTempCommandFile(
            "CREATE_EVENT Brunch 2025-12-20T10:00:00 2025-12-20T11:00:00",
            "PRINT_EVENTS_ON 2025-12-20",
            "ExIt"
    );
    TestCommandProcessor proc = new TestCommandProcessor(calendarModel, headlessView);
    HeadlessAppRunner.run(file, proc, headlessView);

    List<String> logs = headlessView.getLogs();
    assertTrue(logs.contains("[OK] Event 'Brunch' created."));
    assertTrue(logs.stream().anyMatch(s -> s.contains("[INFO] Events on " +
            "2025-12-20: 1 found.")));
    assertTrue(logs.contains("Application exiting."));
  }

  @Test
  public void createEventsSameSubjectDifferentTimesAllowed() throws IOException {
    Path file = createTempCommandFile(
            "create_event Meeting 2025-12-22T10:00:00 2025-12-22T11:00:00",
            "create_event Meeting 2025-12-22T12:00:00 2025-12-22T13:00:00",
            "print_events_on 2025-12-22",
            "exit"
    );
    TestCommandProcessor proc = new TestCommandProcessor(calendarModel, headlessView);
    HeadlessAppRunner.run(file, proc, headlessView);

    List<String> logs = headlessView.getLogs();
    assertTrue(logs.contains("[OK] Event 'Meeting' created."));

    assertTrue(logs.stream().anyMatch(s -> s.contains("[INFO] Events on 2025-12-22: " +
            "2 found.")));
  }

  @Test
  public void createEventMissingArgumentsLogsError() throws IOException {
    Path file = createTempCommandFile(
            "create_event OnlySubject 2025-11-01T10:00:00",
            "exit"
    );
    TestCommandProcessor proc = new TestCommandProcessor(calendarModel, headlessView);
    HeadlessAppRunner.run(file, proc, headlessView);
    assertTrue(headlessView.getLogs().stream().anyMatch(s -> s.contains("[ERROR]")));
  }

  @Test
  public void printEventsOnWithInvalidDateLogsError() throws IOException {
    Path file = createTempCommandFile(
            "print_events_on 2025-13-99",
            "exit"
    );
    TestCommandProcessor proc = new TestCommandProcessor(calendarModel, headlessView);
    HeadlessAppRunner.run(file, proc, headlessView);
    assertTrue(headlessView.getLogs().stream().anyMatch(s -> s.contains("[ERROR]")));
  }

  @Test
  public void commandFileWithBlankLinesAndTabsHandledGracefully() throws IOException {
    Path file = createTempCommandFile(
            "  ",
            "create_event Spaced 2025-11-05T10:00:00 2025-11-05T11:00:00",
            "\t",
            "print_events_on 2025-11-05",
            "",
            "exit"
    );
    TestCommandProcessor proc = new TestCommandProcessor(calendarModel, headlessView);
    HeadlessAppRunner.run(file, proc, headlessView);

    List<String> logs = headlessView.getLogs();
    assertTrue(logs.contains("[OK] Event 'Spaced' created."));
    assertTrue(logs.stream().anyMatch(s -> s.contains("[INFO] Events on 2025-11-05: 1 " +
            "found.")));
    assertTrue(logs.contains("Application exiting."));
  }

  @Test
  public void multipleExitCommandsStopsAtFirstExit() throws IOException {
    Path file = createTempCommandFile(
            "create_event EarlyExit 2025-11-06T09:00:00 2025-11-06T10:00:00",
            "exit",
            "create_event ShouldNotRun 2025-11-06T10:00:00 2025-11-06T11:00:00"
    );
    TestCommandProcessor proc = new TestCommandProcessor(calendarModel, headlessView);
    HeadlessAppRunner.run(file, proc, headlessView);

    List<String> logs = headlessView.getLogs();
    assertTrue(logs.contains("[OK] Event 'EarlyExit' created."));
    assertTrue(logs.contains("Application exiting."));

    assertFalse(logs.stream().anyMatch(s -> s.contains("ShouldNotRun")));
  }

  @Test
  public void multipleInvalidCommandsThenValid() throws IOException {
    Path file = createTempCommandFile(
            "bad_cmd1",
            "bad_cmd2",
            "create_event Valid 2025-10-01T10:00:00 2025-10-01T11:00:00",
            "exit"
    );
    TestCommandProcessor proc = new TestCommandProcessor(calendarModel, headlessView);
    HeadlessAppRunner.run(file, proc, headlessView);
    List<String> logs = headlessView.getLogs();
    assertTrue(logs.contains("[OK] Event 'Valid' created."));
    assertTrue(logs.contains("Application exiting."));
  }

  @Test
  public void createEventSubjectOnlyNumbers() throws IOException {
    Path file = createTempCommandFile(
            "create_event 12345 2025-12-01T10:00:00 2025-12-01T11:00:00",
            "exit"
    );
    TestCommandProcessor proc = new TestCommandProcessor(calendarModel, headlessView);
    HeadlessAppRunner.run(file, proc, headlessView);
    assertTrue(headlessView.getLogs().stream().anyMatch(s -> s.contains("12345")));
  }



}