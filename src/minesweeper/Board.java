/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
     *      - The state of each cell on the board is represented by the array cellStates, where a cell (i,j) is
     *        either "untouched", "flagged", or "dug", as represented by the CellState enum at cellStates[i][j]
     *        
     *      - The location of mines on the board is represented by the array mines, where a cell (i,j) has a
     *        mine if and only if mines[i][j]
     *        
     *      - The number of mines surrounding a cell on the board is represented by the array mineCounts, where
     *        a cell (i,j) has mineCounts[i,j] mines in its surrounding 8 cells.
     *      
     *  (NOTE: (0,0) is top-left, and (i,j) cells have increasing i moving right on the abstract board and
     *         have increasing j moving downward on the abstract board
     */
    
    /*
     *  Rep Invariant
     *  =============
     *  cellStates is an array of width arrays, each of length height
     *  mines is an array of width arrays, each of length height
     *  mineCounts is an array of width arrays, each of length height
     *  
     *  For all 0 <= i < width, 0 <= j < height:
     *      - mineCounts[i][j] is equal to the number of the following elements that are true:
     *                                          1) mines[i+1][j]
     *                                          2) mines[i+1][j-1]
     *                                          3) mines[i][j-1]
     *                                          4) mines[i-1][j-1]
     *                                          5) mines[i-1][j]
     *                                          6) mines[i-1][j+1]
     *                                          7) mines[i][j+1]
     *                                          8) mines[i+1][j+1]
     *      
     *      - If cellStates[i][j] = DUG, mines[i][j] = false
     */
    
    /*
     *  Safety from Rep Exposure
     *  ========================
     *  All fields are private and have immutable references
     *  The fields width and height are immutable
     *  
     */
    
    /*
     *  System thread safety argument
     *  =============================
     *  TODO Problem 5
     */
    
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
        
        int[] mineLocations = randomIntegers(0, width*height, width*height/3);
        for (int n : mineLocations) {
            int x = n / height, y = n % height;
            mines[x][y] = true;
        }
        
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++) {
                cellStates[x][y] = CellState.UNTOUCHED;
                mineCounts[x][y] = countSurroundingMines(x,y);
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
            
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    cellStates[x][y] = CellState.UNTOUCHED;
                    mines[x][y] = (in.nextInt() == 1);
                }
            }
            
            checkRep();
            
        } catch (IOException e) {
            throw new RuntimeException("Illegal file.");
        }
    }
    

    /*
     *  Asserts the rep invariant.
     */
    private void checkRep() {
        assert cellStates.length == width;
        assert mines.length == width;
        assert mineCounts.length == width;
        
        for (int i = 0; i < width; i++) {
            assert cellStates[i].length == height;
            assert mines[i].length == height;
            assert mineCounts[i].length == height;
            
            for (int j = 0; j < height; j++) {
                assert mineCounts[i][j] == countSurroundingMines(i,j);
                if (cellStates[i][j].equals(CellState.DUG)) assert !mines[i][j];
            }
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
                if (isValidBoardCoordinate(x+dx,y+dy) && mines[x+dx][y+dy]) count++;
            }
        
        return count;
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
     * Returns true if there is a mine in cell (x,y) of this Minesweeper board.
     * Requires that x,y be integers such that isValidBoardCoordinate(x,y) returns true.
     * 
     * @param x the x-coordinate of cell being checked
     * @param y the y-coordinate of cell being checked
     * @return true if there is a mine in cell (x,y) of this Minesweeper board. Returns false, otherwise.
     */
    public boolean containsMine(int x, int y) {
        return mines[x][y];
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
    public boolean isFlagged(int x, int y) {
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
    public boolean hasBeenDug(int x, int y) {
        return cellStates[x][y].equals(CellState.DUG);
    }
    
    /**
     * Returns the number of mines in the 8 cells surrounding the cell (x,y) on this Minesweeper board.
     * Requires that x,y be integers such that isValidBoardCoordinate(x,y) return true.
     * 
     * @param x the x-coordinate of cell being checked
     * @param y the y-coordinate of cell being checked
     * @return true the number of mines surrounding the cell (x,y) on this Minesweeper board.
     */
    public int mineCount(int x, int y) {
        return mineCounts[x][y];
    }
    
    public void dig(int x, int y) {
        
    }
    
    public void flag(int x, int y) {
        cellStates[x][y] = CellState.FLAGGED;
    }
    
    public void deflag(int x, int y) {
        
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
