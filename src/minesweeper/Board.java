/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * Represents a thread-safe Minesweeper game board. This Minesweeper game board can be accessed by multiple threads,
 * to allow multiplayer use of the board. The Board data type is thread-safe by making its actions atomic. Any method
 * calls to a Board object occur atomically in the face of multithreaded execution, so multiple threads cannot simultaneously
 * be executing method calls on a Board object - one runs atomically before the other (before-after atomicity).
 */
public class Board {
    
    private static enum CellState {
        UNTOUCHED, FLAGGED, DUG
    }
    
    private final int width, height;
    
    private final CellState[][] cellStates;
    private final boolean[][] mines;
    private final int[][] mineCounts;
    
    /*
     *  Abstraction Function
     *  ====================
     *  Represents a rectangular Minesweeper board with dimensions width by height, where:
     *      - The state of each cell on the board is represented by the array cellStates, where a cell (x,y) is
     *        either "untouched", "flagged", or "dug", as represented by the CellState enum at cellStates[x][y]
     *        
     *      - The location of mines on the board is represented by the array mines, where a cell (x,y) has a
     *        mine if and only if mines[x][y]
     *        
     *      - The number of mines surrounding a cell on the board is represented by the array mineCounts, where
     *        a cell (x,y) has mineCounts[x][y] mines in its surrounding 8 cells.
     *      
     *  (NOTE: (0,0) is top-left, and (x,y) cells have increasing x moving right on the abstract board and
     *         have increasing y moving downward on the abstract board.
     */
    
    /*
     *  Rep Invariant
     *  =============
     *  cellStates is a two-dimensional array of dimensions width x length
     *  mines is a two-dimensional array of dimensions width x length
     *  mineCounts is a two-dimensional array of dimensions width x length
     *  
     *  For all 0 <= x < width, 0 <= y < height:
     *      - mineCounts[x][y] is equal to the number of the following elements that are true:
     *                                          1) mines[x+1][y]
     *                                          2) mines[x+1][y-1]
     *                                          3) mines[x][y-1]
     *                                          4) mines[x-1][y-1]
     *                                          5) mines[x-1][y]
     *                                          6) mines[x-1][y+1]
     *                                          7) mines[x][y+1]
     *                                          8) mines[x+1][y+1]
     *      
     *      - If cellStates[x][y] = DUG, mines[x][y] = false
     */
    
    /*
     *  Safety from Rep Exposure
     *  ========================
     *  All fields are private and have immutable references
     *  The fields width and height are immutable
     *  No references to internal field objects are leaked, all return types are primitive, and so immutable.
     */
    
    /*
     *  System Thread Safety Argument
     *  =============================
     *  Threads are allowed to concurrently access this object, preserve the rep invariant, and
     *  maintain the validity of the specs because of the following thread-safety implementations:
     *      - Immutability      getWidth(), getHeight(), and isValidBoardCoordinate() are non-mutator methods 
     *                          that only read immutable fields of this object
     *      
     *      - Synchronization   the other public methods are all synchronized methods, meaning multithreaded access
     *                          to this object is done atomically by threads (no simultaneous access to the object)
     *                          and so, these methods run atomically, to completion, before another thread can access
     *                          this object
     *                          
     *  Private methods are not explicitly available to external threads (Note: There are no internal threads as this object
     *  does not create new threads). However, these private methods are called from within public methods, which can be called
     *  concurrently. Since these methods are called from within synchronized methods only, synchronization still provides
     *  thread-safety, because the method call still has the lock to this object, while within the private method (furthermore, we
     *  cannot have helper methods be synchronized if we want public synchronized methods to call to them during execution).
     */
    
    // ----------------------------------------------- Constructors -------------------------------------------------
    
    /**
     * Creates a Minesweeper board of dimensions width by height, with a third of
     * its cells (to the nearest whole number), chosen at random, have mines.
     * @param width a positive integer denoting the width of the Minesweeper board
     * @param height a positive integer denoting the height of the Minesweeper board
     */
    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        
        cellStates = new CellState[width][height];
        mines = new boolean[width][height];
        mineCounts = new int[width][height];
        
        for (CellState[] row : cellStates)
            Arrays.fill(row, CellState.UNTOUCHED);
        
        int[] mineLocations = randomIntegers(0, width*height, width*height/3);
        for (int n : mineLocations) {
            int x = n % width, y = n / width;
            placeMine(x,y);
        }
        
        checkRep();
    }
       
    /**
     * Creates a Minesweeper board where the distribution and size of mines is taken from a file
     * following the Minesweeper Server File Format (see Specification & Protocol web page)
     * 
     * @param file A text file representing the initial distribution of mines on a Minesweeper,
     * satisfying the Minesweeper Server File Format. 
     */
    public Board(File file) {
        
        try (
            Scanner in = new Scanner(new FileReader(file));
        ){  
            width = in.nextInt();
            height = in.nextInt();
            
            cellStates = new CellState[width][height];
            mines = new boolean[width][height];
            mineCounts = new int[width][height];
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    cellStates[x][y] = CellState.UNTOUCHED;
                    if (in.nextInt() == 1) placeMine(x,y);
                }
            }
            
            checkRep();
            
        } catch (IOException e) {
            throw new RuntimeException("File illegal format.");
        }
    }
    
    // ------------------------------------------------ Check Rep ---------------------------------------------------
    
    /*
     *  Asserts the rep invariant.
     */
    private void checkRep() {
        assert cellStates.length == width;
        assert mines.length == width;
        assert mineCounts.length == width;
        
        for (int x = 0; x < width; x++) {
            assert cellStates[x].length == height;
            assert mines[x].length == height;
            assert mineCounts[x].length == height;
            
            for (int y = 0; y < height; y++) {
                assert mineCounts[x][y] == countSurroundingMines(x,y);
                if (cellStates[x][y].equals(CellState.DUG)) assert !mines[x][y];
            }
        }
            
    }
    
    // ------------------------------------------------ Observers ---------------------------------------------------
    
    /**
     * Returns the width of this Minesweeper board.
     * @return the width of this Minesweeper board
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Returns the height of this Minesweeper board.
     * @return the height of this Minesweeper board.
     */
    public int getHeight() {
        return height;
    }
    
    /**
     *  Returns true is cell (x,y) is a cell on this Minesweeper board, that is
     *  if (x,y) are valid coordinates on this Minesweeper board.
     *  @param x an integer representing x-coordinate of cell to be checked.
     *  @param y an integer representing y-coordinate of cell to be checked.
     *  @return true if x is in the range [0, width) and y is in the range [0, height)
     */
    public boolean isValidBoardCoordinate(int x, int y) {
        return 0 <= x && x < width &&
               0 <= y && y < height;
    }
    
    /**
     * Returns true if the cell (x,y) of this Minesweeper board is in the untouched state.
     * Requires that x,y be integers such that isValidBoardCoordinate(x,y) returns true.
     * 
     * @param x the x-coordinate of cell being checked
     * @param y the y-coordinate of cell being checked
     * @return true if the cell (x,y) of this Minesweeper board is in the untouched state.
     */
    public synchronized boolean isUntouched(int x, int y) {
        return cellStates[x][y].equals(CellState.UNTOUCHED);
    }
    
    /**
     * Returns true if the cell (x,y) of this Minesweeper board is in the flagged state.
     * Requires that x,y be integers such that isValidBoardCoordinate(x,y) returns true.
     * 
     * @param x the x-coordinate of cell being checked
     * @param y the y-coordinate of cell being checked
     * @return true if the cell (x,y) of this Minesweeper board is in the flagged state.
     */
    public synchronized boolean isFlagged(int x, int y) {
        return cellStates[x][y].equals(CellState.FLAGGED);
    }
    
    /**
     * Returns true if the cell (x,y) of this Minesweeper board is in the dug (opened) state.
     * Requires that x,y be integers such that isValidBoardCoordinate(x,y) return true.
     * 
     * @param x the x-coordinate of cell being checked
     * @param y the y-coordinate of cell being checked
     * @return true if the cell (x,y) of this Minesweeper board is in the dug (opened) state.
     */
    public synchronized boolean hasBeenDug(int x, int y) {
        return cellStates[x][y].equals(CellState.DUG);
    }
    
    /**
     * Returns true if there is a mine in cell (x,y) of this Minesweeper board.
     * Requires that x,y be integers such that isValidBoardCoordinate(x,y) returns true.
     * 
     * @param x the x-coordinate of cell being checked
     * @param y the y-coordinate of cell being checked
     * @return true if there is a mine in cell (x,y) of this Minesweeper board. Returns false, otherwise.
     */
    public synchronized boolean containsMine(int x, int y) {
        return mines[x][y];
    }
    
    /**
     * Returns the number of mines in the 8 cells surrounding the cell (x,y) on this Minesweeper board.
     * Requires that x,y be integers such that isValidBoardCoordinate(x,y) return true.
     * 
     * @param x the x-coordinate of cell being checked
     * @param y the y-coordinate of cell being checked
     * @return true the number of mines surrounding the cell (x,y) on this Minesweeper board.
     */
    public synchronized int getMineCount(int x, int y) {
        return mineCounts[x][y];
    }

    // ------------------------------------------------ Mutators ---------------------------------------------------

    /**
     * Performs a dig on cell (x,y) of this Minesweeper board, that is, the cell (x,y) is opened, and
     * returns true if the cell was untouched before and there was a mine at this cell. A dig on an untouched
     * cell changes it to the dug state. A dig on a flagged cell or dug cell does nothing. A dig on an untouched
     * cell removes the mine after execution of this method, to allow the game to proceed. This all happens in the
     * case where x,y are integers such that isValidBoardCoordinate(x,y) returns true.
     * 
     * In the case where isValidBoardCoordinate(x,y) returns false, the board remains unchanged and this method
     * returns false.
     * 
     * @param x the x-coordinate of cell to be opened/dug
     * @param y the y-coordinate of cell to be opened/dug
     * @return true if this cell was untouched before and had a mine at this location. Otherwise, return false.
     */
    public synchronized boolean dig(int x, int y) {
        if (isValidBoardCoordinate(x,y) && isUntouched(x,y)) {
            boolean containedMine = containsMine(x,y);
            
            recursiveDig(x,y);
            
            if (containedMine) {
                removeMine(x,y);
                checkRep();
                return true;
            }
        }
        
        checkRep();
        return false;
    }
    
    /**
     * Flags the cell (x,y) on this Minesweeper board, if the cell has not been dug or is not already flagged.
     * If the cell has been dug or is already flagged, the method does nothing.
     * All of this happens in the case where x,y are integers such that isValidBoardCoordinate(x,y) returns true
     * 
     * In the case where isValidBoardCoordinate(x,y) returns false, the board remains unchanged.
     * 
     * @param x the x-coordinate of the cell to be flagged
     * @param y the y-coordinate of the cell to be flagged
     */
    public synchronized void flag(int x, int y) {
        if (isValidBoardCoordinate(x,y) && isUntouched(x,y))
            cellStates[x][y] = CellState.FLAGGED;
        checkRep();
    }
    
    /**
     * Deflags the cell (x,y) on this Minesweeper board, if the cell is already flagged.
     * If the cell has been dug or has not been flagged, the method does nothing.
     * All of this happens in the case where x,y are integers such that isValidBoardCoordinate(x,y) returns true
     * 
     * In the case where isValidBoardCoordinate(x,y) returns false, the board remains unchanged.
     * 
     * @param x the x-coordinate of the cell to be deflagged
     * @param y the y-coordinate of the cell to be deflagged
     */
    public synchronized void deflag(int x, int y) {
        if (isValidBoardCoordinate(x,y) && isFlagged(x,y))
            cellStates[x][y] = CellState.UNTOUCHED;
        checkRep();
    }
    
    // ---------------------------------------------- Other Methods ------------------------------------------------
    
    /**
     * Returns a String representation of the board that consists of a series of newline-separated 
     * rows of space-separated characters, thereby giving a grid representation of the board’s state
     * with exactly one char for each square. The mapping of characters is as follows:
     *      “-” for squares with state untouched.
     *      “F” for squares with state flagged.
     *      “ ” (space) for squares with state dug and 0 neighbors that have a bomb.
     *      integer COUNT in range [1-8] for squares with state dug and COUNT neighbors that have a bomb.
     *      
     *  Coordinates work from the origin (0,0) on the top left, x-coordinates increase leftware, and y-coordinates
     *  increasing downward.
     *  
     *  @return A string representing the board as described in this spec.
     */
    @Override
    public synchronized String toString() {
        StringWriter output = new StringWriter();       // StringWriter represents a stream for building a String
        PrintWriter out = new PrintWriter(output);      // PrintWriter used on this stream for platform-independence of println()
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width-1; x++) {
                if (isUntouched(x,y))       out.print("- ");
                else if (isFlagged(x,y))    out.print("F ");
                else {
                    int c = getMineCount(x,y);
                    out.print(c == 0 ? " " : c);
                    out.print(" ");
                }
            }
            
            if (isUntouched(width-1,y))       out.print("-");
            else if (isFlagged(width-1,y))    out.print("F");
            else {
                int c = getMineCount(width,y);
                out.print(c == 0 ? " " : c);
            }
            
            out.println();
        }
        
        out.close();
        return output.toString().replaceAll("\\s+$","");
    }
    
    /**
     * Returns a String representation of the board that consists of a series of newline-separated 
     * rows of space-separated characters, giving a grid representation of the board’s private state of 
     * mines on the board, with exactly one char for each square. The mapping of characters is as follows:
     *      “-” for squares with no mines.
     *      “*” for squares with a mine.
     *      
     *  Coordinates work from the origin (0,0) on the top left, x-coordinates increase leftware, and y-coordinates
     *  increasing downward.
     *  
     *  @return A string representing the board as described in this spec.
     */
    public synchronized String toMineString() {
        StringWriter output = new StringWriter();
        PrintWriter out = new PrintWriter(output);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width-1; x++) {
                if (containsMine(x,y)) out.print("* ");
                else                   out.print("- ");
            }
            
            out.print(containsMine(width-1,y) ? "*" : "-");
            out.println();
        }
        
        out.close();
        return output.toString().replaceAll("\\s+$","");
    }
    
    // ---------------------------------------------- Helper Methods -----------------------------------------------
    
    /*
     *  Performs the recursive dig procedure specified in the specification of the DIG operation.
     *  Sets cell (x,y) to the dug state, and, if there are no mines in the surrounding cells, recursively
     *  calls on the dig procedure to set those cells to the dug state as well.
     */
    private void recursiveDig(int x, int y) {
        cellStates[x][y] = CellState.DUG;
        
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                if (isValidBoardCoordinate(x+dx,y+dy) && containsMine(x+dx, y+dy)) return;
            }
        
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                if (isValidBoardCoordinate(x+dx,y+dy) && isUntouched(x+dx, y+dy)) recursiveDig(x+dx, y+dy);
            }
    }
    
    /*
     *  Counts the number of mines surrounding cell (i,j), and returns the count.
     */
    private int countSurroundingMines(int x, int y) {
        int count = 0;
        
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                if (isValidBoardCoordinate(x+dx,y+dy) && containsMine(x+dx,y+dy)) count++;
            }
        
        return count;
    }
    
    /*
     *  Places a mine at cell (x,y) and updates mine counts in mineCounts array
     */
    private void placeMine(int x, int y) {
        mines[x][y] = true;
        
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                if (isValidBoardCoordinate(x+dx,y+dy)) mineCounts[x+dx][y+dy]++;;
            }
        
//        checkRep();       This method is called only during object construction so we cannot check the rep
    }
    
    /*
     *  Removes a mine at cell (x,y) and updates mine counts in mineCounts array
     */
    private void removeMine(int x, int y) {
        mines[x][y] = false;
        
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                if (isValidBoardCoordinate(x+dx,y+dy) && mineCounts[x+dx][y+dy] > 0) mineCounts[x+dx][y+dy]--;
            }
        
        checkRep();
    }
    
    // =============================================== STATIC METHODS ===============================================
    
    /*
     *  Returns an array of n random distinct integers, in the range of [min, max)
     */
    private static int[] randomIntegers(int min, int max, int n) {
        List<Integer> list = new ArrayList<Integer>(max-min);
        for (int num = min; num < max; num++) list.add(num);
        
        Collections.shuffle(list);
        
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = list.get(i);
        return arr;
    }
    
    public static void main(String[] args) {
        Board board = new Board(new File("largeBoard.txt"));
        System.out.println(board);
    }
    
}
