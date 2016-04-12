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
 * Represents a Minesweeper game board.
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
     *        either "untouched", "flagged", or "dug", as represented by the CellState enum at cellStates[y][x]
     *        
     *      - The location of mines on the board is represented by the array mines, where a cell (x,y) has a
     *        mine if and only if mines[y][x]
     *        
     *      - The number of mines surrounding a cell on the board is represented by the array mineCounts, where
     *        a cell (x,y) has mineCounts[y][x] mines in its surrounding 8 cells.
     *      
     *  (NOTE: (0,0) is top-left, and (i,j) cells have increasing i moving right on the abstract board and
     *         have increasing j moving downward on the abstract board. A point (x,y) is represented with array
     *         index [y][x] since 2D arrays in Java work with [row][column] indexing.
     */
    
    /*
     *  Rep Invariant
     *  =============
     *  cellStates is an array of height arrays, each of length width
     *  mines is an array of height arrays, each of length width
     *  mineCounts is an array of height arrays, each of length width
     *  
     *  For all 0 <= x < width, 0 <= y < height:
     *      - mineCounts[y][x] is equal to the number of the following elements that are true:
     *                                          1) mines[y][x+1]
     *                                          2) mines[y-1][x+1]
     *                                          3) mines[y-1][x]
     *                                          4) mines[y-1][x-1]
     *                                          5) mines[y][x-1]
     *                                          6) mines[y+1][x-1]
     *                                          7) mines[y+1][x]
     *                                          8) mines[y+1][x+1]
     *      
     *      - If cellStates[y][x] = DUG, mines[y][x] = false
     */
    
    /*
     *  Safety from Rep Exposure
     *  ========================
     *  All fields are private and have immutable references
     *  The fields width and height are immutable
     *  TODO Finish.
     */
    
    /*
     *  System thread safety argument
     *  =============================
     *  TODO Problem 5
     */
    
    // ----------------------------------------------- Constructors -------------------------------------------------
    
    /**
     * Creates a Minesweeper board of dimensions width by height, with a third of
     * its cells (to the nearest whole number) have mines.
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
                    cellStates[y][x] = CellState.UNTOUCHED;
                    if (in.nextInt() == 1) placeMine(x,y);
                }
            }
            
            checkRep();
            
        } catch (IOException e) {
            throw new RuntimeException("Illegal file.");
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
        
        for (int y = 0; y < height; y++) {
            assert cellStates[y].length == width;
            assert mines[y].length == width;
            assert mineCounts[y].length == width;
            
            for (int x = 0; x < width; x++) {
                assert mineCounts[y][x] == countSurroundingMines(x,y);
                if (cellStates[y][x].equals(CellState.DUG)) assert !mines[y][x];
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
    public boolean isUntouched(int x, int y) {
        return cellStates[y][x].equals(CellState.UNTOUCHED);
    }
    
    /**
     * Returns true if the cell (x,y) of this Minesweeper board is in the flagged state.
     * Requires that x,y be integers such that isValidBoardCoordinate(x,y) returns true.
     * 
     * @param x the x-coordinate of cell being checked
     * @param y the y-coordinate of cell being checked
     * @return true if the cell (x,y) of this Minesweeper board is in the flagged state.
     */
    public boolean isFlagged(int x, int y) {
        return cellStates[y][x].equals(CellState.FLAGGED);
    }
    
    /**
     * Returns true if the cell (x,y) of this Minesweeper board is in the dug (opened) state.
     * Requires that x,y be integers such that isValidBoardCoordinate(x,y) return true.
     * 
     * @param x the x-coordinate of cell being checked
     * @param y the y-coordinate of cell being checked
     * @return true if the cell (x,y) of this Minesweeper board is in the dug (opened) state.
     */
    public boolean hasBeenDug(int x, int y) {
        return cellStates[y][x].equals(CellState.DUG);
    }
    
    /**
     * Returns true if there is a mine in cell (x,y) of this Minesweeper board.
     * Requires that x,y be integers such that isValidBoardCoordinate(x,y) returns true.
     * 
     * @param x the x-coordinate of cell being checked
     * @param y the y-coordinate of cell being checked
     * @return true if there is a mine in cell (x,y) of this Minesweeper board. Returns false, otherwise.
     */
    public boolean containsMine(int x, int y) {
        return mines[y][x];
    }
    
    /**
     * Returns the number of mines in the 8 cells surrounding the cell (x,y) on this Minesweeper board.
     * Requires that x,y be integers such that isValidBoardCoordinate(x,y) return true.
     * 
     * @param x the x-coordinate of cell being checked
     * @param y the y-coordinate of cell being checked
     * @return true the number of mines surrounding the cell (x,y) on this Minesweeper board.
     */
    public int getMineCount(int x, int y) {
        return mineCounts[y][x];
    }

    // ------------------------------------------------ Mutators ---------------------------------------------------

    /**
     * Performs a dig on cell (x,y) of this Minesweeper board, that is, the cell (x,y) is opened, and
     * returns true if the cell was untouched before and there was a mine at this cell. A dig on an untouched
     * cell changes it to the dug state. A dig on a flagged cell or dug cell does nothing. A dig on an untouched
     * cell removes the mine after execution of this method, to allow the game to proceed.
     * 
     * Requires that x,y be integers such that isValidBoardCoordinate(x,y) return true.
     * 
     * @param x the x-coordinate of cell being dug
     * @param y the y-coordinate of cell being dug
     * @return true if this cell was untouched before and had a mine at this location. Otherwise, return false.
     */
    public boolean dig(int x, int y) {
        if (isUntouched(x,y)) {
            setCellState(x,y,CellState.DUG);
            if (containsMine(x,y)) {
                removeMine(x,y);
                checkRep();
                return true;
            }
        }
        
        checkRep();
        return false;
    }
    
    public void flag(int x, int y) {
        if (isUntouched(x,y))
            setCellState(x,y,CellState.FLAGGED);
        checkRep();
    }
    
    public void deflag(int x, int y) {
        if (isFlagged(x,y))
            setCellState(x,y,CellState.UNTOUCHED);
        checkRep();
    }
    
    // ---------------------------------------------- Other Methods ------------------------------------------------
    
    @Override
    public String toString() {
        StringWriter output = new StringWriter();
        PrintWriter out = new PrintWriter(output);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (isUntouched(x,y))       out.print("-");
                else if (isFlagged(x,y))    out.print("F");
                else {
                    int c = getMineCount(x,y);
                    out.print(c == 0 ? " " : c);
                }
            }
            
            out.println();
        }
        
        out.close();
        return output.toString();
    }
    
    // ---------------------------------------------- Helper Methods -----------------------------------------------
    
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
    
    private void setCellState(int x, int y, CellState dug) {
        // TODO Auto-generated method stub
        
    }
    
    /*
     *  Places a mine at cell (x,y) and updates mine counts in mineCounts array
     */
    private void placeMine(int x, int y) {
        mines[y][x] = true;
        
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                if (isValidBoardCoordinate(x+dx,y+dy)) incrementMineCount(x+dx,y+dy);
            }
        
        checkRep();
    }
    
    /*
     *  Removes a mine at cell (x,y) and updates mine counts in mineCounts array
     */
    private void removeMine(int x, int y) {
        mines[y][x] = false;
        
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                if (isValidBoardCoordinate(x+dx,y+dy)) decrementMineCount(x+dx,y+dy);
            }
        
        checkRep();
    }
    
    private void incrementMineCount(int x, int y) {
        mineCounts[y][x]++;
    }
    
    private void decrementMineCount(int x, int y) {
        mineCounts[y][x]--;
        if (mineCounts[y][x] < 0) mineCounts[y][x] = 0;
    }
    

    // ========================================================STATIC METHODS=========================================================
    
    /**
     *  Returns an array of n random distinct integers, in the range of [min, max)
     *  @param min an integer denoting inclusive lower bound of the random integers
     *  @param max an integer, greater than min, denoting the exclusive upper bound of random integers
     *  @param n a positive integer less than or equal to max-min, denoting the number of distinct random integers
     *  to generate
     *  @return a list of n distinct integers in the range of [min, max)
     */
    private static int[] randomIntegers(int min, int max, int n) {
        List<Integer> list = new ArrayList<Integer>(max-min);
        for (int num = min; num < max; num++) list.add(num);
        
        Collections.shuffle(list);
        
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = list.get(i);
        return arr;
    }
}
