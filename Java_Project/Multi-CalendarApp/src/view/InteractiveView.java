package calendarapp.view;

import java.util.Scanner;

/**
 * It allows reading commands from the user and displaying messages.
 */
public class InteractiveView implements View {

  private final Scanner scanner;

  /**
   * Constructs an InteractiveView using standard input.
   */
  public InteractiveView() {
    this.scanner = new Scanner(System.in);
  }


  /**
   * Reads a line of input from the user.
   *
   * @return the user's input
   */
  public String readCommand() {
    System.out.print("> ");
    return scanner.nextLine();
  }


  @Override
  public void render(String output) {
    System.out.println(output);
  }


}
