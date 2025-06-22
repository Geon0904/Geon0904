package calendarapp.view;

/**
 * Provides a way to render messages to the user.
 */
public interface View {

  /**
   * Renders the given message to the user.
   *
   * @param output the message to display
   */
  void render(String output);
}
