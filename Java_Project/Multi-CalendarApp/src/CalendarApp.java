package calendarapp;

import calendarapp.controller.CalendarController;
import calendarapp.controller.CalendarGuiController;
import calendarapp.controller.CommandParser;
import calendarapp.controller.commands.CalendarCommand;
import calendarapp.exceptions.InvalidCommandException;
import calendarapp.model.CalendarManagerImpl;
import calendarapp.model.MultiCalendarModel;
import calendarapp.view.InteractiveView;
import calendarapp.view.HeadlessView;
import calendarapp.view.View;
import calendarapp.view.SwingCalendarView;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * The main entry point for the Calendar application.
 * Supports GUI (Swing), interactive CLI, and headless/scripted modes via command-line arguments.
 */
public class CalendarApp {

  /**
   * Launches the Calendar Application in the specified mode.
   *
   * @param args command-line arguments to select mode
   */
  public static void main(String[] args) {
    if (args.length == 1 &&
            (args[0].equalsIgnoreCase("--gui")
                    || args[0].equalsIgnoreCase("-g"))) {
      runGui();
    } else if (args.length == 2 &&
            (args[0].equalsIgnoreCase("--headless")
                    || args[0].equalsIgnoreCase("-h"))) {
      runHeadless(args[1]);
    } else {
      runInteractive();
    }
  }

  /**
   * Runs the application in Swing GUI mode.
   */
  private static void runGui() {
    MultiCalendarModel manager = new CalendarManagerImpl();
    CalendarGuiController controller = new CalendarGuiController(manager);
    javax.swing.SwingUtilities.invokeLater(() ->
            new SwingCalendarView(controller)
    );
  }

  /**
   * Runs the application in interactive command-line mode.
   */
  private static void runInteractive() {
    Scanner scanner = new Scanner(System.in);
    MultiCalendarModel manager = new CalendarManagerImpl();
    CalendarController controller = new CalendarController(manager);
    View view = new InteractiveView();

    view.render("Welcome to the Calendar App!");
    view.render("Type a command or 'exit' to quit.");

    while (true) {
      System.out.print("> ");
      if (!scanner.hasNextLine()) {
        view.render("No input. Exiting.");
        break;
      }
      String input = scanner.nextLine().trim();
      if (input.equalsIgnoreCase("exit")
              || input.equalsIgnoreCase("quit")) {
        view.render("Goodbye!");
        break;
      }
      if (input.isEmpty()) {
        view.render("[ERROR] No input provided.");
        continue;
      }

      try {
        CalendarCommand cmd = CommandParser.parse(input);
        String result = controller.handleCommand(cmd);
        view.render(result);
      } catch (InvalidCommandException e) {
        view.render(e.getMessage());
      }
    }
  }

  /**
   * Runs the application in headless/script mode, reading commands from a file.
   *
   * @param filename path to the script file
   */
  private static void runHeadless(String filename) {
    File file = new File(filename);
    HeadlessView view = new HeadlessView();
    MultiCalendarModel manager = new CalendarManagerImpl();
    CalendarController controller = new CalendarController(manager);

    try (Scanner scanner = new Scanner(file)) {
      boolean exited = false;
      while (scanner.hasNextLine()) {
        String input = scanner.nextLine().trim();
        if (input.equalsIgnoreCase("exit")
                || input.equalsIgnoreCase("quit")) {
          view.render("[OK] Exiting.");
          exited = true;
          break;
        }
        if (input.isEmpty()) {
          view.render("[ERROR] No input provided.");
          continue;
        }

        try {
          CalendarCommand cmd = CommandParser.parse(input);
          String result = controller.handleCommand(cmd);
          view.render(result);
        } catch (InvalidCommandException e) {
          view.render("[ERROR] " + e.getMessage());
        }
      }
      if (!exited) {
        view.render("[ERROR] Exit command not found. Exiting.");
      }
    } catch (FileNotFoundException e) {
      System.out.println("[ERROR] File not found: " + filename);
    }

    // Print log of all results
    for (String log : view.getLogs()) {
      System.out.println(log);
    }
  }
}
