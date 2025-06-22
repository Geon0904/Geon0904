a. Design Changes & Justifications

1) Integrated Multi-Mode Controller
Refactored the application entry point and controller to support all three modes: GUI (Swing), Interactive CLI, and Headless scripting (batch mode) Selected via command-line arguments.
Justification: Clean separation of model and view ensures maintainability, supports future extensibility, and allows for consistent logic across all modes.

2) Dedicated Swing GUI View
Developed a Java Swing-based GUI supporting: Schedule view (shows up to 10 events from any selected date). Easy event creation via form inputs. Calendar switching and browsing
Justification: Greatly improves user experience, reduces input errors, and directly addresses assignment requirements.

3) Default Calendar Initialization
On first launch, the app creates a default calendar using the user’s system timezone. Users are not forced to manually create a calendar.
Justification: Reduces user friction and ensures immediate usability.

4) Graceful Error Handling in the GUI
All user input is validated, and errors are displayed via dialogs. No stack traces or raw exceptions leak to the user.
Justification: Ensures a robust and user-friendly application, especially for non-technical users.

5) MVC Architecture Enforcement
All three modes use formal controller and view interfaces; the view never manipulates the model directly.
Justification: Strict MVC structure increases testability, maintainability, and scalability.

6) Command-Line Argument Validation
The main entry method checks all arguments and reports usage errors cleanly.
Justification: Prevents silent failures and provides clear instructions for users.

7) Multi-Calendar & Event Editing (Extra Credit)
The GUI supports: Creating and switching between multiple named calendars. Editing existing events. 
Justification: Advanced usability for power users and addresses extra credit criteria.




b. How to Run the Program



Build Instructions: 
Compile all source files: javac -d out/production/CalendarApp src/calendarapp/**/*.java

Package into executable JAR: jar cfe CalendarApp.jar calendarapp.CalendarApp -C out/production/CalendarApp . 


Run Instructions: 

Interactive CLI: java -jar CalendarApp.jar

You will see:  
Welcome to the Calendar App!
Type a command or 'exit' to quit.
>


Headless Script Mode: java -jar CalendarApp.jar --script res/valid_commands.txt 

GUI Mode: java -jar CalendarApp.jar --gui





c. Features: What Works and What Does Not

Fully Working Features: 
Fully Working:
GUI (Swing): Schedule view (list, 10 events at a time), robust event creation, multiple calendars, editing, switching, real-time validation.
Interactive CLI: All Assignment 5 commands, user-friendly messages, tab-completion.
Headless Scripting: Reads commands from file, outputs results.
Robust Error Handling: All modes report assignment-compliant error messages.
Strict MVC: All model, view, and controller layers are separated.
Unit Tested: All controller/model logic covered by JUnit4 tests.

Partial / Not Supported:
Event Series (GUI): Only single events can be created in the GUI; recurring/series events via CLI.
Advanced Views (Week/Month): Only simple schedule (list) view provided in GUI.
Drag-and-Drop, Calendar Grid: Not implemented (not required).



d. Contribution Distribution


Geon Chang & Philip Elbert: We collaborated fairly and gave our best effort. We worked hard to complete this assignment together. We continually discussed issues, helped each other debug, and exchanged feedback, and as a result we believe we produced a high quality calendar.



e. Additional Grader Notes

Time Zones: All event times are stored and displayed using each calendar’s IANA timezone.
Input Validation: All inputs (GUI & CLI) are validated. Errors are always reported user-friendly.
GUI Screenshot: See screenshot.png for the main schedule view.
Testing: All controller logic is fully unit-tested (see test/ directory).
res/ Directory: Includes three script files for batch/headless testing: valid_commands.txt, invalid_commands.txt, no_exit.txt.
GUI Usage: See the USEME file for a step-by-step bullet list for every GUI operation.

