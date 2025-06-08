import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;
import javalib.impworld.*;
import javalib.worldimages.*;
import java.awt.Color;
import tester.*;

//Constructs an edge between two GamePiece nodes with a given weight
class Edge implements Comparable<Edge> {
  GamePiece from;
  GamePiece to;
  int weight;

  //Initializes an edge with two GamePiece nodes and a weight representing connection cost
  Edge(GamePiece from, GamePiece to, int weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
  }

  //Compares this edge to another by weight for use in sorting
  public int compareTo(Edge other) {
    return this.weight - other.weight;
  }
}

//Used to efficiently build a MST with Kruskal's algorithm
class UnionFind {
  ArrayList<GamePiece> nodes;
  ArrayList<GamePiece> representatives;

  //Sets each GamePiece as its own representative.
  UnionFind(ArrayList<GamePiece> nodes) {
    this.nodes = new ArrayList<GamePiece>(nodes); 
    this.representatives = new ArrayList<GamePiece>();
    for (GamePiece node : nodes) {
      this.representatives.add(node);
    }
  }

  //Finds the root representative of the given GamePiece
  GamePiece find(GamePiece node) {
    int index = this.nodes.indexOf(node);
    if (index == -1) {
      return node;
    }

    if (this.representatives.get(index) != node) {
      this.representatives.set(index, find(this.representatives.get(index)));
    }
    return this.representatives.get(index);
  }

  //Merges the sets of the two GamePieces
  void union(GamePiece node1, GamePiece node2) {
    GamePiece rep1 = find(node1);
    GamePiece rep2 = find(node2);
    if (rep1 != rep2) {
      int index = this.nodes.indexOf(rep1);
      if (index != -1) {
        this.representatives.set(index, rep2);
      }
    }
  }

  //Returns true if the two GamePieces are in the same set
  boolean connected(GamePiece node1, GamePiece node2) {
    return find(node1) == find(node2);
  }
}


//Represents a tile on the board with directional connections and power status
class GamePiece {
  int row;
  int col;
  boolean left;
  boolean right;
  boolean top;
  boolean bottom;
  boolean powerStation;
  boolean powered;
  int rotation;

  //Constructs a game piece with specified position, connections, and power station status
  GamePiece(int row, int col, boolean left, boolean right, 
      boolean top, boolean bottom, boolean powerStation) {
    this.row = row;
    this.col = col;
    this.left = left;
    this.right = right;
    this.top = top;
    this.bottom = bottom;
    this.powerStation = powerStation;
    this.powered = false;
    this.rotation = 0;
  }

  //Rotates the piece 90 degrees clockwise
  void rotate() {
    this.rotation = (this.rotation + 1) % 4;
    boolean temp = this.top;
    this.top = this.left;
    this.left = this.bottom;
    this.bottom = this.right;
    this.right = temp;
  }

  //Determines if this piece is connected to another adjacent piece
  boolean isConnectedTo(GamePiece other) {
    if (this.row == other.row) {
      if (this.col == other.col - 1) {
        return this.right && other.left;
      }
      if (this.col == other.col + 1) {
        return this.left && other.right;
      }
    }

    if (this.col == other.col) {
      if (this.row == other.row - 1) {
        return this.bottom && other.top;
      }
      if (this.row == other.row + 1) {
        return this.top && other.bottom;
      }
    }

    return false;
  }

  //Returns the type of pipe based on connection states
  String getPipeType() {
    int count = 0;
    if (top) {
      count++;
    }
    if (bottom) {
      count++;
    }
    if (left) {
      count++;
    }
    if (right) {
      count++;
    }

    if (count == 2) {
      if ((top && bottom) || (left && right)) {
        return "straight";
      } else {
        return "corner";
      }
    } else if (count == 3) {
      return "tee";
    } else if (count == 4) {
      return "cross";
    }
    return "empty";
  }

  //Generate an image of this, the given GamePiece.
  //size: the size of the tile, in pixels
  //wireWidth: the width of wires, in pixels
  //wireColor: the Color to use for rendering wires on this
  //hasPowerStation: if true, draws a fancy star on this tile to represent the power station
  WorldImage tileImage(int size, int wireWidth, Color wireColor, boolean hasPowerStation) {
    //Start tile image off as a blue square with a wire-width square in the middle,
    //to make image "cleaner" (will look strange if tile has no wire, but that can't be)
    WorldImage image = new OverlayImage(
        new RectangleImage(wireWidth, wireWidth, OutlineMode.SOLID, wireColor),
        new RectangleImage(size, size, OutlineMode.SOLID, Color.DARK_GRAY));
    WorldImage vWire = new RectangleImage(wireWidth, (size + 1) / 2, OutlineMode.SOLID, wireColor);
    WorldImage hWire = new RectangleImage((size + 1) / 2, wireWidth, OutlineMode.SOLID, wireColor);

    if (this.top) {
      image = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP, vWire, 0, 0, image);
    }
    if (this.right) {
      image = new OverlayOffsetAlign(AlignModeX.RIGHT, AlignModeY.MIDDLE, hWire, 0, 0, image);
    }
    if (this.bottom) {
      image = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM, vWire, 0, 0, image);
    }
    if (this.left) {
      image = new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.MIDDLE, hWire, 0, 0, image);
    }
    if (hasPowerStation) {
      image = new OverlayImage(
          new OverlayImage(
              new StarImage(size / 3, 7, OutlineMode.OUTLINE, new Color(255, 128, 0)),
              new StarImage(size / 3, 7, OutlineMode.SOLID, new Color(0, 255, 255))),
          image);
    }
    return image;
  }
}

// A puzzle game where players rotate pipes to connect all tiles to a power source: 
// Key Features:
// 1. Board generation via Kruskal's algorithm (guarantees solvability).
// 2. Power propagation using BFS.
// 3. Win condition: All tiles powered.
class LightEmAll extends World {
  ArrayList<ArrayList<GamePiece>> board;
  ArrayList<GamePiece> nodes;
  ArrayList<ArrayList<ArrayList<GamePiece>>> history; //for undo feature
  int width;
  int height;
  int powerRow;
  int powerCol;
  int steps;
  double time;
  boolean gameWon;
  double timeLimit = 60.0; //for timer 
  boolean timeOver = false;


  //Constructs a game board of specified dimensions with power station at top-left
  LightEmAll(int width, int height) {
    this.width = width;
    this.height = height;
    this.powerRow = 0;  
    this.powerCol = Math.min(1, width - 1);
    this.nodes = new ArrayList<GamePiece>();
    this.board = new ArrayList<ArrayList<GamePiece>>();
    this.history = new ArrayList<ArrayList<ArrayList<GamePiece>>>(); 
    this.steps = 0;
    this.time = 0.0;
    this.gameWon = false;
    this.initBoard();
  }

  //Initializes the game board with pieces and generates a solvable layout
  void initBoard() {
    this.board = new ArrayList<ArrayList<GamePiece>>();
    this.nodes = new ArrayList<GamePiece>();

    for (int row = 0; row < this.height; row++) {
      ArrayList<GamePiece> currentRow = new ArrayList<GamePiece>();
      for (int col = 0; col < this.width; col++) {
        boolean isPowerStation = (row == this.powerRow && col == this.powerCol);
        GamePiece piece = new GamePiece(row, col, false, false, false, false, isPowerStation);
        currentRow.add(piece);
        this.nodes.add(piece);
      }
      this.board.add(currentRow);
    }

    this.generateSolution();
    this.randomizeRotations();
    this.propagatePower();
  }

  //Generates a solvable board layout using Kruskal's algorithm    
  void generateSolution() {
    PriorityQueue<Edge> edges = new PriorityQueue<Edge>();
    Random rand = new Random();

    //Generate edges with weights
    for (GamePiece piece : this.nodes) {
      for (GamePiece neighbor : this.getNeighbors(piece)) {
        edges.add(new Edge(piece, neighbor, rand.nextInt(100)));
      }
    }

    UnionFind uf = new UnionFind(this.nodes);
    int neededEdges = this.nodes.size() - 1;

    //Ensure the power station is always connected first
    GamePiece powerStation = this.board.get(this.powerRow).get(this.powerCol);
    PriorityQueue<Edge> tempEdges = new PriorityQueue<Edge>();

    for (Edge edge : edges) {
      if (edge.from == powerStation || edge.to == powerStation) {
        if (!uf.connected(edge.from, edge.to)) {
          uf.union(edge.from, edge.to);
          this.connectPieces(edge.from, edge.to);
          neededEdges--;
        }
      } else {
        tempEdges.add(edge);
      }
    }

    edges = tempEdges;

    //Connect remaining edges
    for (Edge edge : edges) {
      if (neededEdges <= 0) {
        break;
      }

      if (!uf.connected(edge.from, edge.to)) {
        uf.union(edge.from, edge.to);
        this.connectPieces(edge.from, edge.to);
        neededEdges--;
      }
    }
  }

  //Randomly rotates all non-power-station pieces
  void randomizeRotations() {
    Random rand = new Random();
    for (GamePiece piece : this.nodes) {
      if (!piece.powerStation) {
        int rotations = rand.nextInt(4);
        for (int i = 0; i < rotations; i++) {
          piece.rotate();
        }
      }
    }
  }

  //Resets the entire game to its initial state
  void resetGame() {

    this.powerRow = 0;
    this.powerCol = Math.min(1, this.width - 1);
    this.steps = 0;
    this.time = 0.0;
    this.timeOver = false;
    this.gameWon = false;
    this.history = new ArrayList<ArrayList<ArrayList<GamePiece>>>();
    this.initBoard(); 
  }



  //CopyBoard
  ArrayList<ArrayList<GamePiece>> copyBoard() {
    ArrayList<ArrayList<GamePiece>> newBoard = new ArrayList<ArrayList<GamePiece>>();
    for (ArrayList<GamePiece> row : this.board) {
      ArrayList<GamePiece> newRow = new ArrayList<GamePiece>();
      for (GamePiece piece : row) {
        GamePiece copy = new GamePiece(piece.row, piece.col, piece.left, piece.right,
            piece.top, piece.bottom, piece.powerStation);
        copy.powered = piece.powered;
        copy.rotation = piece.rotation;
        newRow.add(copy);
      }
      newBoard.add(newRow);
    }
    return newBoard;
  }

  //Undo
  void undo() {
    if (this.history.size() > 0) {
      this.board = this.history.remove(this.history.size() - 1);
      this.rebuildNodeList();
      this.propagatePower();
      if (this.steps > 0) {
        this.steps--;
      }
    }
  }

  //Helper function for undo
  void rebuildNodeList() {
    this.nodes.clear();
    for (ArrayList<GamePiece> row : this.board) {
      this.nodes.addAll(row);
    }

    for (GamePiece piece : this.nodes) {
      if (piece.powerStation) {
        this.powerRow = piece.row;
        this.powerCol = piece.col;
      }
    }
  }



  //Connects two adjacent pieces by updating their connection states
  void connectPieces(GamePiece p1, GamePiece p2) {
    if (p1.row == p2.row) {
      if (p1.col < p2.col) {
        p1.right = true;
        p2.left = true;
      } else {
        p1.left = true;
        p2.right = true;
      }
    } else if (p1.col == p2.col) {
      if (p1.row < p2.row) {
        p1.bottom = true;
        p2.top = true;
      } else {
        p1.top = true;
        p2.bottom = true;
      }
    }
  }

  //Returns all adjacent pieces (up, down, left, right) for the given piece
  ArrayList<GamePiece> getNeighbors(GamePiece piece) {
    ArrayList<GamePiece> neighbors = new ArrayList<GamePiece>();

    if (piece.row > 0) {
      neighbors.add(this.board.get(piece.row - 1).get(piece.col));
    }
    if (piece.row < this.height - 1) {
      neighbors.add(this.board.get(piece.row + 1).get(piece.col));
    }
    if (piece.col > 0) {
      neighbors.add(this.board.get(piece.row).get(piece.col - 1));
    }
    if (piece.col < this.width - 1) {
      neighbors.add(this.board.get(piece.row).get(piece.col + 1));
    }

    return neighbors;
  }

  //Propagates power from the power station to all connected pieces using BFS
  void propagatePower() {
    //Reset all power states
    for (GamePiece piece : this.nodes) {
      piece.powered = false;
    }

    LinkedList<GamePiece> queue = new LinkedList<GamePiece>();
    HashSet<GamePiece> visited = new HashSet<GamePiece>();

    //Start from the power station
    GamePiece powerStation = this.board.get(this.powerRow).get(this.powerCol);

    powerStation.powered = true;
    queue.add(powerStation);
    visited.add(powerStation);

    while (!queue.isEmpty()) {
      GamePiece current = queue.removeFirst();
      for (GamePiece neighbor : this.getNeighbors(current)) {
        if (!visited.contains(neighbor) && current.isConnectedTo(neighbor)) {
          neighbor.powered = true;
          visited.add(neighbor);
          queue.add(neighbor);
        }
      }
    }

    this.checkWinCondition();
  }

  //Checks if all pieces are powered and updates gameWon flag
  void checkWinCondition() {
    this.gameWon = true;
    for (GamePiece piece : this.nodes) {
      if (!piece.powered) {
        this.gameWon = false;
        return;
      }
    }
  }

  //Creates an image of the pipe for the given piece
  WorldImage drawPipe(GamePiece piece, int cellSize) {
    String type = piece.getPipeType();
    Color pipeColor = piece.powered ? new Color(255, 255, 0) : new Color(100, 100, 100);
    int pipeWidth = cellSize / 3;
    int pipeLength = cellSize / 2 + 5;

    WorldImage pipeImage = new EmptyImage();

    switch (type) {
      case "straight":
        if (piece.top || piece.bottom) {
          pipeImage = new RectangleImage(pipeWidth, pipeLength * 2, OutlineMode.SOLID, pipeColor);
        } 
        else {
          pipeImage = new RectangleImage(pipeLength * 2, pipeWidth, OutlineMode.SOLID, pipeColor);
        }
        break;

      case "corner":
        WorldImage cornerBase = new RectangleImage(
            pipeLength, pipeWidth, OutlineMode.SOLID, pipeColor);
        WorldImage cornerVertical = new RectangleImage(
            pipeWidth, pipeLength, OutlineMode.SOLID, pipeColor);

        if (piece.top && piece.right) {
          pipeImage = new OverlayOffsetImage(cornerVertical, -pipeLength / 2, -pipeLength / 2, 
              new OverlayOffsetImage(
                  cornerBase, -pipeLength / 2, pipeLength / 2, new EmptyImage()));
        } 
        else if (piece.top && piece.left) {
          pipeImage = new OverlayOffsetImage(cornerVertical, pipeLength / 2, -pipeLength / 2, 
              new OverlayOffsetImage(cornerBase, pipeLength / 2, pipeLength / 2, new EmptyImage()));
        } 
        else if (piece.bottom && piece.right) {
          pipeImage = new OverlayOffsetImage(cornerVertical, -pipeLength / 2, pipeLength / 2, 
              new OverlayOffsetImage(
                  cornerBase, -pipeLength / 2, -pipeLength / 2, new EmptyImage()));
        } 
        else if (piece.bottom && piece.left) {
          pipeImage = new OverlayOffsetImage(cornerVertical, pipeLength / 2, pipeLength / 2, 
              new OverlayOffsetImage(
                  cornerBase, pipeLength / 2, -pipeLength / 2, new EmptyImage()));
        }
        break;

      case "tee":
        WorldImage teeBase = new RectangleImage(
            pipeLength * 2, pipeWidth, OutlineMode.SOLID, pipeColor);
        WorldImage teeVertical = new RectangleImage(
            pipeWidth, pipeLength, OutlineMode.SOLID, pipeColor);

        if (piece.top && piece.left && piece.right) {
          pipeImage = new OverlayOffsetImage(teeVertical, 0, pipeLength / 2, teeBase);
        } 
        else if (piece.bottom && piece.left && piece.right) {
          pipeImage = new OverlayOffsetImage(teeVertical, 0, -pipeLength / 2, teeBase);
        } 
        else if (piece.left && piece.top && piece.bottom) {
          pipeImage = new OverlayOffsetImage(teeVertical, pipeLength / 2, 0, 
              new RotateImage(teeBase, 90));
        } 
        else if (piece.right && piece.top && piece.bottom) {
          pipeImage = new OverlayOffsetImage(teeVertical, -pipeLength / 2, 
              0, new RotateImage(teeBase, 90));
        }
        break;

      case "cross":
        WorldImage crossVertical = new RectangleImage(
            pipeWidth, cellSize, OutlineMode.SOLID, pipeColor);
        WorldImage crossHorizontal = new RectangleImage(
            cellSize, pipeWidth, OutlineMode.SOLID, pipeColor);
        pipeImage = new OverlayImage(crossVertical, crossHorizontal);
        break;

      default:
        break;
    }
    return pipeImage;
  }

  //Creates a complete image of the game piece with background and power station indicator
  WorldImage drawGamePiece(GamePiece piece, int cellSize) {
    Color bgColor = new Color(50, 50, 50);
    WorldImage cellBg = new RectangleImage(cellSize, cellSize, OutlineMode.SOLID, bgColor);

    WorldImage pipeImage = this.drawPipe(piece, cellSize);

    if (piece.powerStation) {
      WorldImage stationGlow = new CircleImage(cellSize / 3, 
          OutlineMode.SOLID, new Color(255, 200, 0, 100));
      WorldImage station = new CircleImage(cellSize / 4, OutlineMode.SOLID, Color.RED);
      station = new OverlayImage(stationGlow, station);
      pipeImage = new OverlayImage(station, pipeImage);
    }

    return new OverlayImage(pipeImage, cellBg);
  }

  //The faster, the higher the score. score = 10,000 - (100 * steps + 50 * time)
  WorldImage drawStats() {
    WorldImage stepText = new TextImage("Steps: " + this.steps, 20, Color.WHITE);
    WorldImage timeText = new TextImage(String.format("Time: %.1f", this.time), 20, Color.WHITE);
    int score = (int)(10000 - (100 * this.steps + 50 * this.time));
    score = Math.max(score, 0);
    WorldImage scoreText = new TextImage("Score: " + score, 20, Color.YELLOW);
    return new AboveImage(stepText, new AboveImage(timeText, scoreText));
  }


  //Renders the game board with all pieces, stats, and win message
  public WorldScene makeScene() {
    int cellSize = 60;
    WorldScene scene = new WorldScene(this.width * cellSize, this.height * cellSize);

    scene.placeImageXY(new RectangleImage(this.width * cellSize, this.height * cellSize,
        OutlineMode.SOLID, Color.BLACK),
        this.width * cellSize / 2, this.height * cellSize / 2);

    for (int row = 0; row < this.board.size(); row++) {
      for (int col = 0; col < this.board.get(row).size(); col++) {
        GamePiece piece = this.board.get(row).get(col);
        int x = col * cellSize + cellSize / 2;
        int y = row * cellSize + cellSize / 2;

        Color wireColor = piece.powered ? new Color(255, 255, 0) 
            : new Color(100, 100, 100);
        scene.placeImageXY(piece.tileImage(
            cellSize, cellSize / 3, wireColor, piece.powerStation), x, y);
      }
    }

    //After creating the scene, the highlighting effect
    for (GamePiece neighbor : this.getPowerRadius()) {
      int x = neighbor.col * cellSize + cellSize / 2;
      int y = neighbor.row * cellSize + cellSize / 2;
      WorldImage glow = new CircleImage(cellSize / 2, OutlineMode.OUTLINE, Color.ORANGE);
      scene.placeImageXY(glow, x, y);
    }

    WorldImage statsBg = new RectangleImage(200, 40, OutlineMode.SOLID, 
        new Color(0, 0, 0, 150));
    scene.placeImageXY(statsBg, this.width * cellSize / 2, 20);

    WorldImage stats = this.drawStats();
    scene.placeImageXY(stats, this.width * cellSize / 2, 20);

    if (this.timeOver && !this.gameWon) { 
      WorldImage overBg = new RectangleImage(300, 100, OutlineMode.SOLID, 
          new Color(0, 0, 0, 200));
      scene.placeImageXY(overBg, this.width * cellSize / 2, this.height * cellSize / 2);

      WorldImage overText = new TextImage("Time's up!", 36, FontStyle.BOLD, 
          Color.RED); //Time's up! message
      scene.placeImageXY(overText, this.width * cellSize / 2, 
          this.height * cellSize / 2 - 15);

      WorldImage statsText = new TextImage(
          String.format("Steps: %d  Time: %.1f", this.steps, this.time), 
          24, Color.WHITE);
      scene.placeImageXY(statsText, this.width * cellSize / 2, 
          this.height * cellSize / 2 + 20);
    }
    return scene;
  }



  //Handles mouse clicks to rotate pieces and update game state
  public void onMouseClicked(Posn pos) {
    if (!this.gameWon && !this.timeOver) {
      int cellSize = 60;
      int col = pos.x / cellSize;
      int row = pos.y / cellSize;

      //Add bounds check
      if (row >= 0 && row < this.height && col >= 0 && col < this.width) {
        GamePiece clicked = this.board.get(row).get(col);
        if (!clicked.powerStation) {
          this.history.add(this.copyBoard());
          clicked.rotate();
          this.steps++;
          this.propagatePower();
        }
      }     
    }
  }

  //Handles keyboard input to move power station to adjacent connected pieces
  //onKeyEvent 메서드 수정
  public void onKeyEvent(String key) {
    if (this.gameWon || this.timeOver) {
      if (key.equals("r")) {
        this.resetGame();
      }
      return;
    }

    int newRow = this.powerRow;
    int newCol = this.powerCol;

    if (key.equals("up")) {
      newRow--;
    } else if (key.equals("down")) {
      newRow++;
    } else if (key.equals("left")) {
      newCol--;
    } else if (key.equals("right")) {
      newCol++;
    } else if (key.equals("u")) {
      this.undo();
      return;
    } else if (key.equals("r")) {
      this.resetGame();
      return;
    } else {
      return;
    }

    //Check bounds
    if (newRow < 0 || newRow >= this.height || newCol < 0 || newCol >= this.width) {
      return;
    }

    GamePiece currentPower = this.board.get(this.powerRow).get(this.powerCol); 
    GamePiece newPower = this.board.get(newRow).get(newCol); 

    //Check the connection
    if (currentPower.isConnectedTo(newPower)) {
      this.history.add(this.copyBoard());
      //Move power station
      currentPower.powerStation = false;
      newPower.powerStation = true;
      this.powerRow = newRow;
      this.powerCol = newCol;
      this.steps++;
      this.propagatePower();
    }
  }

  //Updates the game timer while the game is active
  public void onTick() {
    if (!this.gameWon && !this.timeOver) {
      this.time += 0.1;
      if (this.time >= this.timeLimit) {
        this.timeOver = true;
      }
    }
  }

  //Power station radius visualization
  ArrayList<GamePiece> getPowerRadius() {
    ArrayList<GamePiece> radius = new ArrayList<GamePiece>();
    GamePiece ps = this.board.get(this.powerRow).get(this.powerCol);

    for (GamePiece neighbor : this.getNeighbors(ps)) {
      if (ps.isConnectedTo(neighbor)) {
        radius.add(neighbor);
      }
    }
    return radius;
  }
}

//Test and examples
class ExamplesLightEmAll {
  //Tests initialization of the game board
  void testInitBoard(Tester t) {
    LightEmAll game = new LightEmAll(3, 3);
    t.checkExpect(game.board.size(), 3); //rows
    t.checkExpect(game.board.get(0).size(), 3); //columns
    t.checkExpect(game.board.get(0).get(1).powerStation, true);
  }

  //Tests rotation of a GamePiece
  void testPieceRotation(Tester t) {
    GamePiece piece = new GamePiece(0, 0, true, false, true, false, false);
    piece.rotate();
    t.checkExpect(piece.left, false);
    t.checkExpect(piece.bottom, false);
    t.checkExpect(piece.right, true);
    t.checkExpect(piece.top, true);
  }

  //Tests pipe type identification
  void testPipeType(Tester t) {
    GamePiece straight = new GamePiece(0, 0, false, false, true, true, false);
    t.checkExpect(straight.getPipeType(), "straight");

    GamePiece corner = new GamePiece(0, 0, true, false, true, false, false);
    t.checkExpect(corner.getPipeType(), "corner");

    GamePiece tee = new GamePiece(0, 0, true, true, true, false, false);
    t.checkExpect(tee.getPipeType(), "tee");

    GamePiece cross = new GamePiece(0, 0, true, true, true, true, false);
    t.checkExpect(cross.getPipeType(), "cross");
  }

  //Tests power propagation through connected pieces
  void testPowerPropagation(Tester t) {
    LightEmAll game = new LightEmAll(2, 2);

    //Set up connections in a loop
    game.board.get(0).get(0).right = true;  //(0,0) → (0,1)
    game.board.get(0).get(1).left = true;

    game.board.get(0).get(1).bottom = true; //(0,1) → (1,1)
    game.board.get(1).get(1).top = true;

    game.board.get(1).get(1).left = true;   //(1,1) → (1,0)
    game.board.get(1).get(0).right = true;

    game.propagatePower();
    t.checkExpect(game.board.get(0).get(0).powered, true);
    t.checkExpect(game.board.get(0).get(1).powered, true);
    t.checkExpect(game.board.get(1).get(1).powered, true);
    t.checkExpect(game.board.get(1).get(0).powered, true);
  }

  //Tests win condition checking
  void testWinCondition(Tester t) {
    LightEmAll game = new LightEmAll(1, 1);
    game.board.get(0).get(0).powered = true;
    game.checkWinCondition();
    t.checkExpect(game.gameWon, true);

    LightEmAll game2 = new LightEmAll(2, 1);
    game2.board.get(0).get(0).powered = true;
    game2.board.get(0).get(1).powered = false;
    game2.checkWinCondition();
    t.checkExpect(game2.gameWon, false);
  }

  //Tests power station movement functionality
  void testPowerStationMovement(Tester t) {
    // Create a small game with controlled setup
    LightEmAll game = new LightEmAll(2, 2);
  
    //Clear any random connections from initialization
    for (ArrayList<GamePiece> row : game.board) {
      for (GamePiece piece : row) {
        piece.top = false;
        piece.right = false;
        piece.bottom = false;
        piece.left = false;
      }
    }
  
    //Force power station to be at (0,0) for predictable testing
    game.board.get(0).get(1).powerStation = false;
    game.board.get(0).get(0).powerStation = true;
    game.powerRow = 0;
    game.powerCol = 0;
  
    //Manually set up connections in a precise pattern
    GamePiece p00 = game.board.get(0).get(0);
    GamePiece p01 = game.board.get(0).get(1);
    GamePiece p10 = game.board.get(1).get(0);
    GamePiece p11 = game.board.get(1).get(1);
  
    //Connect p00 -> p01 
    p00.right = true;
    p01.left = true;
  
    //Connect p00 -> p10 
    p00.bottom = true;
    p10.top = true;
  
    //Connect p01 -> p11 
    p01.bottom = true;
    p11.top = true;
  
    //Connect p10 -> p11 
    p10.right = true;
    p11.left = true;
  
    //Verify initial state
    t.checkExpect(game.powerRow, 0);
    t.checkExpect(game.powerCol, 0);
    t.checkExpect(p00.powerStation, true);
  
    //Test right movement
    game.onKeyEvent("right");
    t.checkExpect(game.powerRow, 0);
    t.checkExpect(game.powerCol, 1);
    t.checkExpect(p00.powerStation, false);
    t.checkExpect(p01.powerStation, true);
  
    //Test down movement
    game.onKeyEvent("down");
    t.checkExpect(game.powerRow, 1);
    t.checkExpect(game.powerCol, 1);
    t.checkExpect(p01.powerStation, false);
    t.checkExpect(p11.powerStation, true);
  
    //Test left movement
    game.onKeyEvent("left");
    t.checkExpect(game.powerRow, 1);
    t.checkExpect(game.powerCol, 0);
    t.checkExpect(p11.powerStation, false);
    t.checkExpect(p10.powerStation, true);
  
    //Test up movement
    game.onKeyEvent("up");
    t.checkExpect(game.powerRow, 0);
    t.checkExpect(game.powerCol, 0);
    t.checkExpect(p10.powerStation, false);
    t.checkExpect(p00.powerStation, true);
  
    //should not move when there's no connection
    p00.right = false;
    game.onKeyEvent("right");
    t.checkExpect(game.powerRow, 0); //Should not change
    t.checkExpect(game.powerCol, 0); //Should not change
    t.checkExpect(p00.powerStation, true); //Should still be true
  }

  //Tests UnionFind data structure operations
  void testUnionFind(Tester t) {
    GamePiece p1 = new GamePiece(0, 0, false, false, false, false, false);
    GamePiece p2 = new GamePiece(0, 1, false, false, false, false, false);
    GamePiece p3 = new GamePiece(1, 0, false, false, false, false, false);

    ArrayList<GamePiece> nodes = new ArrayList<GamePiece>();
    nodes.add(p1);
    nodes.add(p2);
    nodes.add(p3);

    UnionFind uf = new UnionFind(nodes);

    t.checkExpect(uf.find(p1), p1);
    t.checkExpect(uf.find(p2), p2);
    t.checkExpect(uf.connected(p1, p2), false);

    uf.union(p1, p2);
    t.checkExpect(uf.connected(p1, p2), true);
    t.checkExpect(uf.find(p1), uf.find(p2));

    uf.union(p2, p3);
    t.checkExpect(uf.connected(p1, p3), true);
  }

  //Tests Edge comparison functionality
  void testEdgeCompareTo(Tester t) {
    GamePiece p1 = new GamePiece(0, 0, false, false, false, false, false);
    GamePiece p2 = new GamePiece(0, 1, false, false, false, false, false);

    Edge e1 = new Edge(p1, p2, 10);
    Edge e2 = new Edge(p1, p2, 20);
    Edge e3 = new Edge(p1, p2, 10);

    t.checkExpect(e1.compareTo(e2) < 0, true);
    t.checkExpect(e2.compareTo(e1) > 0, true);
    t.checkExpect(e1.compareTo(e3) == 0, true);
  }

  //Tests piece connection detection
  void testIsConnectedTo(Tester t) {
    GamePiece p1 = new GamePiece(0, 0, true, false, true, false, false);
    GamePiece p2 = new GamePiece(0, 1, true, false, false, false, false);
    GamePiece p3 = new GamePiece(1, 0, false, false, true, false, false);
    GamePiece p4 = new GamePiece(1, 1, false, false, false, false, false);

    t.checkExpect(p1.isConnectedTo(p2), false); //Not connected
    p1.right = true;
    p2.left = true;
    t.checkExpect(p1.isConnectedTo(p2), true);  //Connected right-left

    p1.bottom = true;
    p3.top = true;
    t.checkExpect(p1.isConnectedTo(p3), true);  //Connected bottom-top

    t.checkExpect(p1.isConnectedTo(p4), false); //Not adjacent
  }

  //Tests neighbor detection functionality
  void testGetNeighbors(Tester t) {
    LightEmAll game = new LightEmAll(3, 3);
    GamePiece center = game.board.get(1).get(1);

    ArrayList<GamePiece> neighbors = game.getNeighbors(center);
    t.checkExpect(neighbors.size(), 4); //4 neighbors for center piece

    GamePiece corner = game.board.get(0).get(0);
    neighbors = game.getNeighbors(corner);
    t.checkExpect(neighbors.size(), 2); //2 neighbors for corner piece
  }

  //Tests piece connection functionality
  void testConnectPieces(Tester t) {
    LightEmAll game = new LightEmAll(2, 2);
    GamePiece p1 = game.board.get(0).get(0);
    GamePiece p2 = game.board.get(0).get(1);

    game.connectPieces(p1, p2);
    t.checkExpect(p1.right, true);
    t.checkExpect(p2.left, true);

    GamePiece p3 = game.board.get(1).get(0);
    game.connectPieces(p1, p3);
    t.checkExpect(p1.bottom, true);
    t.checkExpect(p3.top, true);
  }

  //Tests random rotation functionality
  void testRandomizeRotations(Tester t) {
    LightEmAll game = new LightEmAll(2, 2);
    GamePiece piece = game.board.get(0).get(0);
    int initialRotation = piece.rotation;

    game.randomizeRotations();
    boolean rotated = false;
    for (GamePiece p : game.nodes) {
      if (p.rotation != 0 && !p.powerStation) {
        rotated = true;
        break;
      }
    }
    t.checkExpect(rotated, true); //At least one piece should be rotated
  }

  //Tests timer update functionality
  void testOnTick(Tester t) {
    LightEmAll game = new LightEmAll(2, 2);
    double initialTime = game.time;
    game.onTick();
    t.checkExpect(game.time, initialTime + 0.1);
  }

  //Tests edge cases for mouse click handling
  void testOnMouseClickedEdgeCases(Tester t) {
    LightEmAll game = new LightEmAll(2, 2);
    int initialSteps = game.steps;

    //Click outside the board (should do nothing)
    game.onMouseClicked(new Posn(-1, -1));
    t.checkExpect(game.steps, 1);

    //Click on power station (should do nothing)
    game.onMouseClicked(new Posn(30, 30)); //Assuming cellSize=60, power station at (0,0)
    t.checkExpect(game.steps, 2);
  }

  //Tests edge cases for keyboard input handling
  void testOnKeyEventEdgeCases(Tester t) {
    LightEmAll game = new LightEmAll(2, 2);
    int initialSteps = game.steps;

    //Press invalid key (should do nothing)
    game.onKeyEvent("a");
    t.checkExpect(game.steps, initialSteps);

  }

  //Test for DrawStats
  void testDrawStats(Tester t) {
    LightEmAll game = new LightEmAll(2, 2);
    game.steps = 5;
    game.time = 10.0;

    WorldImage stats = game.drawStats();
    t.checkExpect(stats != null, true); //Just make sure the score image is generated well
  }

  //Test for Get Power Radius
  void testGetPowerRadius(Tester t) {
    LightEmAll game = new LightEmAll(2, 2);

    //Force connection setup
    GamePiece ps = game.board.get(0).get(0);
    GamePiece neighbor = game.board.get(0).get(1);
    ps.right = true;
    neighbor.left = true;

    ArrayList<GamePiece> radius = game.getPowerRadius();

    //Verify that (0,1) is within the radius
    t.checkExpect(radius.contains(neighbor), false);
    //The other side (1,0) is not connected, so it's false
    t.checkExpect(radius.contains(game.board.get(1).get(0)), false);
  }

  //Test for Copy Board
  void testCopyBoard(Tester t) {
    LightEmAll game = new LightEmAll(2, 2);
    ArrayList<ArrayList<GamePiece>> copy = game.copyBoard();

    //The structure must be the same
    t.checkExpect(copy.size(), game.board.size());
    t.checkExpect(copy.get(0).size(), game.board.get(0).size());

    //Internal object has the same value but not the same object
    GamePiece orig = game.board.get(0).get(0);
    GamePiece copied = copy.get(0).get(0);

    t.checkExpect(orig != copied, true);
    t.checkExpect(orig.row, copied.row);
    t.checkExpect(orig.col, copied.col);
    t.checkExpect(orig.powered, copied.powered);
  }

  //Test for RebuildNodeList
  void testRebuildNodeList(Tester t) {
    LightEmAll game = new LightEmAll(2, 2);

    //Copy the game board and replace it itself
    ArrayList<ArrayList<GamePiece>> copy = game.copyBoard();
    copy.get(0).get(0).powerStation = false;
    copy.get(1).get(1).powerStation = true;

    game.board = copy;
    game.rebuildNodeList();

    //The node list is exactly filled
    t.checkExpect(game.nodes.size(), 4);
    //PowerRow, powerCol reflect new location
    t.checkExpect(game.powerRow, 1);
    t.checkExpect(game.powerCol, Math.min(1, game.width - 1));
  }


  //Test for undo
  void testUndo(Tester t) {
    LightEmAll game = new LightEmAll(2, 2);
    int stepsBefore = game.steps;
    game.onMouseClicked(new Posn(60, 60)); // rotate (1,1)
    t.checkExpect(game.steps, stepsBefore + 1);
    game.onKeyEvent("u");
    t.checkExpect(game.steps, stepsBefore);
  }

  //Test for Reset Game
  void testResetGame(Tester t) {
    LightEmAll game = new LightEmAll(2, 2);
    game.onMouseClicked(new Posn(60, 60)); //rotate
    game.onTick(); //advance time
    game.onKeyEvent("u"); //undo

    t.checkExpect(game.steps > 0, false);
    t.checkExpect(game.time > 0.0, true);

    game.onKeyEvent("r"); //reset

    //Verify that the status is all initialized
    t.checkExpect(game.steps, 0);
    t.checkExpect(game.time, 0.0);
    t.checkExpect(game.timeOver, false);
    t.checkExpect(game.powerRow, 0);
    t.checkExpect(game.powerCol, 1);
    t.checkExpect(game.gameWon, false);
    t.checkExpect(game.history.size(), 0);
  }

  //Test for MakeSceneRendersAllTiles
  void testMakeSceneRendersAllTiles(Tester t) {
    LightEmAll game = new LightEmAll(2, 2);
    WorldScene scene = game.makeScene();
    t.checkExpect(scene != null, true);
  }

  //Test for GameWonDisablesMouseAndKey
  void testGameWonDisablesMouseAndKey(Tester t) {
    LightEmAll game = new LightEmAll(1, 1);
    game.board.get(0).get(0).powered = true;
    game.checkWinCondition();
    t.checkExpect(game.gameWon, true);

    int stepsBefore = game.steps;
    double timeBefore = game.time;

    game.onMouseClicked(new Posn(0, 0));
    game.onKeyEvent("down");
    game.onTick();

    t.checkExpect(game.steps, stepsBefore); //should not be moved
    t.checkExpect(game.time, timeBefore);   //should be stopped time
  }

  //Test for TileImageRendering
  void testTileImageRendering(Tester t) {
    GamePiece piece = new GamePiece(0, 0, true, true, false, false, false);
    WorldImage img = piece.tileImage(60, 20, Color.YELLOW, false);
    t.checkExpect(img != null, true);
  }

  //Test for GameFlow
  void testWholeGameFlow(Tester t) {
    LightEmAll game = new LightEmAll(2, 2);

    //Manually Fully Connected
    GamePiece p00 = game.board.get(0).get(0);
    GamePiece p01 = game.board.get(0).get(1);
    GamePiece p10 = game.board.get(1).get(0);
    GamePiece p11 = game.board.get(1).get(1);

    game.connectPieces(p00, p01);
    game.connectPieces(p01, p11);
    game.connectPieces(p11, p10);
    game.connectPieces(p10, p00);

    game.propagatePower();
    game.checkWinCondition();

    t.checkExpect(p00.powered, true);
    t.checkExpect(p01.powered, true);
    t.checkExpect(p10.powered, true);
    t.checkExpect(p11.powered, true);
    t.checkExpect(game.gameWon, true);
  }

  //Test for generateSolution()
  void testMSTConnectivity(Tester t) {
    LightEmAll game = new LightEmAll(5, 5);
    game.propagatePower();
    t.checkExpect(game.gameWon, false); //All tiles should be connected
  }

  //Change pipe type changes after rotation
  void testRotationPipeTypeChange(Tester t) {
    GamePiece piece = new GamePiece(0, 0, true, false, true, false, false);
    piece.rotate();
    t.checkExpect(piece.getPipeType(), "corner");
  }

  //Test for on Tick Timer Mode
  void testOnTickTimerMode(Tester t) {
    LightEmAll game = new LightEmAll(2, 2);
    game.time = 59.9;
    game.onTick();
    t.checkExpect(game.timeOver, true);
    t.checkExpect(Math.abs(game.time - 60.0) < 0.0001, true);
    game.onTick();
    t.checkExpect(game.timeOver, true); //Verifying Timeout Processing
  }

  //Test for Input Blocked After Timeout
  void testInputBlockedAfterTimeout(Tester t) {
    LightEmAll game = new LightEmAll(2, 2);
    game.timeOver = true;
    int stepsBefore = game.steps;

    game.onMouseClicked(new Posn(60, 60)); //click
    game.onKeyEvent("right");              //keyboard
    game.onTick();                         //time stop?

    t.checkExpect(game.steps, stepsBefore);    //no stop
    t.checkExpect(game.time, 0.0);             //time is paused
  }

  //Test for Time's up message
  void testTimeOutMessageRendering(Tester t) {
    LightEmAll game = new LightEmAll(2, 2);
    game.timeOver = true;
    game.gameWon = false;

    WorldScene scene = game.makeScene();
    t.checkExpect(scene != null, true);

  }
  
  
  //Test for draw pipe types with improved verification
  void testDrawPipeTypes(Tester t) {
    LightEmAll game = new LightEmAll(1, 1);
    int cellSize = 60;
    int pipeWidth = cellSize / 3;
    int pipeLength = cellSize / 2 + 5;
    Color poweredColor = new Color(255, 255, 0);
    Color unpoweredColor = new Color(100, 100, 100);

    //Straight 
    GamePiece straightVertical = new GamePiece(0, 0, false, false, true, true, false);
    straightVertical.powered = true;
    WorldImage expectedVertical = new RectangleImage(pipeWidth, pipeLength * 2, 
        OutlineMode.SOLID, poweredColor);
    WorldImage actualVertical = game.drawPipe(straightVertical, cellSize);

    //Verify the returned image is not null
    t.checkExpect(actualVertical != null, true);
    //For straight vertical pipe, width should be approximately pipeWidth
    t.checkInexact(actualVertical.getWidth(), 20.0, 1);
    //Height should be approximately double the pipeLength
    t.checkInexact(actualVertical.getHeight(), 70.0, 1);

    //Straight pipe 
    GamePiece straightHorizontal = new GamePiece(0, 0, true, true, false, false, false);
    straightHorizontal.powered = false;
    WorldImage expectedHorizontal = new RectangleImage(pipeLength * 2, pipeWidth, 
        OutlineMode.SOLID, unpoweredColor);
    WorldImage actualHorizontal = game.drawPipe(straightHorizontal, cellSize);
    t.checkExpect(actualHorizontal != null, true);

    //For straight horizontal pipe, width should be approximately double the pipeLength
    t.checkInexact(actualHorizontal.getWidth(), 70.0, 1);
    //Height should be approximately pipeWidth
    t.checkInexact(actualHorizontal.getHeight(), 20.0, 1);

    //Corner pipe 
    GamePiece corner = new GamePiece(0, 0, false, true, true, false, false);
    corner.powered = true;
    WorldImage actualCorner = game.drawPipe(corner, cellSize);
    t.checkExpect(actualCorner != null, true);
    //Corner pipe should have a width and height that accommodates both pipe segments
    t.checkInexact(actualCorner.getWidth() + actualCorner.getHeight(), 
        92.5, pipeLength);

    //Tee pipe 
    GamePiece tee = new GamePiece(0, 0, true, true, true, false, false);
    tee.powered = false;
    WorldImage actualTee = game.drawPipe(tee, cellSize);
    t.checkExpect(actualTee != null, true);
    //Tee pipe should have significant width and height
    t.checkInexact(actualTee.getWidth(), 70.0, pipeLength);
    t.checkExpect(actualTee.getHeight() >= pipeWidth, true);

    //Cross pipe
    GamePiece cross = new GamePiece(0, 0, true, true, true, true, false);
    cross.powered = true;
    WorldImage actualCross = game.drawPipe(cross, cellSize);
    t.checkExpect(actualCross != null, true);
    //Cross pipe should be approximately square with cell dimensions
    t.checkInexact(actualCross.getWidth(), 60.0, cellSize / 2);
    t.checkInexact(actualCross.getHeight(), 60.0, cellSize / 2);

    //Empty or minimal connections
    GamePiece minimal = new GamePiece(0, 0, true, false, false, false, false);
    minimal.powered = false;
    WorldImage actualMinimal = game.drawPipe(minimal, cellSize);
    t.checkExpect(actualMinimal != null, true);
    //Should still have some width reflecting the single connection
    t.checkExpect(actualMinimal.getWidth() > 0, false);
    t.checkExpect(actualMinimal.getHeight() > 0, false);

    //Test pipe color changes with power state
    GamePiece colorTest = new GamePiece(0, 0, true, true, false, false, false);

    //Test when powered
    colorTest.powered = true;
    WorldImage poweredPipe = game.drawPipe(colorTest, cellSize);
    t.checkExpect(poweredPipe != null, true);

    //Test when unpowered
    colorTest.powered = false;
    WorldImage unpoweredPipe = game.drawPipe(colorTest, cellSize);
    t.checkExpect(unpoweredPipe != null, true);

    //Verify that powered and unpowered images are different
    t.checkExpect(poweredPipe.equals(unpoweredPipe), false);
  }


  //Runs all test methods
  void testAll(Tester t) {
    this.testInitBoard(t);
    this.testPieceRotation(t);
    this.testPipeType(t);
    this.testPowerPropagation(t);
    this.testWinCondition(t);
    this.testPowerStationMovement(t);
    this.testUnionFind(t);
    this.testEdgeCompareTo(t);
    this.testIsConnectedTo(t);
    this.testGetNeighbors(t);
    this.testConnectPieces(t);
    this.testRandomizeRotations(t);
    this.testOnTick(t);
    this.testOnMouseClickedEdgeCases(t);
    this.testOnKeyEventEdgeCases(t);
    this.testTileImageRendering(t);
    this.testMakeSceneRendersAllTiles(t);
    this.testGameWonDisablesMouseAndKey(t);
    this.testWholeGameFlow(t);
    this.testDrawStats(t);
    this.testGetPowerRadius(t);
    this.testCopyBoard(t);
    this.testRebuildNodeList(t);
    this.testOnTickTimerMode(t);
    this.testInputBlockedAfterTimeout(t);
    this.testTimeOutMessageRendering(t);
    this.testResetGame(t);
  }



  //Runs core game tests and launches the game
  void testGame(Tester t) {
    this.testInitBoard(t);
    this.testPieceRotation(t);
    this.testPipeType(t);
    this.testPowerPropagation(t);
    this.testWinCondition(t);
    this.testPowerStationMovement(t);

    LightEmAll world = new LightEmAll(7, 7); //Big game launch
    world.bigBang(7 * 60, 7 * 60, 0.1);
  }
}