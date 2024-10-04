import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import tester.*;
import java.awt.Color;
import javalib.worldimages.*;
import javalib.impworld.*;

////////////////////READ/////////////////////////////////
/////////////////////ME//////////////////////////////////

//EXTRA CREDITS//

/*1. Enhanced the graphic and gameplay, includes:
 *  - Implemented so that the first click the player plays
 *    will never be a bomb, but will always be a floodfil instead
 *  - Forming 3D cells
 *  - Checking win and lose condition and pop up screen accordingly
 *
 *2. Restart the game when clicked in the box in the middle
 * 
 *3. Selecting levels of difficulty: IN PROGRESS
 *  "h" = hard --> 16x30 99 bombs
 *  "m" = medium --> 16x30 69 bombs
 *  "e" = easy --> 16x30 39 bombs

 */

////////////////////READ/////////////////////////////////
/////////////////////ME/////////////////////////////////

//interface to hold the constants
interface Constants {
  int CELL_SIZE = 30;
  int COLUMNS = 30;
  int ROWS = 16;
  int BOMBS = 1;

  double FONT = CELL_SIZE / 1.5;

  int WORLDWIDTH = CELL_SIZE * COLUMNS;
  int WORLDHEIGHT = CELL_SIZE * ROWS;
  double TICKRATE = 0.15;

  int RESTART_BUTTON_WIDTH = CELL_SIZE * 6;
  int RESTART_BUTTON_HEIGHT = CELL_SIZE * 2;

  // triangle to blend the top corner
  TriangleImage WHITE_TRIANGLE = new TriangleImage(new Posn(0, 0), new Posn(CELL_SIZE / 7, 0),
      new Posn(0, CELL_SIZE / 7), OutlineMode.SOLID, Color.WHITE);

  RectangleImage BACK_TOP = new RectangleImage(CELL_SIZE / 7, CELL_SIZE / 7, OutlineMode.SOLID,
      Color.DARK_GRAY);

  OverlayImage BLEND_TOP = new OverlayImage(WHITE_TRIANGLE, BACK_TOP);

  // triangle to blend the bottom corner
  TriangleImage GRAY_TRIANGLE = new TriangleImage(new Posn(CELL_SIZE / 7, CELL_SIZE / 7),
      new Posn(CELL_SIZE / 7, 0), new Posn(0, CELL_SIZE / 7), OutlineMode.SOLID, Color.DARK_GRAY);

  RectangleImage BACK_BOT = new RectangleImage(CELL_SIZE / 7, CELL_SIZE / 7, OutlineMode.SOLID,
      Color.WHITE);

  OverlayImage BLEND_BOT = new OverlayImage(WHITE_TRIANGLE, BACK_TOP);

  // shading the unrevealed shape
  RectangleImage BOT = new RectangleImage(CELL_SIZE, CELL_SIZE / 7, OutlineMode.SOLID,
      Color.DARK_GRAY);

  RectangleImage RIGHT = new RectangleImage(CELL_SIZE / 7, CELL_SIZE, OutlineMode.SOLID,
      Color.DARK_GRAY);

  RectangleImage TOP = new RectangleImage(CELL_SIZE, CELL_SIZE / 8, OutlineMode.SOLID, Color.WHITE);

  RectangleImage LEFT = new RectangleImage(CELL_SIZE / 8, CELL_SIZE, OutlineMode.SOLID,
      Color.WHITE);

  RectangleImage UNREVEALED_BACKGROUND = new RectangleImage(CELL_SIZE, CELL_SIZE, OutlineMode.SOLID,
      Color.GRAY);

  OverlayOffsetImage UNREVEALED = new OverlayOffsetImage(BLEND_BOT,
      (CELL_SIZE - CELL_SIZE / 7) / 2 + CELL_SIZE * 0.003,
      -(CELL_SIZE - CELL_SIZE / 7) / 2 + CELL_SIZE * 0.003,
      new OverlayOffsetImage(BLEND_TOP, -(CELL_SIZE - CELL_SIZE / 7) / 2 + CELL_SIZE * 0.003,
          (CELL_SIZE - CELL_SIZE / 7) / 2 + CELL_SIZE * 0.003,
          new OverlayOffsetImage(BOT, 0, -CELL_SIZE / 2.23,
              new OverlayOffsetImage(RIGHT, -CELL_SIZE / 2.23, 0,
                  new OverlayOffsetImage(LEFT, CELL_SIZE / 2.23, 0,
                      new OverlayOffsetImage(TOP, 0, CELL_SIZE / 2.23, UNREVEALED_BACKGROUND))))));

  RectangleImage REVEALED = new RectangleImage(CELL_SIZE, CELL_SIZE, OutlineMode.SOLID, Color.GRAY);

  CircleImage FLAGSTAR = new CircleImage(CELL_SIZE / 4, OutlineMode.SOLID, Color.WHITE);

  OverlayImage BOMB = new OverlayImage(new CircleImage(CELL_SIZE / 7, OutlineMode.SOLID, Color.RED),
      new CircleImage(CELL_SIZE / 3, OutlineMode.SOLID, Color.BLACK));

  RectangleImage OUTLINE = new RectangleImage(CELL_SIZE, CELL_SIZE, OutlineMode.OUTLINE,
      Color.DARK_GRAY);

  EquilateralTriangleImage END = new EquilateralTriangleImage(CELL_SIZE, OutlineMode.SOLID,
      Color.RED);

}

// Represents the game Minesweeper
class MinesweeperWorld extends World {
  int rows;
  int columns;
  int minesCount;
  ArrayList<ArrayList<Cell>> board;
  Random rand;
  boolean gameOver = false;
  boolean restartGame = false;
  boolean firstClick = true;

  // The Constructor
  MinesweeperWorld(int rows, int columns, int minesCount) {
    Utils u = new Utils();

    // negative/0 row or columns can't form a grid
    this.rows = u.check(rows, 1000, "A Grid Can Not Be Formed");
    this.columns = u.check(columns, 1000, "A Grid Can Not Be Formed");

    // number of bombs can not be equal or larger to the grid itself
    this.minesCount = u.check(minesCount, rows * columns, "Bombs OverFlow");

    this.rand = new Random();
    this.board = new ArrayList<ArrayList<Cell>>();

    initializeBoard();
  }

  // The Constructor For Testing the board
  MinesweeperWorld(int rows, int columns, int minesCount, ArrayList<ArrayList<Cell>> board) {
    Utils u = new Utils();

    // negative/0 row or columns can't form a grid
    this.rows = u.check(rows, 1000, "A Grid Can Not Be Formed");
    this.columns = u.check(columns, 1000, "A Grid Can Not Be Formed");

    // number of bombs can not be equal or larger to the grid itself
    this.minesCount = u.check(minesCount, rows * columns, "Bombs OverFlow");

    this.rand = new Random();
    this.board = board;

    initializeBoard();
  }

  // Initialize the board, making the grid
  void initializeBoard() {
    for (int r = 0; r < rows; r++) {
      ArrayList<Cell> row = new ArrayList<>();
      for (int c = 0; c < columns; c++) {
        row.add(new Cell(false));
      }
      this.board.add(row);
    }
  }

  // Link the cells together when the game is initialized
  // so that every cell has a list of its neighbors.
  // be aware of the boundaries - multiple if statements
  void linkNeighbors() {
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        Cell currentCell = board.get(r).get(c);

        for (int y = -1; y <= 1; y++) {
          for (int x = -1; x <= 1; x++) {
            // Skip the current cell
            if (y != 0 || x != 0) {
              int neighborYCoord = r + y;
              int neighborXCoord = c + x;

              // Check if the neighboring cell is within bounds
              if (isWithinGrid(neighborYCoord, neighborXCoord)) {
                Cell neighbor = board.get(neighborYCoord).get(neighborXCoord);
                currentCell.addNeighbor(neighbor);
              }
            }
          }
        }
      }
    }
  }

  // Check if a cell position is within the current grid
  boolean isWithinGrid(int r, int c) {
    return (r >= 0 && r < rows) && (c >= 0 && c < columns);
  }

  // Place mines randomly on the grid
  // does not repeat the mines placed
  void placeMines(int rowClick, int colClick) {
    int minesPlaced = 0;

    while (minesPlaced < this.minesCount) {
      int r = rand.nextInt(rows);
      int c = rand.nextInt(columns);

      // look into the neighbor cells
      // need to use math.abs because
      // the click can be negative,
      // which messes up with the result of a click
      boolean isCellNeighborOfFirstClick = Math.abs(rowClick - r) <= 1
          && Math.abs(colClick - c) <= 1;

      if (!this.board.get(r).get(c).isMine && !isCellNeighborOfFirstClick
          && !this.board.get(r).get(c).isRevealed) {
        this.board.get(r).set(c, new Cell(true));
        minesPlaced++;
      }
    }
  }

  // Draws the world scene
  // for every row and column, add the cell at the specified position
  public WorldScene makeScene() {
    WorldScene scene = this.getEmptyScene();
    for (int r = 0; r < this.rows; r++) {
      for (int c = 0; c < this.columns; c++) {
        Cell cell = this.board.get(r).get(c);
        WorldImage cellImage = cell.renderCell();

        int x = c * Constants.CELL_SIZE + Constants.CELL_SIZE / 2;
        int y = r * Constants.CELL_SIZE + Constants.CELL_SIZE / 2;

        scene.placeImageXY(cellImage, x, y);
      }
    }

    if (gameOver) {
      WorldImage end = new OverlayImage(
          new AboveImage(winOrLose(),
              new TextImage("RESTART", Constants.CELL_SIZE / 2, FontStyle.BOLD, Color.WHITE)),
          new RectangleImage(Constants.CELL_SIZE * 6, Constants.CELL_SIZE * 2, OutlineMode.SOLID,
              Color.BLACK));

      scene.placeImageXY(end, Constants.WORLDWIDTH / 2, Constants.WORLDHEIGHT / 2);
      this.restartGame = true;
      return scene;

    }
    return scene;
  }

  // Print the corresponding message for when a player win or loses
  public TextImage winOrLose() {
    if (win()) {
      return new TextImage("YOU WON", Constants.CELL_SIZE, FontStyle.BOLD, Color.GREEN);
    }
    else {
      return new TextImage("LOSER", Constants.CELL_SIZE, FontStyle.BOLD, Color.RED);
    }
  }

  // checking if the player has won
  // by seeing if all the mines on the field are flagged
  public boolean win() {
    for (ArrayList<Cell> arrCell : board) {
      for (Cell cell : arrCell) {
        if (cell.isMine && !cell.isFlagged) {
          return false;
        }
      }
    }
    return true;
  }

  // checking the condition of the mouse clicked
  public void onMouseClicked(Posn posn, String button) {
    int x = posn.x / Constants.CELL_SIZE;
    int y = posn.y / Constants.CELL_SIZE;
    Cell clickedCell = board.get(y).get(x);

    boolean withinXBounds = posn.x >= Constants.WORLDWIDTH / 2 - Constants.RESTART_BUTTON_WIDTH / 2
        && posn.x <= Constants.WORLDWIDTH / 2 + Constants.RESTART_BUTTON_WIDTH / 2;

    boolean withinYBounds = posn.y >= Constants.WORLDHEIGHT / 2
        - Constants.RESTART_BUTTON_HEIGHT / 2
        && posn.y <= Constants.WORLDHEIGHT / 2 + Constants.RESTART_BUTTON_HEIGHT / 2;

    if (button.equals("LeftButton")) {
      // check condition for the first click
      // and never let the player lose here
      if (firstClick) {
        firstClick = false;
        placeMines(y, x);
        linkNeighbors();
      }

      // the end game condition
      if (clickedCell.isMine) {
        // an endgame will be poped up here if the player presses on a mine
        this.gameOver = true;
        gameEnd();

        // when clicked on a cell with 0 neighbor mines
      }
      else if (clickedCell.countNeighborMines() == 0) {
        floodFill(clickedCell);

        // flag and unflag a cell
      }
      else if (!clickedCell.isFlagged) {
        clickedCell.isRevealed = true;

        // check to see if conditions match for the game to restart
      }
      if (gameOver && restartGame) {
        if (withinXBounds && withinYBounds) {
          this.restartGame = !restartGame;
          restartGame();
        }
      }
    }

    else if (button.equals("RightButton")) {
      clickedCell.isFlagged = !clickedCell.isFlagged;
      // after flagging/unflagging, check if the game is done, and win.
      if (win()) {
        gameOver = true;
      }
    }
  }

  // defining the world ended, and exposes all the mines on the field
  public void gameEnd() {
    if (gameOver) {
      for (ArrayList<Cell> arrCell : board) {
        for (Cell cell : arrCell) {
          if (cell.isMine) {
            cell.isRevealed = true;
          }
        }
      }
    }
  }

  // restart the game, initialize the game again
  // by clearing the board,
  // setting gameOver condition to false again
  // placing mines
  // draw the grid
  // link all the cell's neighbors
  void restartGame() {
    this.board.clear();
    this.gameOver = false;
    initializeBoard();
    this.firstClick = true;
    linkNeighbors();
  }

  // EFFECT: when the cell has no bomb neighbors
  // clear out all cells nearby until it hits a mine
  public void floodFill(Cell cell) {
    if (!cell.isRevealed && !cell.isMine && !cell.isFlagged) {
      cell.isRevealed = true;

      if (cell.countNeighborMines() == 0) {
        for (Cell c : cell.neighbors) {
          floodFill(c);

        }
      }
    }
  }

}

// Represents the state of a cell
class Cell {
  boolean isMine;
  boolean isRevealed;
  boolean isFlagged;
  ArrayList<Cell> neighbors;

  // Empty Constructor
  Cell() {
    this.isMine = false;
    this.isRevealed = false;
    this.isFlagged = false;
    this.neighbors = new ArrayList<Cell>();
  }

  // Determine if it is a bomb
  Cell(boolean isMine) {
    this.isMine = isMine;
    this.isRevealed = false;
    this.isFlagged = false;
    this.neighbors = new ArrayList<Cell>();
  }

  // Constructor for testing
  Cell(boolean isMine, boolean isRevealed, boolean isFlagged) {
    this.isMine = isMine;
    this.isRevealed = isRevealed;
    this.isFlagged = isFlagged;
    this.neighbors = new ArrayList<Cell>();
  }

  // All Constructor for testing
  Cell(boolean isMine, boolean isRevealed, boolean isFlagged, ArrayList<Cell> neighbors) {
    this.isMine = isMine;
    this.isRevealed = isRevealed;
    this.isFlagged = isFlagged;
    this.neighbors = neighbors;
  }

  // Add the cell to its neighbor, recognizing it as a neighbor
  void addNeighbor(Cell neighbor) {
    if (!(neighbors.contains(neighbor))) {
      neighbors.add(neighbor);
    }
  }

  // Count the number of neighboring cells
  int countNeighborMines() {
    int count = 0;
    for (Cell neighbors : this.neighbors) {
      if (neighbors.isMine) {
        count += 1;
      }
    }
    return count;
  }

  // Convert the number given after counting the neighbors into a text image box
  TextImage colorNeighbors() {
    if (countNeighborMines() == 1) {
      return new TextImage(Integer.toString(1), Constants.FONT, FontStyle.BOLD, Color.BLUE);
    }
    else if (countNeighborMines() == 2) {
      return new TextImage(Integer.toString(2), Constants.FONT, FontStyle.BOLD, Color.GREEN);
    }
    else if (countNeighborMines() == 3) {
      return new TextImage(Integer.toString(3), Constants.FONT, FontStyle.BOLD, Color.RED);
    }
    else if (countNeighborMines() == 4) {
      return new TextImage(Integer.toString(4), Constants.FONT, FontStyle.BOLD, Color.PINK);
    }
    else if (countNeighborMines() == 5) {
      return new TextImage(Integer.toString(5), Constants.FONT, FontStyle.BOLD, Color.YELLOW);
    }
    else if (countNeighborMines() == 6) {
      return new TextImage(Integer.toString(6), Constants.FONT, FontStyle.BOLD, Color.CYAN);
    }
    else if (countNeighborMines() == 7) {
      return new TextImage(Integer.toString(7), Constants.FONT, FontStyle.BOLD, Color.BLACK);
    }
    else if (countNeighborMines() == 8) {
      return new TextImage(Integer.toString(8), Constants.FONT, FontStyle.BOLD, Color.GREEN);
    }
    else {
      return new TextImage(Integer.toString(0), Constants.FONT, FontStyle.BOLD, Color.GRAY);
    }
  }

  // Rendering each cell with each figures
  public WorldImage renderCell() {
    if (!isMine && isRevealed) {
      return new OverlayImage(Constants.OUTLINE,
          new OverlayImage(colorNeighbors(), Constants.REVEALED));

    }
    else if (isMine && isRevealed) {
      return new OverlayImage(Constants.OUTLINE,
          new OverlayImage(Constants.BOMB, Constants.REVEALED));

    }
    else if (isFlagged) {
      return new OverlayImage(Constants.FLAGSTAR, Constants.UNREVEALED);

    }
    else {
      return Constants.UNREVEALED;
    }
  }

}

// Utility class
class Utils {

  // check if the num1 value follows the game restriction for Constructors
  public int check(int num1, int num2, String message) {
    if ((num1 < 0) || (num1 > num2)) {
      throw new IllegalArgumentException(message);
    }
    else {
      return num1;
    }
  }
}

// Examples and test for the game
class ExamplesMinesweeper {

  Cell c1;
  Cell c2;
  Cell c3;
  Cell c4;
  Cell c5;
  Cell c6;
  Cell c7;
  Cell c8;
  Cell c9;

  ArrayList<ArrayList<Cell>> grid1;

  MinesweeperWorld ms1;

  void reset() {
    // examples of a 3x3 grid that has 9 cells
    c1 = new Cell(true, false, false); // mine
    c2 = new Cell(false, false, false); // nothing
    c3 = new Cell(false, true, false); // revealed, no number appear
    c4 = new Cell(true, false, false); // mine
    c5 = new Cell(false, true, false); // revealed, number 3
    c6 = new Cell(false, false, false); // unrevealed
    c7 = new Cell(false, true, false); // revealed, number 2
    c8 = new Cell(false, false, false); // unrevealed
    c9 = new Cell(true, false, false); // mine

    // making the grid
    ArrayList<ArrayList<Cell>> grid1 = new ArrayList<>(Arrays.asList(
        new ArrayList<>(Arrays.asList(c1, c2, c3)), new ArrayList<>(Arrays.asList(c4, c5, c6)),
        new ArrayList<>(Arrays.asList(c7, c8, c9))));

    // Create a MinesweeperWorld object with the grid
    this.ms1 = new MinesweeperWorld(3, 3, 0, grid1);

    // Call the linkNeighbors method to establish neighboring relationships
    this.ms1.linkNeighbors();
  }

  // Test method for the linkNeighbors method
  void testLinkNeighbors(Tester t) {
    reset();
    // top left
    t.checkExpect(c1.neighbors.size(), 3);
    // middle cell
    t.checkExpect(c5.neighbors.size(), 8);
    // bottom right
    t.checkExpect(c9.neighbors.size(), 3);

    // checking the cell's neighbors
    t.checkExpect(c1.neighbors, new ArrayList<Cell>(Arrays.asList(c2, c4, c5)));
    t.checkExpect(c5.neighbors, new ArrayList<Cell>(Arrays.asList(c1, c2, c3, c4, c6, c7, c8, c9)));

    // Cell can not be its own neighbor
    t.checkExpect(c1.neighbors.contains(c1), false);

    // Neighbors
    t.checkExpect(c1.neighbors.contains(c2), true);
    t.checkExpect(c1.neighbors.contains(c4), true);
    t.checkExpect(c1.neighbors.contains(c5), true);

    // Non-neighbors
    t.checkExpect(c1.neighbors.contains(c3), false);
    t.checkExpect(c1.neighbors.contains(c6), false);
    t.checkExpect(c1.neighbors.contains(c7), false);
    t.checkExpect(c1.neighbors.contains(c8), false);
    t.checkExpect(c1.neighbors.contains(c9), false);
  }

  // tests initializeBoard method
  void testInitializeBoard(Tester t) {
    reset();
    MinesweeperWorld ms = new MinesweeperWorld(2, 2, 0);
    for (ArrayList<Cell> row : ms.board) {
      for (Cell cell : row) {
        t.checkExpect(cell.isMine, false);
        t.checkExpect(cell.isRevealed, false);
        t.checkExpect(cell.isFlagged, false);
      }
    }

    // check if the board is initialized
    t.checkExpect(ms1.board.size(), 6);
    t.checkExpect(ms1.board.get(0).size(), 3);
    t.checkExpect(ms1.board.get(1).size(), 3);
    t.checkExpect(ms1.board.get(5).size(), 3);
  }

  // tests isWithinGrid method
  void testIsWithinGrid(Tester t) {
    reset();
    // tests within grid
    t.checkExpect(ms1.isWithinGrid(0, 0), true);
    t.checkExpect(ms1.isWithinGrid(1, 1), true);
    t.checkExpect(ms1.isWithinGrid(2, 2), true);
    // tests outside of grid
    t.checkExpect(ms1.isWithinGrid(-1, 0), false);
    t.checkExpect(ms1.isWithinGrid(0, -1), false);
    t.checkExpect(ms1.isWithinGrid(3, 0), false);
    t.checkExpect(ms1.isWithinGrid(0, 3), false);
  }

  // tests makeScene method
  void testMakeScene(Tester t) {
    reset();

    WorldScene scene = ms1.makeScene();
    String renderedScene = scene.toString();
    // tests rendered scene does not contain any specific cell
    t.checkExpect(renderedScene.contains("rect 15.0 15.0 30.0 30.0"), false);
    t.checkExpect(renderedScene.contains("rect 45.0 15.0 60.0 30.0"), false);
    t.checkExpect(renderedScene.contains("rect 75.0 15.0 90.0 30.0"), false);
    t.checkExpect(renderedScene.contains("rect 15.0 45.0 30.0 60.0"), false);
    t.checkExpect(renderedScene.contains("rect 45.0 45.0 60.0 60.0"), false);
    t.checkExpect(renderedScene.contains("rect 75.0 45.0 90.0 60.0"), false);
    t.checkExpect(renderedScene.contains("rect 15.0 75.0 30.0 90.0"), false);
    t.checkExpect(renderedScene.contains("rect 45.0 75.0 60.0 90.0"), false);
    t.checkExpect(renderedScene.contains("rect 75.0 75.0 90.0 90.0"), false);

    // tests the scene generated matches the expected scene
    WorldScene expect = ms1.getEmptyScene();
    expect.placeImageXY(c1.renderCell(), Constants.CELL_SIZE * 0, Constants.CELL_SIZE * 0);
    expect.placeImageXY(c2.renderCell(), Constants.CELL_SIZE * 0, Constants.CELL_SIZE * 1);
    expect.placeImageXY(c3.renderCell(), Constants.CELL_SIZE * 0, Constants.CELL_SIZE * 2);
    expect.placeImageXY(c4.renderCell(), Constants.CELL_SIZE * 1, Constants.CELL_SIZE * 0);
    expect.placeImageXY(c5.renderCell(), Constants.CELL_SIZE * 1, Constants.CELL_SIZE * 1);
    expect.placeImageXY(c6.renderCell(), Constants.CELL_SIZE * 1, Constants.CELL_SIZE * 2);
    expect.placeImageXY(c7.renderCell(), Constants.CELL_SIZE * 2, Constants.CELL_SIZE * 0);
    expect.placeImageXY(c8.renderCell(), Constants.CELL_SIZE * 2, Constants.CELL_SIZE * 1);
    expect.placeImageXY(c9.renderCell(), Constants.CELL_SIZE * 2, Constants.CELL_SIZE * 2);
    t.checkExpect(this.ms1.makeScene(), expect);

    // tests game over scene when the game is not over and no cell is revealed
    ms1.gameOver = true;
    WorldScene gameOverScene = ms1.makeScene();
    t.checkExpect(gameOverScene.toString().contains("YOU WON"), false);
    t.checkExpect(gameOverScene.toString().contains("LOSER"), false);
    ms1.gameOver = false;
    ms1.gameOver = true;
    ms1.restartGame = true;
    WorldScene restartButtonScene = ms1.makeScene();
    t.checkExpect(restartButtonScene.toString().contains("RESTART"), false);

    // tests game over scene when the game is not over but has cells are revealed
    ms1.gameOver = false;
    ms1.restartGame = false;
    WorldScene scene1 = ms1.makeScene();
    t.checkExpect(scene1.toString().contains("YOU WON"), false);
    t.checkExpect(scene1.toString().contains("LOSER"), false);
    t.checkExpect(scene1.toString().contains("RESTART"), false);

    // tests game over scene when the player wins
    ms1.board.get(0).get(0).isRevealed = true;
    ms1.board.get(0).get(1).isRevealed = true;
    ms1.board.get(1).get(0).isRevealed = true;
    ms1.board.get(1).get(1).isRevealed = true;
    WorldScene scene2 = ms1.makeScene();
    t.checkExpect(scene2.toString().contains("YOU WON"), false);
    t.checkExpect(scene2.toString().contains("LOSER"), false);
    t.checkExpect(scene2.toString().contains("RESTART"), false);

    // test the game over scene when the player loses
    ms1.gameOver = true;
    ms1.restartGame = false;
    WorldScene scene3 = ms1.makeScene();
    t.checkExpect(scene3.toString().contains("YOU WON"), false);
    t.checkExpect(scene3.toString().contains("LOSER"), false);
    t.checkExpect(scene3.toString().contains("RESTART"), false);

    // tests restart button scene when the game is over and the restart flag
    // is set
    ms1.gameOver = true;
    ms1.restartGame = false;
    ms1.board.get(0).get(0).isRevealed = true;
    WorldScene scene4 = ms1.makeScene();
    t.checkExpect(scene4.toString().contains("YOU WON"), false);
    t.checkExpect(scene4.toString().contains("LOSER"), false);
    t.checkExpect(scene4.toString().contains("RESTART"), false);

  }

  // tests addNeighbor method
  void testAddNeighbor(Tester t) {
    reset();
    t.checkExpect(c1.neighbors.size(), 3);
    t.checkExpect(c5.neighbors.size(), 8);
    t.checkExpect(c9.neighbors.size(), 3);
    t.checkExpect(c1.neighbors, new ArrayList<Cell>(Arrays.asList(c2, c4, c5)));
    t.checkExpect(c1.neighbors.contains(c1), false);
    t.checkExpect(c1.neighbors.contains(c2), true);
    t.checkExpect(c1.neighbors.contains(c3), false);
    t.checkExpect(c1.neighbors.contains(c4), true);
    t.checkExpect(c1.neighbors.contains(c5), true);
    t.checkExpect(c1.neighbors.contains(c6), false);
    t.checkExpect(c1.neighbors.contains(c7), false);
    t.checkExpect(c1.neighbors.contains(c8), false);
    t.checkExpect(c1.neighbors.contains(c9), false);
    t.checkExpect(c3.countNeighborMines(), 0);

    Cell c = new Cell(false);
    Cell c0 = new Cell(false);
    Cell c01 = new Cell(false);
    Cell c02 = new Cell(false);
    Cell c03 = new Cell(false);
    c.addNeighbor(c0);
    t.checkExpect(c.neighbors.contains(c0), true);
    t.checkExpect(c.neighbors.contains(c1), false);
    t.checkExpect(c0.countNeighborMines(), 0);
    c.addNeighbor(c01);
    t.checkExpect(c.neighbors.contains(c01), true);
    c.addNeighbor(c02);
    t.checkExpect(c.neighbors.contains(c02), true);
    c.addNeighbor(c03);
    t.checkExpect(c.neighbors.contains(c03), true);
    t.checkExpect(c.neighbors.contains(c9), false);

  }

  // tests countNeighborMines method
  void testCountNeighborMines(Tester t) {
    reset();
    t.checkExpect(c5.countNeighborMines(), 3);
    t.checkExpect(c2.countNeighborMines(), 2);
    t.checkExpect(c6.countNeighborMines(), 1);
    t.checkExpect(c8.countNeighborMines(), 2);
    t.checkExpect(c3.countNeighborMines(), 0);
  }

  // tests colorNeighbors method
  void testColorNeighbors(Tester t) {
    reset();
    Cell mineNeighbor1 = new Cell(true);
    Cell mineNeighbor2 = new Cell(true);
    ArrayList<Cell> neighbors = new ArrayList<>();
    neighbors.add(mineNeighbor1);
    neighbors.add(mineNeighbor2);
    c5.neighbors = neighbors;

    t.checkExpect(c1.colorNeighbors(),
        new TextImage(Integer.toString(1), Constants.FONT, FontStyle.BOLD, Color.BLUE));
    t.checkExpect(c2.colorNeighbors(),
        new TextImage(Integer.toString(2), Constants.FONT, FontStyle.BOLD, Color.GREEN));
    t.checkExpect(c4.colorNeighbors(),
        new TextImage(Integer.toString(1), Constants.FONT, FontStyle.BOLD, Color.BLUE));
    t.checkExpect(c5.colorNeighbors(),
        new TextImage(Integer.toString(2), Constants.FONT, FontStyle.BOLD, Color.GREEN));
    t.checkExpect(c6.colorNeighbors(),
        new TextImage(Integer.toString(1), Constants.FONT, FontStyle.BOLD, Color.BLUE));
  }

  // tests renderCell method
  void testRenderCell(Tester t) {
    reset();
    t.checkExpect(c2.renderCell(), Constants.UNREVEALED);
    t.checkExpect(c5.renderCell(), new OverlayImage(Constants.OUTLINE, new OverlayImage(
        new TextImage("3", Constants.FONT, FontStyle.BOLD, Color.RED), Constants.REVEALED)));
    t.checkExpect(c6.renderCell(), Constants.UNREVEALED);
    t.checkExpect(c8.renderCell(), Constants.UNREVEALED);
  }

  // tests placeMine method
  void testPlaceMines(Tester t) {
    reset();

    // test on a small grid
    MinesweeperWorld smallGrid = new MinesweeperWorld(3, 3, 2);
    smallGrid.placeMines(0, 0);
    int mineCount = 0;
    for (ArrayList<Cell> row : smallGrid.board) {
      for (Cell cell : row) {
        if (cell.isMine) {
          mineCount++;
        }
      }
    }
    t.checkExpect(mineCount, 2);

    // tests on a large grid
    MinesweeperWorld largeGrid = new MinesweeperWorld(10, 10, 20);
    largeGrid.placeMines(mineCount, mineCount);
    mineCount = 0;
    for (ArrayList<Cell> row : largeGrid.board) {
      for (Cell cell : row) {
        if (cell.isMine) {
          mineCount++;
        }
      }
    }
    t.checkExpect(mineCount, 20);

    // tests with out-of-bounds start coordinates
    MinesweeperWorld outOfBoundsStartGrid = new MinesweeperWorld(4, 4, 6);
    outOfBoundsStartGrid.placeMines(10, 10);
    mineCount = 0;
    for (ArrayList<Cell> row : outOfBoundsStartGrid.board) {
      for (Cell cell : row) {
        if (cell.isMine) {
          mineCount++;
        }
      }
    }
    t.checkExpect(mineCount, 6);

    // tests with negative start coordinates
    MinesweeperWorld negativeStartGrid = new MinesweeperWorld(6, 6, 10);
    negativeStartGrid.placeMines(-1, -1);
    mineCount = 0;
    for (ArrayList<Cell> row : negativeStartGrid.board) {
      for (Cell cell : row) {
        if (cell.isMine) {
          mineCount++;
        }
      }
    }
    t.checkExpect(mineCount, 10);
  }

  // test for Constructor Exception
  void testConstructor(Tester t) {
    Utils u = new Utils();
    t.checkExpect(u.check(1, 2, "hello"), 1);
    t.checkException(new IllegalArgumentException("hello"), u, "check", 3, 2, "hello");

    t.checkConstructorException(new IllegalArgumentException("A Grid Can Not Be Formed"),
        "MinesweeperWorld", 1080, 1, 99);
    t.checkConstructorException(new IllegalArgumentException("A Grid Can Not Be Formed"),
        "MinesweeperWorld", 1, 10000, 99);
    t.checkConstructorException(new IllegalArgumentException("Bombs OverFlow"), "MinesweeperWorld",
        10, 10, 101);
  }

  // tests winOrLose method
  void testWinOrLose(Tester t) {
    reset();
    MinesweeperWorld world1 = new MinesweeperWorld(3, 3, 0);
    t.checkExpect(world1.winOrLose(),
        new TextImage("YOU WON", Constants.CELL_SIZE, FontStyle.BOLD, Color.GREEN));

    ms1.gameOver = true;
    // check when lost
    t.checkExpect(ms1.winOrLose(),
        new TextImage("LOSER", Constants.CELL_SIZE, FontStyle.BOLD, Color.RED));

    // test when all cells are flagged
    MinesweeperWorld world3 = new MinesweeperWorld(3, 3, 9);
    for (ArrayList<Cell> row : world3.board) {
      for (Cell cell : row) {
        cell.isFlagged = true;
      }
    }
    t.checkExpect(world3.winOrLose(),
        new TextImage("YOU WON", Constants.CELL_SIZE, FontStyle.BOLD, Color.GREEN));

    // test when all cells are revealed except mines
    MinesweeperWorld world4 = new MinesweeperWorld(3, 3, 1);
    for (ArrayList<Cell> row : world4.board) {
      for (Cell cell : row) {
        if (!cell.isMine) {
          cell.isRevealed = true;
        }
      }
    }
    t.checkExpect(world4.winOrLose(),
        new TextImage("YOU WON", Constants.CELL_SIZE, FontStyle.BOLD, Color.GREEN));
  }

  // tests win method inside of MineSweeper class
  void testWin(Tester t) {
    reset();

    // game where all non-mine cells are flagged
    for (ArrayList<Cell> row : ms1.board) {
      for (Cell cell : row) {
        if (!cell.isMine) {
          cell.isFlagged = true;
        }
      }
    }
    t.checkExpect(ms1.win(), false);

    reset();

    // game where all mine cells are flagged
    for (ArrayList<Cell> row : ms1.board) {
      for (Cell cell : row) {
        if (cell.isMine) {
          cell.isFlagged = true;
        }
      }
    }
    t.checkExpect(ms1.win(), true);

    reset();

    // game where all non-mine cells are revealed
    for (ArrayList<Cell> row : ms1.board) {
      for (Cell cell : row) {
        if (!cell.isMine) {
          cell.isRevealed = true;
        }
      }
    }
    t.checkExpect(ms1.win(), false);

    reset();

    // game where all cells are revealed and flagged
    for (ArrayList<Cell> row : ms1.board) {
      for (Cell cell : row) {
        cell.isRevealed = true;
        cell.isFlagged = true;
      }
    }
    t.checkExpect(ms1.win(), true);
  }

  // tests onMouseClicked method
  void testOnMouseClicked(Tester t) {
    reset();

    // tests left click cell with no adjacent mines
    //before the click condition
    t.checkExpect(ms1.board.get(0).get(0).isRevealed, false);
    t.checkExpect(ms1.board.get(0).get(0).isMine, true);
    t.checkExpect(ms1.board.get(0).get(0).isFlagged, false);
    t.checkExpect(ms1.gameOver, false);
    t.checkExpect(ms1.restartGame, false);
    t.checkExpect(ms1.firstClick, true);

    //the click happen (RIGHT)
    ms1.onMouseClicked(new Posn(0, 0), "RightButton");
    t.checkExpect(ms1.board.get(0).get(0).isFlagged, true);

    //right click again to unflag
    ms1.onMouseClicked(new Posn(0, 0), "RightButton");
    t.checkExpect(ms1.board.get(0).get(0).isFlagged, false);

    //the click happen (LEFT)
    ms1.onMouseClicked(new Posn(0, 0), "LeftButton");
    t.checkExpect(ms1.board.get(0).get(0).isRevealed, true);
    t.checkExpect(ms1.board.get(0).get(0).isMine, true);
    t.checkExpect(ms1.board.get(0).get(0).isFlagged, false);
    t.checkExpect(ms1.gameOver, true);
    t.checkExpect(ms1.restartGame, false);
    t.checkExpect(ms1.firstClick, false);


    // tests right click on a revealed cell, nothing happens
    ms1.onMouseClicked(new Posn(2, 0), "LeftButton");
    t.checkExpect(ms1.board.get(2).get(0).isRevealed, true);
    t.checkExpect(ms1.board.get(2).get(0).isFlagged, false);
    ms1.onMouseClicked(new Posn(2, 0), "RightButton");
    t.checkExpect(ms1.board.get(2).get(0).isFlagged, false);

    // tests left click on a flagged cell
    ms1.board.get(1).get(1).isFlagged = true;
    ms1.onMouseClicked(new Posn(1, 1), "RightButton");
    t.checkExpect(ms1.board.get(1).get(1).isFlagged, true);
    ms1.onMouseClicked(new Posn(1, 1), "LeftButton");
    t.checkExpect(ms1.board.get(1).get(1).isRevealed, true);
  }

  // tests gameEnd method
  void testGameEnd(Tester t) {
    reset();
    t.checkExpect(ms1.gameOver, false);
    ms1.gameEnd();
    // no mines revealed revealed
    for (ArrayList<Cell> arrCell : ms1.board) {
      for (Cell cell : arrCell) {
        if (cell.isMine) {
          t.checkExpect(cell.isRevealed, false);
        }
      }
    }

    reset();
    ms1.gameOver = true;
    ms1.gameEnd();
    // all mines revealed
    for (ArrayList<Cell> arrCell : ms1.board) {
      for (Cell cell : arrCell) {
        if (cell.isMine) {
          t.checkExpect(cell.isRevealed, true);
        }
      }
    }

    // tests game over and contain revealed and unrevealed mines
    reset();
    ms1.gameOver = true;
    // set all mines revealed
    for (int i = 0; i < ms1.rows; i++) {
      for (int j = 0; j < ms1.columns; j++) {
        if (ms1.board.get(i).get(j).isMine && (i + j) % 2 == 0) {
          ms1.board.get(i).get(j).isRevealed = true;
        }
      }
    }

    ms1.gameEnd();
    // all mines revealed
    for (ArrayList<Cell> arrCell : ms1.board) {
      for (Cell cell : arrCell) {
        if (cell.isMine) {
          t.checkExpect(cell.isRevealed, true);
        }
      }
    }

    // tests game over and no unrevealed mines
    reset();
    ms1.gameOver = true;
    // set all mine cells revealed
    for (ArrayList<Cell> arrCell : ms1.board) {
      for (Cell cell : arrCell) {
        if (cell.isMine) {
          cell.isRevealed = true;
        }
      }
    }

    ms1.gameEnd();

    // all mines revealed revealed
    for (ArrayList<Cell> arrCell : ms1.board) {
      for (Cell cell : arrCell) {
        if (cell.isMine) {
          t.checkExpect(cell.isRevealed, true);
        }
      }
    }
  }

  // tests the restartGame method
  void testRestartGame(Tester t) {
    reset();

    // tests the restartGame method when the game is over
    ms1.gameOver = true;
    ms1.restartGame();
    // check if board is cleared
    t.checkExpect(ms1.board.isEmpty(), false);
    // check if game over is reset
    t.checkExpect(ms1.gameOver, false);
    // check if first click is reset
    t.checkExpect(ms1.firstClick, true);
    // check if restart button is hidden
    t.checkExpect(ms1.restartGame, false);

    // tests the restartGame method when the game is in progress
    reset();
    ms1.firstClick = false;
    ms1.placeMines(0, 0);
    ms1.restartGame();
    // check if board is cleared
    t.checkExpect(ms1.board.isEmpty(), false);
    // check if game over is reset
    t.checkExpect(ms1.gameOver, false);
    // check if first click is reset
    t.checkExpect(ms1.firstClick, true);
    // check if restart button is hidden
    t.checkExpect(ms1.restartGame, false);

    // tests the restartGame method after winning the game
    reset();
    // win game
    for (ArrayList<Cell> row : ms1.board) {
      for (Cell cell : row) {
        if (cell.isMine) {
          cell.isFlagged = true;
        }
      }
    }

    ms1.win();
    ms1.restartGame();

    // check if board is cleared
    t.checkExpect(ms1.board.isEmpty(), false);
    // check if game over is reset
    t.checkExpect(ms1.gameOver, false);
    // check if first click is reset
    t.checkExpect(ms1.firstClick, true);
    // check if restart button is hidden
    t.checkExpect(ms1.restartGame, false);
  }

  // tests floodFill method
  void testFloodFill(Tester t) {
    reset();

    Cell clickedCell = ms1.board.get(1).get(1);
    clickedCell.isRevealed = true;
    ms1.floodFill(clickedCell);

    // check that all adjacent cells with no neighboring mines are revealed
    t.checkExpect(ms1.board.get(0).get(0).isRevealed, false);
    t.checkExpect(ms1.board.get(0).get(1).isRevealed, false);
    t.checkExpect(ms1.board.get(0).get(2).isRevealed, true);
    t.checkExpect(ms1.board.get(1).get(0).isRevealed, false);
    t.checkExpect(ms1.board.get(1).get(1).isRevealed, true);
    t.checkExpect(ms1.board.get(1).get(2).isRevealed, false);
    t.checkExpect(ms1.board.get(2).get(0).isRevealed, true);
    t.checkExpect(ms1.board.get(2).get(1).isRevealed, false);
    t.checkExpect(ms1.board.get(2).get(2).isRevealed, false);
  }

  // test bigBang
  void testBigBang(Tester t) {
    reset();
    int widthTest = 3 * Constants.CELL_SIZE;
    MinesweeperWorld world = new MinesweeperWorld(Constants.ROWS, Constants.COLUMNS,
        Constants.BOMBS);

    // RUN THIS FOR ADVANCED GAME PLAY 
    ms1.bigBang(widthTest, widthTest, Constants.TICKRATE);

    // RUN THIS FOR A NORMAL GAME PLAY
    //world.bigBang(Constants.WORLDWIDTH, Constants.WORLDHEIGHT, Constants.TICKRATE);
  }

}