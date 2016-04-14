/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

import org.junit.Test;

/**
 * Unit test for single threaded (non-concurrent) use of Board data type.
 */
public class BoardTest {
    
    /*
     *  Testing Strategy
     *  ================
     *  Since abstract data types are defined by their operations, constructors
     *  are tested by assuring the results from the object methods are correct.
     *  
     *  First, the methods are tested by using the file constructor, so as to be
     *  able to test against expected results.
     *  Afterwards, the toString() and toMineString() tests are used to assure
     *  the Strings represent the Board as described.
     *  Finally, the methods are tested against the normal constructor, using
     *  the toString() and toMineString() method for expected results from the
     *  methods.
     *  
     *  There are 4 available board setup files that can be used to test
     *  
     *  getWidth(), getHeight()
     *          - Test that this returns value specified in constructor
     *  
     *  isValidBoardCoordinate(int x, int y)
     *          - Test Partition:
     *                  - x < 0     x = 0       0 < x < width       x >= width
     *                  - y < 0     y = 0       0 < y < height      y >= height
     *  
     *  isUntouched(int x, int y), isFlagged(int x, int y), hasBeenDug(int x, int y), containsMine(int x, int y)
     *          - Test Input Partition:
     *                  - x = 0       0 < x < width
     *                  - y = 0       0 < y < height
     *          - Test Output Partition:
     *                  - Should return true
     *                  - Should return false
     *          - Test these methods with file constructor to know what expected values to test against.
     *          
     *  containsMine(int x, int y)
     *          - Test Input Partition:
     *                  - x = 0       0 < x < width
     *                  - y = 0       0 < y < height
     *          - Test Output Partition:
     *                  - Should return true
     *                  - Should return false
     *          - Test these methods with file constructor to know what expected values to test against.
     *  
     *  getMineCount(int x, int y)
     *          - Test Input Partition:
     *                  - x < 0     x = 0       0 < x < width       x >= width
     *                  - y < 0     y = 0       0 < y < height      y >= height
     *          - Test Output Partition:
     *                  - Should return 0
     *                  - Should return positive integer
     *          - Test these methods with file constructor to know what expected values to test against
     *          
     *  dig(int x, int y), flag(int x, int y), deflag(int x, int y)
     *          - Test Input Partition:
     *                  - x < 0     x = 0       0 < x < width       x >= width
     *                  - y < 0     y = 0       0 < y < height      y >= height
     *          
     *          - Test correct mutation by using observer methods
     *          - Test these methods with file constructor to know what expected states to test against
     *  
     *  toString(), toMineString()
     *          - Test on Board objects created with file constructor
     *          - Mutate with mutator methods
     *          
     *          - After testing above object methods with file constructor, we can assure data type representation
     *            functionality is coherent with toString() and toMineString(), and so we can test the above methods
     *            of Board with values we expect give the toString() and toMineString() representation
     */
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    // Change this to test with one of the four different board files
    public static final String BOARD_NAME = "mediumBoard";
    
    // Do not change these fields
    public static final File BOARD_FILE = new File(BOARD_NAME + ".txt");
    public static final File MINECOUNTS_FILE = new File(BOARD_NAME + "_MineCounts.txt");
    public static final Board BOARD = new Board(BOARD_FILE);
    
    @Test public void testFileBoardWidthAndHeight() {
        try (
            Scanner in = new Scanner(new FileReader(BOARD_FILE));
        ){
            
            
            assertEquals(BOARD.getWidth(), in.nextInt());
            assertEquals(BOARD.getHeight(), in.nextInt());
        
            
        } catch (FileNotFoundException e) {
            fail("Test board file not found.");
        }
    }
    
    @Test public void testFileBoardValidBoardCoordinate() {
        try (
            Scanner in = new Scanner(new FileReader(BOARD_FILE));
        ){
            
            
            int width = in.nextInt();
            int height = in.nextInt();
            
            assertFalse(BOARD.isValidBoardCoordinate(-1, -1));
            assertTrue(BOARD.isValidBoardCoordinate(0, 0));
            assertEquals(1 < width, BOARD.isValidBoardCoordinate(1, 0));
            assertEquals(1 < height, BOARD.isValidBoardCoordinate(0, 1));
            assertFalse(BOARD.isValidBoardCoordinate(width, 0));
            assertFalse(BOARD.isValidBoardCoordinate(0, height));
            
            
        } catch (FileNotFoundException e) {
            fail("Test board file not found.");
        }
    }
    
    @Test public void testFileBoardIsInStateMethodsNoMutation() {
        try (
            Scanner in = new Scanner(new FileReader(BOARD_FILE));
        ){
            
            
            int width = in.nextInt();
            int height = in.nextInt();
            
            for(int x = 0; x < width; x++)
                for (int y = 0; y < height; y++) {
                    assertTrue(BOARD.isUntouched(x, y));
                    assertFalse(BOARD.isFlagged(x, y));
                    assertFalse(BOARD.hasBeenDug(x, y));
                }
            
            
        } catch (FileNotFoundException e) {
            fail("Test board file not found.");
        }
    }
    
    @Test public void testFileBoardContainsMine() {
        try (
            Scanner in = new Scanner(new FileReader(BOARD_FILE));
        ){
            
            
            int width = in.nextInt();
            int height = in.nextInt();
            
            for (int y = 0; y < height; y++)
                for(int x = 0; x < width; x++) 
                    assertEquals(in.nextInt() == 1, BOARD.containsMine(x, y));
            
            
        } catch (FileNotFoundException e) {
            fail("Test board file not found.");
        }
    }
    
    @Test public void testFileBoardMineCount() {
        try (
            Scanner in = new Scanner(new FileReader(MINECOUNTS_FILE));
        ){
            
            
            int width = in.nextInt();
            int height = in.nextInt();
            
            for (int y = 0; y < height; y++)
                for (int x = 0; x < width; x++)
                    assertEquals(in.nextInt(), BOARD.getMineCount(x, y));
            
            
        } catch (FileNotFoundException e) {
            fail("Test board file not found.");
        }
    }

    // TODO Finish tests.
}
