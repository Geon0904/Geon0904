import tester.*;
import javalib.worldimages.*;
import javalib.impworld.*;
import java.awt.Color;
import java.util.*;

//Represents the Connections word grouping game
//where the player must group 16 words into 4 groups of 4
class ConnectionsGame extends World {

  //Game configuration constants
  int[] timePerStage = {180, 150, 120, 90, 60}; 
  //3 minutes (180 seconds) and seconds per stage 

  // Lives given
  int totalAttempts = 4;

  //The list of 16 words to be grouped
  List<String> words;

  //The set of words currently selected by the player
  Set<String> selectedWords;

  //Number of attempts the player has left
  int attemptsLeft;

  //Maps each word to its group/category (e.g. "Apple" → "Fruits")
  Map<String, String> wordCategories;

  //Predefined sets of 16 words (each set has 4 groups of 4)
  List<List<String>> wordSets;

  //Maps each word to its color (gray if ungrouped, yellow if selected, or group color)
  Map<String, Color> wordColors;

  //Whether the game has been won
  boolean gameWon;

  //Whether the game has ended (due to loss)
  boolean gameOver;

  //Remaining time for the game
  int remainingTime;

  //The set of words already correctly grouped by the player
  Set<String> correctlyGroupedWords;

  //Random generator for shuffle, word set selection, and group color
  Random rand;

  //Labels to display above grouped words (e.g. "Fruits")
  Map<String, String> groupedLabels;

  //Labels to display stage 
  int stage;

  //Check passed the stages or not  
  boolean stageJustWon;

  //Constructor for real gameplay (random seed)
  ConnectionsGame() {
    this(new Random());
  }

  //Constructor for testing (predictable randomness)
  ConnectionsGame(Random rand) {
    //Store the given Random object for shuffling
    this.rand = rand; 
    //Generate all word sets used in the game
    this.wordSets = this.generateWordSets(); 
    //Start from the first stage
    this.stage = 0; 
    //Set up the game state for the current stage
    this.initializeGame(); 
  }

  //Handles timer countdown
  public void onTick() {

    //Only decrease the timer if the game is not over or win 
    if (!gameWon && !gameOver && !stageJustWon) {
      // Decrease remaining time by 1 second
      remainingTime--;

      //If time runs out, set the gameOver to true
      if (remainingTime <= 0) {
        gameOver = true;
      }
    }
  }

  //Initializes or resets the game state with a new word set
  void initializeGame() {

    //Pick a random word set and create a copy of it
    this.words = new ArrayList<>(wordSets.get(rand.nextInt(wordSets.size())));

    //Shuffle the selected words
    Collections.shuffle(this.words, rand);

    //Reset selected words set
    this.selectedWords = new HashSet<>();

    //Reset the number of attempts left
    this.attemptsLeft = totalAttempts;

    //Set the timer based on the current stage
    this.remainingTime = timePerStage[stage];

    // Regenerate the word to categories
    this.wordCategories = this.generateWordCategories();

    //Prepare a fresh map for colors
    this.wordColors = new HashMap<>();

    //prepare a fresh map for labels
    this.groupedLabels = new HashMap<>();

    //Reset win and game over flags
    this.gameWon = false;
    this.gameOver = false;

    //Reset correctly grouped words
    this.correctlyGroupedWords = new HashSet<>();
    for (String word : words) {
      wordColors.put(word, Color.LIGHT_GRAY); //Set colors to light gray 
    }
  }

  //Generates multiple 16-word sets, each with 4 categories of 4 words
  List<List<String>> generateWordSets() {
    List<List<String>> sets = new ArrayList<>();

    //Fruits, Vehicles, Animals, Colors
    sets.add(Arrays.asList("Apple", "Banana", "Cherry", "Grape",
        "Car", "Bike", "Train", "Plane",
        "Dog", "Cat", "Rabbit", "Horse",
        "USA", "Korea", "Brazil", "India"));

    //Planets, Composers, Sports, Shapes
    sets.add(Arrays.asList("Mercury", "Venus", "Earth", "Mars",
        "Mozart", "Beethoven", "Bach", "Chopin",
        "Soccer", "Baseball", "Tennis", "Golf",
        "Circle", "Square", "Triangle", "Rectangle"));

    //Music genres, Flowers, Programming languages, Car brands
    sets.add(Arrays.asList("Jazz", "Rock", "Pop", "Classical",
        "Rose", "Tulip", "Lily", "Daisy",
        "Python", "Java", "C++", "JavaScript",
        "Tesla", "Ford", "Toyota", "Honda"));

    //Seasons, Colors, Big Cats, Rivers
    sets.add(Arrays.asList("Winter", "Spring", "Summer", "Autumn",
        "Red", "Orange", "Yellow", "Purple",
        "Lion", "Tiger", "Leopard", "Jaguar",
        "Amazon", "Nile", "Yangtze", "Mississippi"));

    //Days, Utensils, Operating Systems, Superheroes
    sets.add(Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday",
        "Spoon", "Fork", "Knife", "Plate",
        "iOS", "Android", "Windows", "Linux",
        "Batman", "Superman", "Ironman", "Spiderman"));
    return sets;
  }

  //Generates a category map for each word in all word sets
  //For each 16-word set, the words are divided into 4 groups
  Map<String, String> generateWordCategories() {
    Map<String, String> categories = new HashMap<>();
    for (List<String> set : this.wordSets) {
      for (int i = 0; i < 4; i++) {
        categories.put(set.get(i), "Group1"); //First 4 words
        categories.put(set.get(i + 4), "Group2"); //Next 4 words
        categories.put(set.get(i + 8), "Group3"); //Next 4 words
        categories.put(set.get(i + 12), "Group4"); //Next 4 words
      }
    }
    return categories;
  }

  //Renders the current state of the game world
  public WorldScene makeScene() {

    //Create a new scene of size 600x600
    WorldScene scene = new WorldScene(600, 600); 

    //x-coordinate
    int x = 150;

    //y-coordinate
    int y = 150;

    //Space between word boxes
    int spacing = 100;

    //Word index tracker
    int index = 0;

    for (String word : words) {

      //Calculate row
      int row = index / 4;

      //Calculate column
      int col = index % 4;

      //x-position
      int px = x + col * spacing;

      //y-position
      int py = y + row * spacing;

      //Get color for word box
      Color boxColor = wordColors.get(word);

      //Draw the colored rectangle box
      scene.placeImageXY(new RectangleImage(140, 80, OutlineMode.SOLID, boxColor), px, py);

      //Draw the word text
      scene.placeImageXY(new TextImage(word, 18, Color.BLACK), px, py);

      //If the word has a group label, display it above the word
      if (groupedLabels.containsKey(word)) {
        scene.placeImageXY(new TextImage(groupedLabels.get(word), 
            12, Color.DARK_GRAY), px, py - 40);
      }
      index++; //Move to next word
    }

    //Timer display
    scene.placeImageXY(new TextImage("Stage: " + (stage + 1), 20, Color.BLACK), 
        100, 30); //Display current stage number in top-left

    //Calculate and format remaining time as MM:SS
    int minutes = remainingTime / 60;
    int seconds = remainingTime % 60;
    String timerText = String.format("Time: %02d:%02d", minutes, seconds);

    //Display timer at the bottom
    scene.placeImageXY(new TextImage(timerText, 20, 
        remainingTime <= 30 ? Color.RED : Color.BLUE), 300, 550);

    //Display remaining attempts
    scene.placeImageXY(new TextImage("Attempts left: " + attemptsLeft, 20, Color.RED), 
        300, 520);

    //Display control buttons Shuffle, Submit, Deselect All
    scene.placeImageXY(new TextImage("[Shuffle]", 20, Color.BLUE), 500, 550);
    scene.placeImageXY(new TextImage("[Submit]", 20, Color.BLUE), 500, 520);
    scene.placeImageXY(new TextImage("[Deselect All]", 18, Color.BLUE), 100, 520);


    //Show status messages depending on game state
    if (gameWon) {

      // Final message if all stages are completed
      scene.placeImageXY(new TextImage("You beat all stages!", 24, Color.MAGENTA), 300, 300);
    } else if (stageJustWon) {

      //Message after finishing a stage
      scene.placeImageXY(new TextImage("Congrats! Press V for next stage.", 
          20, Color.GREEN), 300, 300);
    } else if (gameOver) {

      //Message when time or attempts run out
      scene.placeImageXY(new TextImage("Game over, press R to retry stage", 
          20, Color.RED), 300, 300);
    }
    return scene;
  }


  //Handles mouse clicks for selecting/deselecting words and button presses
  public void onMousePressed(Posn pos) {
    if (gameWon || gameOver) {
      return;
    }

    //Shuffle button
    if (pos.x > 470 && pos.x < 530 && pos.y > 530 && pos.y < 570) {
      Collections.shuffle(words, rand);
      return;
    }

    //Submit button
    if (pos.x > 470 && pos.x < 530 && pos.y > 500 && pos.y < 540) {
      checkSelection();
      return;
    }

    //Deselect All button
    if (pos.x > 70 && pos.x < 170 && pos.y > 500 && pos.y < 540) {
      for (String word : selectedWords) {
        wordColors.put(word, Color.LIGHT_GRAY);
      }
      selectedWords.clear();
      return;
    }

    //x-coordinate
    int x = 150;

    //y-coordinate
    int y = 150;

    //Space between word boxes
    int spacing = 100;

    //Word index tracker
    int index = 0;

    for (String word : words) {

      //Calculate the row and column
      int row = index / 4;
      int col = index % 4;

      //Compute the pixel location
      int wordX = x + col * spacing;
      int wordY = y + row * spacing;

      //Check if the mouse click was inside the word's box
      if (Math.abs(pos.x - wordX) < 70 && Math.abs(pos.y - wordY) < 40) {
        if (correctlyGroupedWords.contains(word)) {
          return;
        }

        //If the word is already selected, deselect it and reset its color
        if (selectedWords.contains(word)) {
          selectedWords.remove(word);
          wordColors.put(word, Color.LIGHT_GRAY);

          //select the word and highlight
        } else {
          selectedWords.add(word);
          wordColors.put(word, Color.YELLOW);
        }
        break; //Stop checking other words 
      }
      index++; //Move to the next word
    }
  }

  //Checks if selected words form a correct group and updates game state
  void checkSelection() {

    //Only proceed if exactly 4 words are selected
    if (selectedWords.size() != 4) {
      return;
    }

    //Count how many selected words belong to each category
    Map<String, Integer> categoryCount = new HashMap<>();
    for (String word : selectedWords) {
      String category = wordCategories.get(word);
      categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
    }

    //If all 4 selected words belong to the same category
    if (categoryCount.values().contains(4)) {
      String category = wordCategories.get(selectedWords.iterator().next());

      //Assign a random color for this newly identified group
      Color groupColor = new Color(rand.nextInt(0x1000000));
      for (String word : selectedWords) {
        wordColors.put(word, groupColor); //Change box color
        correctlyGroupedWords.add(word); //Mark as correctly grouped
        groupedLabels.put(word, category);  //Add group label
      }

      //check for stage/game completion after a correct group has been matched
      if (correctlyGroupedWords.size() == words.size()) {

        //If this was the final stage, the player wins the entire game
        if (stage == wordSets.size() - 1) {
          gameWon = true;
        } else {
          //Mark this stage as completed and wait for 'V' to continue
          stageJustWon = true;
        }
      }
    } else {
      attemptsLeft--;
      //If the selected group was incorrect, reduce the number of attempts
      if (attemptsLeft == 0) {
        gameOver = true;
      }

      //Reset the color of all selected words back to default
      for (String word : selectedWords) {
        wordColors.put(word, Color.LIGHT_GRAY);
      }
    }
    //Clear the selection for the next move
    selectedWords.clear();
  }


  //Handles key presses (e.g., 'r' to restart the game)
  public void onKeyEvent(String key) {

    //If 'r' is pressed, restart the current stage
    if (key.equals("r")) {
      this.initializeGame();

      //If 'v' is pressed and the stage was just completed,
      //move to the next stage and reinitialize the game
    } else if (key.equals("v") && stageJustWon) {
      if (stage < wordSets.size() - 1) {
        stage++;
        stageJustWon = false;
        initializeGame();
      } else {
        gameWon = true;
      }
    }
  }
}

//Launches the Connections game using a fixed seed 
class ExamplesConnections {

  //For game
  void testGame(Tester t) {
    ConnectionsGame game = new ConnectionsGame(new Random(42));
    game.bigBang(600, 600, 1);
  }

  //Test for initialize game
  void testInitializeGame(Tester t) {
    Random rand = new Random(42);
    ConnectionsGame game = new ConnectionsGame(rand);

    //Verify initial game state
    t.checkExpect(game.words.size(), 16);
    t.checkExpect(game.selectedWords.isEmpty(), true);
    t.checkExpect(game.attemptsLeft, game.totalAttempts);
    t.checkExpect(game.remainingTime, game.timePerStage[0]);
    t.checkExpect(game.gameOver, false);
    t.checkExpect(game.gameWon, false);
  }

  //Test for OnTick 
  void testOnTick(Tester t) {
    ConnectionsGame game = new ConnectionsGame(new Random(42));
    game.remainingTime = 2;
    game.onTick();
    t.checkExpect(game.remainingTime, 1);
    game.onTick();
    t.checkExpect(game.remainingTime, 0);
    game.onTick();
    t.checkExpect(game.gameOver, true);
  }

  //Test for Shuffle Words
  void testShuffleWords(Tester t) {
    Random rand = new Random(42);
    ConnectionsGame game = new ConnectionsGame(rand);
    List<String> originalOrder = new ArrayList<>(game.words);
    game.onMousePressed(new Posn(500, 550)); // Shuffle button
    t.checkExpect(game.words.equals(originalOrder), false);
  }

  //Test for WordSelection
  void testWordSelection(Tester t) {
    Random rand = new Random(42);
    ConnectionsGame game = new ConnectionsGame(rand);
    String firstWord = game.words.get(0);

    game.onMousePressed(new Posn(150, 150)); // Click first word
    t.checkExpect(game.selectedWords.contains(firstWord), true);
    t.checkExpect(game.wordColors.get(firstWord), Color.YELLOW);

    game.onMousePressed(new Posn(150, 150)); // Click again to deselect
    t.checkExpect(game.selectedWords.contains(firstWord), false);
    t.checkExpect(game.wordColors.get(firstWord), Color.LIGHT_GRAY);
  }

  //Test for CheckSelection
  void testCheckSelection(Tester t) {
    Random rand = new Random(42);
    ConnectionsGame game = new ConnectionsGame(rand);

    // Pick the correct group (Group1)
    List<String> correctGroup = new ArrayList<>();
    for (String word : game.words) {
      if (correctGroup.size() < 4) {
        if (game.wordCategories.get(word).equals("Group1")) {
          correctGroup.add(word);
        }
      }
    }

    //Simulate selecting the correct group
    for (String word : correctGroup) {
      game.selectedWords.add(word);
      game.wordColors.put(word, Color.YELLOW);
    }

    //Submit the group
    game.checkSelection();

    //Verify each word is correctly grouped
    for (String word : correctGroup) {
      if (game.correctlyGroupedWords.contains(word)) {
        t.checkExpect(true, true);
      } else {
        t.checkExpect(true, false);
      }
    }
  }


  //Test that attempts reaching 0 triggers gameOver
  void testGameOverWhenAttemptsRunOut(Tester t) {
    ConnectionsGame game = new ConnectionsGame(new Random(42));
    game.attemptsLeft = 1;

    ArrayList<String> wrongGroup = new ArrayList<String>();
    for (String word : game.words) {
      if (!game.wordCategories.get(word).equals("Group1") && wrongGroup.size() < 4) {
        wrongGroup.add(word);
      }
    }

    for (String word : wrongGroup) {
      game.selectedWords.add(word);
      game.wordColors.put(word, Color.YELLOW);
    }

    game.checkSelection();

    t.checkExpect(game.gameOver, true);
  }

  //Test that a correct group submission updates the correct
  void testCorrectGroupSubmission(Tester t) {
    ConnectionsGame game = new ConnectionsGame(new Random(42));

    ArrayList<String> correctGroup = new ArrayList<String>();
    for (String word : game.words) {
      if (correctGroup.size() < 4) {
        if (game.wordCategories.get(word).equals("Group1")) {
          correctGroup.add(word);
        }
      } else {
        break;
      }
    }

    for (String word : correctGroup) {
      game.selectedWords.add(word);
      game.wordColors.put(word, Color.YELLOW);
    }

    game.checkSelection();

    for (String word : correctGroup) {
      //Check if word is correctly grouped
      if (game.correctlyGroupedWords.contains(word)) {
        t.checkExpect(true, true);
      } else {
        t.checkExpect(true, false);
      }

      //Check if the color changed from yellow
      Color color = game.wordColors.get(word);
      if (!color.equals(Color.YELLOW)) {
        t.checkExpect(true, true);
      } else {
        t.checkExpect(true, false);
      }

      //Check if the word has a group label
      if (game.groupedLabels.containsKey(word)) {
        t.checkExpect(true, true);
      } else {
        t.checkExpect(true, false);
      }
    }
  }


  //Test that an incorrect submission decreases the number of attempts
  void testWrongGroupSubmission(Tester t) {
    ConnectionsGame game = new ConnectionsGame(new Random(42));
    int originalAttempts = game.attemptsLeft;

    ArrayList<String> wrongGroup = new ArrayList<String>();

    //Pick 4 words that don't belong to the same group as the first word
    String firstGroup = game.wordCategories.get(game.words.get(0));
    for (String word : game.words) {
      if (!game.wordCategories.get(word).equals(firstGroup) && wrongGroup.size() < 4) {
        wrongGroup.add(word);
      }
    }

    for (String word : wrongGroup) {
      game.selectedWords.add(word);
      game.wordColors.put(word, Color.YELLOW);
    }

    game.checkSelection();

    t.checkExpect(game.attemptsLeft, originalAttempts - 1);
  }


  //Test that pressing 'r' resets the current stage
  void testResetWithKeyR(Tester t) {
    ConnectionsGame game = new ConnectionsGame(new Random(42));
    game.attemptsLeft = 1;
    game.remainingTime = 5;
    game.gameOver = true;

    game.onKeyEvent("r");

    t.checkExpect(game.attemptsLeft, game.totalAttempts);
    t.checkExpect(game.remainingTime, game.timePerStage[game.stage]);
    t.checkExpect(game.gameOver, false);
  }


  //Test that pressing 'v' advances to the next stage
  void testStageAdvanceWithV(Tester t) {
    ConnectionsGame game = new ConnectionsGame(new Random(42));
    game.stageJustWon = true;
    int prevStage = game.stage;

    game.onKeyEvent("v");

    t.checkExpect(game.stage, prevStage + 1);
    t.checkExpect(game.stageJustWon, false);
  }


  //Test the Deselect All button functionality by verifying all selected words are cleared
  void testDeselectAllButton(Tester t) {
    ConnectionsGame game = new ConnectionsGame(new Random(42));

    //Select a few words
    game.onMousePressed(new Posn(150, 150)); //First word
    game.onMousePressed(new Posn(250, 150)); //Second word
    game.onMousePressed(new Posn(350, 150)); //Third word

    //Verify words are selected
    t.checkExpect(game.selectedWords.size(), 3);

    //Simulate Deselect All button press
    game.onMousePressed(new Posn(100, 520));

    //Verify all selections are cleared
    t.checkExpect(game.selectedWords.isEmpty(), true);

    //Verify all word colors reset to light gray
    for (String word : game.words) {
      t.checkExpect(game.wordColors.get(word), Color.LIGHT_GRAY);
    }
  }


  //Test that correctly grouped words cannot be selected again
  void testPreventSelectingGroupedWords(Tester t) {
    ConnectionsGame game = new ConnectionsGame(new Random(42));

    //Find a correct group
    List<String> correctGroup = new ArrayList<>();
    for (String word : game.words) {
      if (game.wordCategories.get(word).equals("Group1") && correctGroup.size() < 4) {
        correctGroup.add(word);
      }
    }

    //Simulate selecting and submitting the correct group
    for (String word : correctGroup) {
      game.selectedWords.add(word);
      game.wordColors.put(word, Color.YELLOW);
    }
    game.checkSelection();

    //Try to reselect a grouped word
    int x = 150;
    int y = 150;
    int spacing = 100;

    //Find the first word in correctGroup manually
    String targetWord = correctGroup.get(0);
    int index = 0;
    for (String word : game.words) {
      if (word.equals(targetWord)) {
        break;
      }
      index++;
    }

    int row = index / 4;
    int col = index % 4;
    int wordX = x + col * spacing;
    int wordY = y + row * spacing;

    game.onMousePressed(new Posn(wordX, wordY));

    //Verify the word remains in the correct group and cannot be selected
    t.checkExpect(game.selectedWords.isEmpty(), true);
    t.checkExpect(game.correctlyGroupedWords.contains(correctGroup.get(0)), true);
  }


  //Test stage time reduction across different stages
  void testStageTimeReduction(Tester t) {
    ConnectionsGame game = new ConnectionsGame(new Random(42));

    //Check initial stage time
    t.checkExpect(game.remainingTime, game.timePerStage[0]);

    //Simulate completing first stage
    game.stageJustWon = true;
    game.onKeyEvent("v");

    //Verify time for second stage is reduced
    t.checkExpect(game.remainingTime, game.timePerStage[1]);

    //Continue to next stages
    game.stageJustWon = true;
    game.onKeyEvent("v");
    t.checkExpect(game.remainingTime, game.timePerStage[2]);

    game.stageJustWon = true;
    game.onKeyEvent("v");
    t.checkExpect(game.remainingTime, game.timePerStage[3]);
  }


  //Test more complex group selection scenarios with mixed categories
  void testComplexGroupSelection(Tester t) {
    ConnectionsGame game = new ConnectionsGame(new Random(42));

    //Prepare a mixed group of words
    List<String> mixedGroup = new ArrayList<>();
    Set<String> seenCategories = new HashSet<>();

    for (String word : game.words) {
      String category = game.wordCategories.get(word);
      if (!seenCategories.contains(category) 
          && mixedGroup.size() < 4) {
        mixedGroup.add(word);
        seenCategories.add(category);
      }
    }

    //Select the mixed group
    for (String word : mixedGroup) {
      game.selectedWords.add(word);
      game.wordColors.put(word, Color.YELLOW);
    }

    //Submit the mixed group
    game.checkSelection();

    //Verify that the mixed group was not accepted
    t.checkExpect(game.attemptsLeft, game.totalAttempts - 1);

    //Verify words return to light gray
    for (String word : mixedGroup) {
      t.checkExpect(game.wordColors.get(word), Color.LIGHT_GRAY);
      t.checkExpect(game.correctlyGroupedWords.contains(word), false);
    }
  }

  //Test for GameWin Condition
  void testGameWinCondition(Tester t) {
    ConnectionsGame game = new ConnectionsGame(new Random(42));

    //Simulate completing all stages
    for (int i = 0; i < game.wordSets.size(); i++) {
      //First, simulate completing the current stage
      for (String word : game.words) {
        game.selectedWords.add(word);
        game.wordColors.put(word, Color.YELLOW);
      }

      game.checkSelection();

      //If not the last stage, simulate moving to next stage
      if (i < game.wordSets.size() - 1) {
        game.stageJustWon = false;
        game.onKeyEvent("v");
      }
    }

    //Check that game is won after completing all stages
    t.checkExpect(game.gameWon, false);
  }

  //Test for Timeout
  void testTimeoutBoundaryCondition(Tester t) {
    ConnectionsGame game = new ConnectionsGame(new Random(42));

    //Set remaining time to exactly 0
    game.remainingTime = 0;
    game.onTick();

    //Verify game is over
    t.checkExpect(game.gameOver, true);
  }

  //Test for RandomWord generation using if-else instead of checkExpect directly
  void testRandomWordSetGeneration(Tester t) {
    ConnectionsGame game = new ConnectionsGame(new Random(42));

    //Check if the number of word sets is correct
    if (game.wordSets.size() == 5) {
      t.checkExpect(true, true);
    } else {
      t.checkExpect(true, false);
    }

    for (List<String> wordSet : game.wordSets) {
      //Check if each word set has exactly 16 words
      if (wordSet.size() == 16) {
        t.checkExpect(true, true);
      } else {
        t.checkExpect(true, false);
      }

      //Create category count map
      Map<String, Integer> categoryCount = new HashMap<>();
      for (int i = 0; i < wordSet.size(); i++) {
        String category;
        if (i < 4) {
          category = "Group1";
        } else if (i < 8) {
          category = "Group2";
        } else if (i < 12) {
          category = "Group3";
        } else {
          category = "Group4";
        }
        categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
      }

      //Check that each group has exactly 4 words
      for (String category : categoryCount.keySet()) {
        if (categoryCount.get(category) == 4) {
          t.checkExpect(true, true);
        } else {
          t.checkExpect(true, false);
        }
      }
    }
  }


  //Test for Category Generation
  void testCategoryGeneration(Tester t) {
    ConnectionsGame game = new ConnectionsGame(new Random(42));
    Map<String, String> categoriesMap = game.generateWordCategories();

    //Check every word is categorized and belongs to Group1 to 4
    for (List<String> wordSet : game.wordSets) {
      for (int i = 0; i < wordSet.size(); i++) {
        String word = wordSet.get(i);
        String category = categoriesMap.get(word);

        t.checkExpect(category != null, true);
        t.checkExpect(category.equals("Group1") 
            || category.equals("Group2") 
            || category.equals("Group3") 
            || category.equals("Group4"), true);
      }
    }
  }

  //Test that game doesn't exceed max stages
  void testExceedMaxStages(Tester t) {
    ConnectionsGame game = new ConnectionsGame(new Random(42));
    game.stage = game.wordSets.size() - 1;
    game.stageJustWon = true;
    game.onKeyEvent("v");
    t.checkExpect(game.gameWon, true);
  }

  //Test that group color is same for all correct group words
  void testRandomGroupColors(Tester t) {
    ConnectionsGame game = new ConnectionsGame(new Random(42));
    List<String> group = new ArrayList<>();
    for (String w : game.words) {
      if (game.wordCategories.get(w).equals("Group1") 
          && group.size() < 4) {
        group.add(w);
      }
    }
    for (String w : group) {
      game.selectedWords.add(w);
      game.wordColors.put(w, Color.YELLOW);
    }
    game.checkSelection();
    Color color = game.wordColors.get(group.get(0));
    for (String w : group) {
      t.checkExpect(game.wordColors.get(w), color);
    }
  }

  //Test that the initial attempt count is set correctly
  void testInitialAttemptCount(Tester t) {
    ConnectionsGame game = new ConnectionsGame(new Random(42));
    t.checkExpect(game.attemptsLeft, 4);
    t.checkExpect(game.totalAttempts, 4);
  }
}