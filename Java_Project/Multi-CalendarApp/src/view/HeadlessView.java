package calendarapp.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A view that does not display to the user but stores outputs internally.
 */
public class HeadlessView implements View {

  private final List<String> logs;


  /**
   * Constructs a HeadlessView that stores all outputs internally.
   */
  public HeadlessView() {
    this.logs = new ArrayList<>();
  }


  @Override
  public void render(String output) {
    logs.add(output);
  }


  /**
   * Returns an unmodifiable list of all messages rendered.
   *
   * @return the list of rendered messages
   */
  public List<String> getLogs() {
    return Collections.unmodifiableList(logs);
  }


  /**
   * Clears the stored log messages.
   */
  public void clear() {
    logs.clear();
  }
}
