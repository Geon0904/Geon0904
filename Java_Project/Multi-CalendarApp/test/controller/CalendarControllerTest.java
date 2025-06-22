package controller;


import org.junit.Before;
import org.junit.Test;


import calendarapp.controller.CalendarController;
import calendarapp.controller.commands.CalendarCommand;
import calendarapp.model.CalendarImpl;
import calendarapp.model.CalendarModel;
import calendarapp.model.Event;
import calendarapp.model.EventId;
import calendarapp.model.EventSeries;
import calendarapp.model.MultiCalendarModel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the CalendarController class.
 * Verifies that handleCommand properly executes commands and handles edge cases.
 */
public class CalendarControllerTest {

  private CalendarController controller;

  @Before
  public void setUp() {
    // Dummy MultiCalendarModel that forwards to an inner CalendarModel stub:
    MultiCalendarModel stub = new MultiCalendarModel() {
      private final CalendarModel inner = new CalendarImpl();

      @Override
      public void createCalendar(String name, ZoneId tz) { /* no-op */ }

      @Override
      public void useCalendar(String name) { /* no-op */ }

      @Override
      public CalendarModel getActiveCalendar() {
        return inner;
      }

      @Override
      public void editCalendar(String name, String property, String newValue) { /* no-op */ }

      //delegate all CalendarModel methods to inner
      @Override
      public void addEvent(Event event) {
        inner.addEvent(event);
      }

      @Override
      public void addEventSeries(EventSeries s) {
        inner.addEventSeries(s);
      }

      @Override
      public List<Event> getEventsOn(LocalDate d) {
        return inner.getEventsOn(d);
      }

      @Override
      public List<Event> getEventsBetween(LocalDateTime f, LocalDateTime t) {
        return inner.getEventsBetween(f, t);
      }

      @Override
      public boolean isBusyAt(LocalDateTime dt) {
        return inner.isBusyAt(dt);
      }

      @Override
      public void editEvent(EventId id, Event e) {
        inner.editEvent(id, e);
      }

      @Override
      public void editEventsFrom(EventId id, String p, String v) {
        inner.editEventsFrom(id, p, v);
      }

      @Override
      public void editSeries(String s, LocalDateTime t, String p, String v) {
        inner.editSeries(s, t, p, v);
      }

      @Override
      public Event getEventById(EventId id) {
        return inner.getEventById(id);
      }


      @Override
      public List<String> getCalendarNames() {
        return java.util.Collections.singletonList("Dummy");
      }

      @Override
      public String getActiveCalendarName() {
        return "Dummy";
      }


      // and stub out your new copy methods if needed:
      @Override
      public int copyEvent(String subj, LocalDateTime src, String dstCal,
                           LocalDateTime dst) {
        return 0;
      }

      @Override
      public int copyEventsOn(LocalDate src, String dstCal, LocalDate dst) {
        return 0;
      }

      @Override
      public int copyEventsBetween(LocalDate f, LocalDate t, String dstCal, LocalDate d0) {
        return 0;
      }

      @Override
      public List<Event> getEventsFrom(LocalDate start, int maxCount) {
        return java.util.Collections.emptyList();
      }

      @Override
      public void deleteEvent(calendarapp.model.EventId id) {
        //do nothing
      }

    };

    controller = new CalendarController(stub);
  }


  //Dummy command implementation for testing
  private static class DummyCommand implements CalendarCommand {
    private final String message;

    public DummyCommand(String message) {
      this.message = message;
    }

    @Override
    public String execute(calendarapp.model.CalendarModel model) {
      return message;
    }
  }

  @Test
  //Verifies that a normal command's return value is passed through unchanged.
  public void testHandleCommandReturnsResult() {
    CalendarCommand cmd = new DummyCommand("Hello");
    assertEquals("Hello", controller.handleCommand(cmd));
  }

  @Test
  //Verifies that an empty string result is preserved.
  public void testHandleCommandEmptyString() {
    CalendarCommand cmd = new DummyCommand("");
    assertEquals("", controller.handleCommand(cmd));
  }

  @Test
  //Verifies that an error-formatted string is preserved.
  public void testHandleCommandErrorString() {
    CalendarCommand cmd = new DummyCommand("[ERROR] Ooops");
    assertEquals("[ERROR] Ooops", controller.handleCommand(cmd));
  }

  @Test
  // Verifies that whitespace-only results are preserved.
  public void testHandleCommandWhitespaceOnly() {
    CalendarCommand cmd = new DummyCommand("   ");
    assertEquals("   ", controller.handleCommand(cmd));
  }

  @Test
  // Verifies that a null return value is converted to an empty string.
  public void testHandleCommandNullReturnBecomesEmpty() {
    CalendarCommand cmd = model -> null;
    assertEquals("", controller.handleCommand(cmd));
  }

  @Test
  // Verifies that multiple sequential commands work independently.
  public void testMultipleSequentialCommands() {
    CalendarCommand cmd1 = new DummyCommand("First");
    CalendarCommand cmd2 = new DummyCommand("Second");
    assertEquals("First", controller.handleCommand(cmd1));
    assertEquals("Second", controller.handleCommand(cmd2));
  }

  @Test(timeout = 50)
  // Verifies that long‐running commands still complete and return correctly.
  public void testSlowCommandExecution() {
    CalendarCommand slow = model -> {
      try {
        Thread.sleep(10);
      } catch (InterruptedException ignore) {
      }
      return "Done";
    };
    assertEquals("Done", controller.handleCommand(slow));
  }

  @Test(expected = IllegalArgumentException.class)
  // Verifies that passing null to handleCommand throws IllegalArgumentException.
  public void testHandleNullCommandThrows() {
    controller.handleCommand(null);
  }


  // Command output contains special characters
  @Test
  public void testSpecialCharacterOutput() {
    String specialOutput = "Line1\nLine2\tTabbed";
    CalendarCommand cmd = new DummyCommand(specialOutput);
    assertEquals(specialOutput, controller.handleCommand(cmd));
  }

  // Very long command output
  @Test
  public void testVeryLongOutput() {
    StringBuilder longOutput = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longOutput.append("a");
    }
    CalendarCommand cmd = new DummyCommand(longOutput.toString());
    assertEquals(longOutput.toString(), controller.handleCommand(cmd));
  }

  // Command execution after model modification
  @Test
  public void testCommandAfterModelChange() {
    CalendarModel testModel = new CalendarImpl();
    MultiCalendarModel wrapper = new MultiCalendarModel() {
      @Override
      public CalendarModel getActiveCalendar() {
        return testModel;
      }

      @Override
      public void createCalendar(String n, ZoneId z) {
        //
      }

      @Override
      public void useCalendar(String n) {
        //
      }

      @Override
      public void editCalendar(String n, String p, String v) {
        //
      }

      @Override
      public void addEvent(Event e) {
        testModel.addEvent(e);
      }

      @Override
      public void addEventSeries(EventSeries s) {
        testModel.addEventSeries(s);
      }

      @Override
      public List<Event> getEventsOn(LocalDate d) {
        return testModel.getEventsOn(d);
      }

      @Override
      public List<Event> getEventsBetween(LocalDateTime f, LocalDateTime t) {
        return testModel.getEventsBetween(f, t);
      }

      @Override
      public boolean isBusyAt(LocalDateTime dt) {
        return testModel.isBusyAt(dt);
      }

      @Override
      public void editEvent(EventId id, Event e) {
        testModel.editEvent(id, e);
      }

      @Override
      public void editEventsFrom(EventId id, String p, String v) {
        testModel.editEventsFrom(id, p, v);
      }

      @Override
      public void editSeries(String s, LocalDateTime t, String p, String v) {
        testModel.editSeries(s, t, p, v);
      }

      @Override
      public Event getEventById(EventId id) {
        return testModel.getEventById(id);
      }


      @Override
      public int copyEvent(String s, LocalDateTime st, String dc, LocalDateTime dt) {
        return 0;
      }

      @Override
      public int copyEventsOn(LocalDate s, String dc, LocalDate d) {
        return 0;
      }

      @Override
      public int copyEventsBetween(LocalDate f, LocalDate t, String dc, LocalDate d) {
        return 0;
      }

      @Override
      public List<String> getCalendarNames() {
        return java.util.Collections.singletonList("Dummy");
      }

      @Override
      public String getActiveCalendarName() {
        return "Dummy";
      }

      @Override
      public List<Event> getEventsFrom(LocalDate date, int max) {
        return java.util.Collections.emptyList();
      }

      @Override
      public void deleteEvent(calendarapp.model.EventId id) {
        //do nothing
      }

    };

    CalendarController testController = new CalendarController(wrapper);

    //First command adds event
    CalendarCommand addCmd = model -> {
      model.addEvent(new Event("Test", LocalDateTime.now(),
              LocalDateTime.now().plusHours(1), "", "", true));
      return "Added";
    };

    //Second command checks events
    CalendarCommand checkCmd = model -> {
      return model.getEventsOn(LocalDate.now()).isEmpty() ? "Empty" : "HasEvents";
    };

    testController.handleCommand(addCmd);
    assertEquals("HasEvents", testController.handleCommand(checkCmd));
  }


  @Test
  public void testSingleCharacterResult() {
    CalendarCommand cmd = new DummyCommand("!");
    assertEquals("!", controller.handleCommand(cmd));
  }


  @Test
  public void testNullThenNormal() {
    CalendarCommand nullCmd = model -> null;
    CalendarCommand normalCmd = new DummyCommand("OK");
    assertEquals("", controller.handleCommand(nullCmd));
    assertEquals("OK", controller.handleCommand(normalCmd));
  }

  // Command returns only whitespace and special unicode characters
  @Test
  public void testWhitespaceAndUnicodeResult() {
    CalendarCommand cmd = new DummyCommand("   \u2602\t\n");
    assertEquals("   \u2602\t\n", controller.handleCommand(cmd));
  }


  // Command returns a very large string (stress test, already similar one exists)
  @Test
  public void testVeryLargeOutput() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 100_000; i++) {
      sb.append('x');
    }
    CalendarCommand cmd = new DummyCommand(sb.toString());
    assertEquals(sb.toString(), controller.handleCommand(cmd));
  }

  // Multiple error-formatted results in sequence
  @Test
  public void testMultipleErrorResultsInSequence() {
    CalendarCommand c1 = new DummyCommand("[ERROR] First");
    CalendarCommand c2 = new DummyCommand("[ERROR] Second");
    assertEquals("[ERROR] First", controller.handleCommand(c1));
    assertEquals("[ERROR] Second", controller.handleCommand(c2));
  }

  // Verifies that exceptions thrown by the command are caught and returned as [ERROR] messages.
  @Test
  public void testHandleCommandReturnsErrorStringOnException() {
    CalendarCommand failing = model -> {
      throw new RuntimeException("boom");
    };
    assertEquals("[ERROR] boom", controller.handleCommand(failing));
  }

  // Command that returns null, then throws an exception on next call
  @Test
  public void testNullThenExceptionReturnsErrorString() {
    CalendarCommand nullCmd = model -> null;
    CalendarCommand failCmd = model -> {
      throw new RuntimeException("err");
    };

    assertEquals("", controller.handleCommand(nullCmd));
    assertEquals("[ERROR] err", controller.handleCommand(failCmd));
  }

  @Test
  public void testHandleCommandReturnsErrorOnIllegalArgument() {
    CalendarCommand cmd = model -> {
      throw new IllegalArgumentException("illegal argument!");
    };
    assertEquals("[ERROR] illegal argument!", controller.handleCommand(cmd));
  }


  @Test
  public void testManyFastSequentialCommands() {
    for (int i = 0; i < 1000; i++) {
      CalendarCommand cmd = new DummyCommand("seq" + i);
      assertEquals("seq" + i, controller.handleCommand(cmd));
    }
  }

}
