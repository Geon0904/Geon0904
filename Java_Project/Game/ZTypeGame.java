import tester.*;
import java.util.Random;
import javalib.worldimages.*;
import javalib.funworld.*;
import java.awt.Color;


//Represents the world state of the ZType typing game.
class ZTypeWorld extends World {
  int width = 600;
  int height = 400;
  ILoWord words;
  String currentTyped;
  int score;
  boolean gameOver;
  int stage;
  int scoreForNextStage;
  int wordSpeed;


  // Main constructor: Initializes a ZTypeWorld with the given state values.
  ZTypeWorld(ILoWord words, String currentTyped, int score, boolean gameOver, 
      int stage, int scoreForNextStage, int wordSpeed) {
    this.words = words;
    this.currentTyped = currentTyped;
    this.score = score;
    this.gameOver = gameOver;
    this.stage = stage;
    this.scoreForNextStage = scoreForNextStage;
    this.wordSpeed = wordSpeed;
  }

  /* TEMPLATE:
  Fields:
  ... this.width ...                 -- int
  ... this.height ...                -- int
  ... this.words ...                 -- ILoWord
  ... this.currentTyped ...          -- String
  ... this.score ...                 -- int
  ... this.gameOver ...              -- boolean
  ... this.stage ...                 -- int
  ... this.scoreForNextStage ...     -- int
  ... this.wordSpeed ...             -- int
  ---------------------------------------------------------------
  Methods:
  ... this.makeScene() ...           -- WorldScene
  ... this.onTick() ...              -- World
  ... this.onKeyEvent(String key) ... -- World
  ... this.lastScene(String msg) ...  -- WorldScene
  ---------------------------------------------------------------
  Methods for fields:
  ... this.words.draw(WorldScene acc, String currentTyped)   -- WorldScene
  ... this.words.move(int speed)                             -- ILoWord
  ... this.words.reachedBottom()                             -- boolean
  ... this.words.maybeAddWord()                              -- ILoWord
  ... this.words.removeActive()                              -- ILoWord
  ... this.words.hasActiveWordMatching(String typed)         -- boolean
  ... this.words.activeWordCompleted(String typed)           -- boolean
  ... this.words.matchedWord(String typed)                   -- String
  ... this.words.activateWord(String word)                   -- ILoWord
  ... this.words.removeMatching(String typed)                -- ILoWord
  ... this.currentTyped + key                                -- String
  ... this.score + 1                                         -- int
  ... this.scoreForNextStage + 10                            -- int
  ... new ZTypeWorld(...)                                    -- ZTypeWorld 
   */ 


  // Convenience constructor: Initializes a new game with default values.
  ZTypeWorld() {
    this(new MtLoWord(), "", 0, false, 1, 10, 3);
  }


  // Produces the current visual representation of the game world.
  public WorldScene makeScene() {
    WorldScene scene = this.words.draw(new WorldScene(width, height), this.currentTyped);
    scene = scene.placeImageXY(new TextImage("Score: " + this.score, 20, Color.BLACK), 50, 20);
    scene = scene.placeImageXY(new TextImage("Stage: " + this.stage, 20, Color.BLACK), 50, 50);

    if (this.gameOver) {
      scene = scene.placeImageXY(new TextImage("Game Over! Final Score: " 
          + this.score, 30, Color.RED), width / 2, height / 2);
      scene = scene.placeImageXY(new TextImage("Press 'r' to restart", 
          20, Color.BLUE), width / 2, height / 2 + 40);
    }
    return scene;
  }


  // Updates the world state on each tick (timer event), 
  // moving words and checking for stage progression or game over.
  public World onTick() {
    if (this.gameOver) {
      return this;
    }

    if (this.score >= this.scoreForNextStage && this.stage < 10) {
      return new ZTypeWorld(this.words, this.currentTyped, this.score, false,
          this.stage + 1, this.scoreForNextStage + 10, this.wordSpeed + 1);
    }

    ILoWord movedWords = this.words.move(this.wordSpeed);
    if (movedWords.reachedBottom()) {
      return new ZTypeWorld(this.words, this.currentTyped, this.score, true,
          this.stage, this.scoreForNextStage, this.wordSpeed);
    }

    return new ZTypeWorld(movedWords.maybeAddWord(), this.currentTyped,
        this.score, false, this.stage, this.scoreForNextStage, this.wordSpeed);
  }


  // Updates the world state in response to a key event, 
  // either typing a letter or restarting the game.
  public World onKeyEvent(String key) {
    if (this.gameOver) {
      if (key.equals("r")) {
        return new ZTypeWorld();
      }
      return this;
    }

    if (key.length() == 1 && Character.isLetter(key.charAt(0))) {
      String newTyped = this.currentTyped + key;

      if (!this.currentTyped.isEmpty()) {
        if (this.words.hasActiveWordMatching(newTyped)) {
          if (this.words.activeWordCompleted(newTyped)) {

            ILoWord updatedWords = this.words.removeActive();
            return new ZTypeWorld(updatedWords, "", this.score + 1, false,
                this.stage, this.scoreForNextStage, this.wordSpeed);
          }

          return new ZTypeWorld(this.words, newTyped, this.score, false,
              this.stage, this.scoreForNextStage, this.wordSpeed);
        } else {

          return new ZTypeWorld(this.words, "", this.score, false,
              this.stage, this.scoreForNextStage, this.wordSpeed);
        }
      } else {

        String matchedWord = this.words.matchedWord(key);
        if (!matchedWord.isEmpty()) {
          ILoWord updatedWords = this.words.activateWord(matchedWord);
          if (matchedWord.equals(key)) {

            updatedWords = updatedWords.removeActive();
            return new ZTypeWorld(updatedWords, "", this.score + 1, false,
                this.stage, this.scoreForNextStage, this.wordSpeed);
          }             
          return new ZTypeWorld(updatedWords, key, this.score, false,
              this.stage, this.scoreForNextStage, this.wordSpeed);
        }
      }
    }
    return this;
  }

  // Produces the final scene displayed when the game ends, showing a custom message.
  public WorldScene lastScene(String msg) {
    return this.makeScene().placeImageXY(new TextImage(msg, 30, Color.RED), width / 2, height / 2);
  }
}



interface ILoWord {
  // Draws all the words in this list onto the given scene, highlighting any
  // matches with the currently typed string.
  WorldScene draw(WorldScene acc, String currentTyped);

  // Moves all words in this list downward by the given speed.
  ILoWord move(int speed);

  // Checks if any word in this list has reached the bottom of the screen.
  boolean reachedBottom();

  // Potentially adds a new random word to the list, with a low probability.
  ILoWord maybeAddWord();

  // Removes words from the list that match the given typed string.
  ILoWord removeMatching(String typed);

  // Returns the text of the first word in this list that starts with the given string.
  String matchedWord(String typed);

  // Activates the word that matches the given string, and inactivates others.
  ILoWord activateWord(String word);

  // Checks if there is an active word in the list that starts with the given string.
  boolean hasActiveWordMatching(String typed);

  // Checks if the active word is fully typed and matches the given string.
  boolean activeWordCompleted(String typed);

  // Removes the currently active word from the list.
  ILoWord removeActive();

  // Inactivates all words in the list.
  ILoWord inactivateAll();
}




// empty words list
class MtLoWord implements ILoWord {

  /* TEMPLATE:

  Fields:
  ---------------------------------------------------------------
  Methods:
  ... this.draw(WorldScene acc, String currentTyped) ...       -- WorldScene
  ... this.move(int speed) ...                                 -- ILoWord
  ... this.reachedBottom() ...                                 -- boolean
  ... this.maybeAddWord() ...                                  -- ILoWord
  ... this.removeMatching(String typed) ...                   -- ILoWord
  ... this.matchedWord(String typed) ...                      -- String
  ... this.activateWord(String word) ...                      -- ILoWord
  ... this.hasActiveWordMatching(String typed) ...            -- boolean
  ... this.activeWordCompleted(String typed) ...              -- boolean
  ... this.removeActive() ...                                  -- ILoWord
  ... this.inactivateAll() ...                                 -- ILoWord
  ---------------------------------------------------------------
  Methods for fields:
   */ 


  // Draws nothing because the list is empty.
  public WorldScene draw(WorldScene acc, String currentTyped) { 
    return acc; 
  }

  // Does nothing because the list is empty.
  public ILoWord move(int speed) { 
    return this; 
  }

  // Returns false because no word can reach the bottom in an empty list.
  public boolean reachedBottom() { 
    return false; 
  }

  // Possibly adds a random word to the empty list.
  public ILoWord maybeAddWord() {
    Utils utils = new Utils();
    if (new Random().nextInt(50) == 0) {
      String newWord = utils.randomWord();
      if (this.matchedWord(newWord).isEmpty()) {
        return new ConsLoWord(new ActiveWord(newWord, 
            50 + new Random().nextInt(500 - 60), 0), this);
      }
    }
    return this;
  }

  // Removes nothing because the list is empty.
  public ILoWord removeMatching(String typed) { 
    return this; 
  }

  // Returns an empty string because no word matches in an empty list.
  public String matchedWord(String typed) { 
    return ""; 
  }

  // Does nothing because the list is empty.
  public ILoWord activateWord(String word) {
    return this;
  }

  // Returns false because there are no active words in an empty list.
  public boolean hasActiveWordMatching(String typed) {
    return false;
  }

  // Returns false because no active word can be completed in an empty list.
  public boolean activeWordCompleted(String typed) {
    return false;
  }

  // Removes nothing because the list is empty.
  public ILoWord removeActive() {
    return this;
  }

  // Does nothing because the list is empty.
  public ILoWord inactivateAll() {
    return this;
  }
}




// Non-empty words list
class ConsLoWord implements ILoWord {
  IWord first;
  ILoWord rest;

  ConsLoWord(IWord first, ILoWord rest) {
    this.first = first;
    this.rest = rest;
  }

  /* TEMPLATE:

  Fields:
  ... this.first ...               -- IWord
  ... this.rest ...                -- ILoWord

 ----------------------------------------------------------------
  Methods:
  ... this.draw(WorldScene acc, 
      String currentTyped) ...                                      -- WorldScene
  ... this.move(int speed) ...                                      -- String
  ... this.reachedBottom() ...                                      -- boolean
  ... this.maybeAddWord() ...                                       -- ILoWord
  ... this.removeMatching(String typed) ...                         -- ILoWord
  ... this.matchedWord(String typed) ...                            -- ILoWord
  ... this.activateWord(String word) ...                            -- ILoWord
  ... this.hasActiveWordMatching(String typed) ...                  -- boolean
  ... this.activeWordCompleted(String typed) ...                    -- boolean
  ... this.removeActive() ...                                       -- ILoWord
  ... this.inactivateAll() ...                                      -- ILoWord
  ---------------------------------------------------------------
  Methods for fields:
  ... this.first.draw(WorldScene acc,
      String currentTyped) ...                                  -- WorldScene
  ... this.first.move(int speed) ...                             -- IWord
  ... this.first.reachedBottom() ...                             -- boolean
  ... this.first.matches(String typed) ...                      -- boolean
  ... this.first.startsWith(String typed) ...                   -- boolean
  ... this.first.getText() ...                                   -- String
  ... this.first.activate() ...                                  -- IWord
  ... this.first.inactivate() ...                                -- IWord
  ... this.first.removeMatchingPrefix(
       String typed) ...                                         -- IWord
  ... this.rest.draw(WorldScene acc, 
      String currentTyped) ...                                  -- WorldScene
  ... this.rest.move(int speed) ...                              -- ILoWord
  ... this.rest.reachedBottom() ...                              -- boolean
  ... this.rest.matchedWord(String typed) ...                    -- String
  ... this.rest.activateWord(String word) ...                    -- ILoWord
  ... this.rest.hasActiveWordMatching(
       String typed) ...                                          -- boolean
  ... this.rest.activeWordCompleted(
       String typed) ...                                           -- boolean
  ... this.rest.removeActive() ...                               -- ILoWord
  ... this.rest.inactivateAll() ...                              -- ILoWord
   */


  // Draws this word and the rest of the words onto the given scene.
  public WorldScene draw(WorldScene acc, String currentTyped) {
    return this.rest.draw(this.first.draw(acc, currentTyped), currentTyped);
  }

  // Moves this word and the rest of the words downward by the given speed.
  public ILoWord move(int speed) {
    return new ConsLoWord(this.first.move(speed), this.rest.move(speed));
  }

  // Checks if this word or any word in the rest has reached the bottom.
  public boolean reachedBottom() {
    return this.first.reachedBottom() || this.rest.reachedBottom();
  }

  // Possibly adds a random word to the front of the list.
  public ILoWord maybeAddWord() {
    Utils utils = new Utils();
    if (new Random().nextInt(50) == 0) {
      String newWord = utils.randomWord();
      if (this.matchedWord(newWord).isEmpty()) {
        return new ConsLoWord(new ActiveWord(newWord, 
            50 + new Random().nextInt(500 - 60), 0), this);
      }
    }
    return this;
  }

  // Removes the first matching word, or continues searching in the rest.
  public ILoWord removeMatching(String typed) {
    if (this.first.matches(typed)) {
      return this.rest;
    }
    return new ConsLoWord(this.first.removeMatchingPrefix(typed), 
        this.rest.removeMatching(typed));
  }

  // Returns the text of the first word that starts with the given string.
  public String matchedWord(String typed) {
    if (this.first.startsWith(typed)) {
      return this.first.getText();
    } else {
      return this.rest.matchedWord(typed);
    }
  }

  // Activates the word that matches the given string and inactivates others.
  public ILoWord activateWord(String word) {
    if (this.first.getText().equals(word)) {
      return new ConsLoWord(this.first.activate(), this.rest.inactivateAll());
    } else {
      return new ConsLoWord(this.first.inactivate(), this.rest.activateWord(word));
    }
  }

  // Checks if this or any word in the rest is active and starts with the given string.
  public boolean hasActiveWordMatching(String typed) {
    return (this.first.isActive() && this.first.startsWith(typed)) 
        || this.rest.hasActiveWordMatching(typed);
  }

  // Checks if this or any word in the rest is an active completed match.
  public boolean activeWordCompleted(String typed) {
    return (this.first.isActive() && this.first.getText().equals(typed)) 
        || this.rest.activeWordCompleted(typed);
  }

  // Removes the active word from the list.
  public ILoWord removeActive() {
    if (this.first.isActive()) {

      return this.rest;
    } else {

      return new ConsLoWord(this.first, this.rest.removeActive());
    }
  }

  // Inactivates this word and all others in the list.
  public ILoWord inactivateAll() {
    return new ConsLoWord(this.first.inactivate(), this.rest.inactivateAll());
  }
}


interface IWord {
  // Represents a word in the falling words typing game.

  // Draws this word onto the given WorldScene with consideration of the current typed input.
  WorldScene draw(WorldScene acc, String currentTyped);

  // Moves this word down by the given speed.
  IWord move(int speed);

  // Checks if this word has reached the bottom of the screen.
  boolean reachedBottom();

  // Checks if this word matches the given typed string exactly.
  boolean matches(String typed);

  // Checks if this word starts with the given typed string.
  boolean startsWith(String typed);

  // Removes the matching prefix from this word based on the given typed string.
  IWord removeMatchingPrefix(String typed);

  // Returns the text of this word.
  String getText();

  // Determines if this word is active.
  boolean isActive();

  // Activates this word.
  IWord activate();

  // Inactivates this word.
  IWord inactivate();
}




class ActiveWord implements IWord {
  String word;
  int x;
  int y;

  ActiveWord(String word, int x, int y) {
    this.word = word;
    this.x = x;
    this.y = y;
  }


  /* TEMPLATE:

  Fields:
  ... this.word ...             -- String 
  ... this.x ...                -- int    
  ... this.y ...                -- int    
  ---------------------------------------------------------------
  Methods:
  ... this.draw(WorldScene acc, 
      String currentTyped) ...                         -- WorldScene
  ... this.move(int speed) ...                             -- IWord
  ... this.reachedBottom() ...                             -- boolean
  ... this.matches(String typed) ...                      -- boolean
  ... this.startsWith(String typed) ...                   -- boolean
  ... this.removeMatchingPrefix(String typed) ...         -- IWord
  ... this.getText() ...                                   -- String
  ... this.isActive() ...                                  -- boolean
  ... this.activate() ...                                  -- IWord
  ... this.inactivate() ...                                -- IWord
  ---------------------------------------------------------------
  Methods for fields:
  ... this.word.equals(typed)                             -- boolean
  ... this.word.startsWith(typed)                         -- boolean
  ... new ActiveWord(this.word.substring(typed.length()), 
      this.x, this.y)                                     -- IWord
  ... new ActiveWord(this.word, this.x, this.y + speed)   -- IWord
  ---------------------------------------------------------------
   */


  // Represents an active word that is currently being typed by the player.
  public WorldScene draw(WorldScene acc, String currentTyped) {
    String displayText = this.word;
    Color color = Color.RED;

    if (!currentTyped.isEmpty() && this.word.startsWith(currentTyped)) {
      color = Color.GREEN; // Highlights matching part in green
      displayText = this.word.substring(currentTyped.length());
    }

    return acc.placeImageXY(new TextImage(displayText, 20, color), this.x, this.y);
  }

  //Moves the word downward by the given speed
  public IWord move(int speed) {
    return new ActiveWord(this.word, this.x, this.y + speed);
  }

  // Checks if the word has reached the bottom of the screen
  public boolean reachedBottom() {
    return this.y >= 400;
  }

  // Checks if the word matches the given typed string exactly
  public boolean matches(String typed) {
    return this.word.equals(typed);
  }

  //Checks if the word starts with the given typed string
  public boolean startsWith(String typed) {
    return this.word.startsWith(typed);
  }

  // Removes the typed prefix from the word
  public IWord removeMatchingPrefix(String typed) {
    if (this.startsWith(typed)) {
      return new ActiveWord(this.word.substring(typed.length()), this.x, this.y);
    }
    return this;
  }

  //Returns the full text of the word
  public String getText() {
    return this.word;
  }

  //Shows that this word is active
  public boolean isActive() {
    return true;
  }

  //Returns the word as is because it is already active
  public IWord activate() {
    return this;
  }

  //Converts this active word to an inactive word
  public IWord inactivate() {
    return new InactiveWord(this.word, this.x, this.y);
  }
}




class InactiveWord implements IWord {
  String word;
  int x;
  int y;

  InactiveWord(String word, int x, int y) {
    this.word = word;
    this.x = x;
    this.y = y;
  }


  /* TEMPLATE:

  Fields:
  ... this.word ...             -- String 
  ... this.x ...                -- int   
  ... this.y ...                -- int    
  ---------------------------------------------------------------
  Methods:
  ... this.draw(WorldScene acc, 
      String currentTyped) ...                          -- WorldScene
  ... this.move(int speed) ...                             -- IWord
  ... this.reachedBottom() ...                             -- boolean
  ... this.matches(String typed) ...                      -- boolean
  ... this.startsWith(String typed) ...                   -- boolean
  ... this.removeMatchingPrefix(String typed) ...         -- IWord
  ... this.getText() ...                                   -- String
  ... this.isActive() ...                                  -- boolean
  ... this.activate() ...                                  -- IWord
  ... this.inactivate() ...                                -- IWord
  ---------------------------------------------------------------
  Methods for fields:
  ... this.word.equals(typed)                             -- boolean
  ... this.word.startsWith(typed)                         -- boolean
  ... new InactiveWord(this.word, this.x, this.y + speed) -- IWord
  ---------------------------------------------------------------
   */


  // Represents an inactive word that is not currently being typed by the player.
  public WorldScene draw(WorldScene acc, String currentTyped) {
    return acc.placeImageXY(new TextImage(this.word, 20, Color.RED), this.x, this.y);
  }

  //Moves the word downward by the given speed
  public IWord move(int speed) {
    return new InactiveWord(this.word, this.x, this.y + speed);
  }

  //Checks if the word has reached the bottom of the screen
  public boolean reachedBottom() {
    return this.y >= 400;
  }

  // returns false because inactive words are not typed
  public boolean matches(String typed) {
    return false;
  }

  //Checks if the word starts with the given typed string
  public boolean startsWith(String typed) {
    return this.word.startsWith(typed);
  }

  //Returns itself because inactive words do not remove prefixes
  public IWord removeMatchingPrefix(String typed) {
    return this;
  }


  // Returns the full text of the word
  public String getText() {
    return this.word;
  }

  // Indicates that this word is inactive
  public boolean isActive() {
    return false;
  }

  // Converts this inactive word to an active word
  public IWord activate() {
    return new ActiveWord(this.word, this.x, this.y);
  }

  //Returns itself because it is already inactive
  public IWord inactivate() {
    return this;
  }
}


class Utils {
  Random rand;

  Utils() {
    this.rand = new Random();
  }


  /* TEMPLATE:

  Fields:
  ... this.rand ...             -- Random

 ----------------------------------------------------------------
  Methods:
  ... this.randomWord() ...            -- String
  ... this.randomWordHelper() ...      -- String
  ---------------------------------------------------------------
  Methods for fields:
  ... this.randomWordHelper(6, "") ...                        -- String
  ... this.randomWordHelper(length - 1, acc + nextChar) ...   -- String
   */


  Utils(Random rand) { // Constructor for testing
    this.rand = rand;
  }

  // Generates a random word with a length of 6 characters, 
  // composed of lowercase letters.
  String randomWord() {
    return this.randomWordHelper(6, "");
  }

  // Helper method that recursively constructs a random word of the given length.
  String randomWordHelper(int length, String acc) {
    if (length == 0) {
      return acc;
    }
    char nextChar = (char) ('a' + this.rand.nextInt(26));
    return this.randomWordHelper(length - 1, acc + nextChar);
  }
}

//Tests to start the game and test helper functions
class ExamplesZType {

  ILoWord mt = new MtLoWord();
  IWord activeWord = new ActiveWord("hello", 100, 100);
  IWord inactiveWord = new InactiveWord("world", 150, 150);
  ILoWord oneWordList = new ConsLoWord(activeWord, mt);
  ILoWord twoWordList = new ConsLoWord(activeWord, new ConsLoWord(inactiveWord, mt));

  ZTypeWorld initWorld = new ZTypeWorld();
  ZTypeWorld customWorld = new ZTypeWorld(twoWordList, "h", 5, false, 1, 10, 3);

  //Tests the default initialization of a game world
  boolean testInitialGameState(Tester t) {
    return t.checkExpect(initWorld.words, new MtLoWord()) &&
        t.checkExpect(initWorld.currentTyped, "") &&
        t.checkExpect(initWorld.score, 0) &&
        t.checkExpect(initWorld.gameOver, false) &&
        t.checkExpect(initWorld.stage, 1) &&
        t.checkExpect(initWorld.scoreForNextStage, 10) &&
        t.checkExpect(initWorld.wordSpeed, 3);
  }



  //Tests that pressing keys after game over does not change state
  boolean testGameOverKeyPress(Tester t) {
    ZTypeWorld gameOverWorld = new ZTypeWorld(twoWordList, "h", 5, true, 1, 10, 3);
    return t.checkExpect(gameOverWorld.onKeyEvent("a"), gameOverWorld);
  }

  //Tests that removing an active word when none exist does nothing
  boolean testRemoveActiveNoActiveWord(Tester t) {
    ILoWord inactiveList = new ConsLoWord(inactiveWord, mt);
    return t.checkExpect(inactiveList.removeActive(), inactiveList);
  }


  //Tests that all random words have exactly 6 characters
  boolean testRandomWordLength(Tester t) {
    Utils utils = new Utils(new Random(1));
    return t.checkExpect(utils.randomWord().length(), 6);
  }



  //Tests that stage does not increase beyond 10
  boolean testMaxStage(Tester t) {
    ZTypeWorld stage10World = new ZTypeWorld(twoWordList, "h", 5, false, 10, 100, 3);
    ZTypeWorld afterTick = (ZTypeWorld) stage10World.onTick();
    return t.checkExpect(afterTick.stage, 10);
  }


  //Tests game state update per tick
  boolean testOnTick(Tester t) {
    ZTypeWorld afterTick = (ZTypeWorld) customWorld.onTick();
    return t.checkExpect(afterTick.words, customWorld.words.move(3).maybeAddWord());
  }

  // Tests key input handling (typing letters)
  boolean testOnKeyEvent(Tester t) {
    ZTypeWorld afterKeyEvent = (ZTypeWorld) customWorld.onKeyEvent("e");
    return t.checkExpect(afterKeyEvent.currentTyped, "he");
  }


  //Tests rendering of the game scene
  boolean testMakeScene(Tester t) {
    WorldScene expectedScene = new WorldScene(600, 400);
    expectedScene = customWorld.words.draw(expectedScene, "h");
    expectedScene = expectedScene.placeImageXY(new TextImage("Score: 5", 20, Color.BLACK), 50, 20);
    expectedScene = expectedScene.placeImageXY(new TextImage("Stage: 1", 20, Color.BLACK), 50, 50);
    return t.checkExpect(customWorld.makeScene(), expectedScene);
  }


  // Tests rendering of the final game-over scene
  boolean testLastScene(Tester t) {
    WorldScene expectedScene = customWorld.makeScene()
        .placeImageXY(new TextImage("Game Over!", 30, Color.RED), 600 / 2, 400 / 2);
    return t.checkExpect(customWorld.lastScene("Game Over!"), expectedScene);
  }

  // Tests word movement downward
  boolean testWordMove(Tester t) {
    return t.checkExpect(activeWord.move(5), new ActiveWord("hello", 100, 105));
  }

  // Tests if a word correctly detects when it reaches the bottom
  boolean testWordReachedBottom(Tester t) {
    return t.checkExpect(activeWord.reachedBottom(), false)
        && t.checkExpect(new ActiveWord("test", 50, 400).reachedBottom(), true);
  }

  // Tests exact word matching
  boolean testWordMatches(Tester t) {
    return t.checkExpect(activeWord.matches("hello"), true)
        && t.checkExpect(activeWord.matches("world"), false);
  }

  //Tests prefix matching for typing progress
  boolean testWordStartsWith(Tester t) {
    return t.checkExpect(activeWord.startsWith("he"), true)
        && t.checkExpect(activeWord.startsWith("wo"), false);
  }

  // Tests removing typed prefix from an active word
  boolean testWordRemoveMatchingPrefix(Tester t) {
    return t.checkExpect(activeWord.removeMatchingPrefix("he"), new ActiveWord("llo", 100, 100));
  }

  // Tests activating and inactivating words
  boolean testWordActivateInactivate(Tester t) {
    return t.checkExpect(new InactiveWord("hello", 100, 100).activate(),
        new ActiveWord("hello", 100, 100))
        && t.checkExpect(new ActiveWord("world", 150, 150).inactivate(),
            new InactiveWord("world", 150, 150));
  }

  //Tests movement of an empty word list 
  boolean testMtLoWordMove(Tester t) {
    return t.checkExpect(mt.move(3), mt);
  }

  // Tests movement of a list with words
  boolean testConsLoWordMove(Tester t) {
    return t.checkExpect(oneWordList.move(3), 
        new ConsLoWord(new ActiveWord("hello", 100, 103), mt));
  }

  // Tests if a word list correctly detects when a word reaches the bottom
  boolean testReachedBottom(Tester t) {
    IWord bottomWord = new ActiveWord("test", 50, 400);
    ILoWord bottomList = new ConsLoWord(bottomWord, mt);
    return t.checkExpect(mt.reachedBottom(), false) 
        && t.checkExpect(bottomList.reachedBottom(), true);
  }

  //Tests checking for an active word matching a typed prefix
  boolean testHasActiveWordMatching(Tester t) {
    return t.checkExpect(twoWordList.hasActiveWordMatching("h"), true)
        && t.checkExpect(twoWordList.hasActiveWordMatching("w"), false);
  }

  //Tests if an active word is fully typed and should be removed
  boolean testActiveWordCompleted(Tester t) {
    return t.checkExpect(twoWordList.activeWordCompleted("hello"), true)
        && t.checkExpect(twoWordList.activeWordCompleted("hell"), false);
  }

  //Tests removing the active word from the list
  boolean testRemoveActive(Tester t) {
    return t.checkExpect(twoWordList.removeActive(), new ConsLoWord(inactiveWord, mt));
  }

  // Tests finding the first word that matches a typed prefix
  boolean testMatchedWord(Tester t) {
    return t.checkExpect(twoWordList.matchedWord("h"), "hello")
        && t.checkExpect(twoWordList.matchedWord("z"), "");
  }

  //Tests activating a word in the list
  boolean testActivateWord(Tester t) {
    ILoWord activated = twoWordList.activateWord("hello");
    return t.checkExpect(activated.hasActiveWordMatching("h"), true);
  }

  //Tests inactivating all words in the list
  boolean testInactivateAll(Tester t) {
    ILoWord inactivated = twoWordList.inactivateAll();
    return t.checkExpect(inactivated.hasActiveWordMatching("h"), false);
  }


  //Tests that when a certain number of words exist, new words are not added
  boolean testWordOverflow(Tester t) {
    ILoWord fullWordList = new ConsLoWord(new ActiveWord("a", 100, 50), 
        new ConsLoWord(new ActiveWord("b", 200, 60), 
            new ConsLoWord(new ActiveWord("c", 300, 70), mt)));

    ZTypeWorld worldWithFullWords = new ZTypeWorld(fullWordList, "", 5, false, 1, 10, 3);
    ZTypeWorld afterTick = (ZTypeWorld) worldWithFullWords.onTick();

    return t.checkExpect(afterTick.words.maybeAddWord().matchedWord(""), "a");
  }


  //Tests that word speed increases after reaching the required score
  boolean testSpeedIncreaseOnLevelUp(Tester t) {
    ZTypeWorld levelUpWorld = new ZTypeWorld(twoWordList, "", 10, false, 1, 10, 3);
    ZTypeWorld afterTick = (ZTypeWorld) levelUpWorld.onTick();

    return t.checkExpect(afterTick.stage, 2) &&
        t.checkExpect(afterTick.wordSpeed, 4);
  }


  //Tests the helper method for generating a random word
  boolean testRandomWordHelper(Tester t) {
    Utils utils = new Utils(new Random(1)); // Fixed seed

    return t.checkExpect(utils.randomWordHelper(0, ""), "")
        && t.checkExpect(utils.randomWordHelper(1, ""), "r")
        && t.checkExpect(utils.randomWordHelper(3, "a"), "aahj");
  }

  //Tests random word generation 
  boolean testRandomWord(Tester t) {
    Utils utils = new Utils(new Random(1)); // Fixed seed

    return t.checkExpect(utils.randomWord(), "rahjmy");
  }


  //Tests that pressing an invalid key (non-letter) does not change state
  boolean testInvalidKeyPress(Tester t) {
    ZTypeWorld afterInvalidKey = (ZTypeWorld) customWorld.onKeyEvent("1");
    return t.checkExpect(afterInvalidKey.currentTyped, customWorld.currentTyped);
  }


  //Tests random word generation 
  ILoWord mt1 = new MtLoWord();
  ZTypeWorld initWorld1 = new ZTypeWorld();

  //Tests starting the game 
  boolean testBigBang(Tester t) {
    int worldWidth = 600;
    int worldHeight = 400;
    double tickRate = 0.1;
    return initWorld1.bigBang(worldWidth, worldHeight, tickRate);
  }

  public class Main {
    public static void main(String[] args) {
      ZTypeWorld game = new ZTypeWorld();
      game.bigBang(600, 400, 0.1);
    }
  }


}

}
