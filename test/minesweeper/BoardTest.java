/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
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
     *  There are 4 available board setup files that can be used to test.
     *  Methods are tested using file constructor to know what expected values to test against.
     *  
     *  getWidth(), getHeight()
     *          - Test that this returns correct value
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
     *          
     *  containsMine(int x, int y)
     *          - Test Input Partition:
     *                  - x = 0       0 < x < width
     *                  - y = 0       0 < y < height
     *          - Test Output Partition:
     *                  - Should return true
     *                  - Should return false
     *  
     *  getMineCount(int x, int y)
     *          - Test Input Partition:
     *                  - x < 0     x = 0       0 < x < width       x >= width
     *                  - y < 0     y = 0       0 < y < height      y >= height
     *          - Test Output Partition:
     *                  - Should return 0
     *                  - Should return positive integer
     *          
     *  dig(int x, int y), flag(int x, int y), deflag(int x, int y)
     *          - Test Input Partition:
     *                  - x < 0     x = 0       0 < x < width       x >= width
     *                  - y < 0     y = 0       0 < y < height      y >= height
     *          
     *          - Test correct mutation by using observer methods
     *  
     *  toString(), toMineString()
     *          - Test on Board objects created with file constructor
     *          - Mutate with mutator methods
     */
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    // Change this to test with one of the four different board files
    public static final String BOARD_NAME = "largeBoard";
    
    // Do not change these fields
    public static final File BOARD_FILE = new File(BOARD_NAME + ".txt");
    public static final File MINECOUNTS_FILE = new File(BOARD_NAME + "_MineCounts.txt");
    
    @Test public void testFileBoardWidthAndHeight() {
        Board board = new Board(BOARD_FILE);
        
        try (
            Scanner in = new Scanner(new FileReader(BOARD_FILE));
        ){
            
            
            assertEquals(board.getWidth(), in.nextInt());
            assertEquals(board.getHeight(), in.nextInt());
        
            
        } catch (FileNotFoundException e) {
            fail("Test board file not found.");
        }
    }
    
    @Test public void testFileBoardValidBoardCoordinate() {
        Board board = new Board(BOARD_FILE);
        
        try (
            Scanner in = new Scanner(new FileReader(BOARD_FILE));
        ){
            
            
            int width = in.nextInt();
            int height = in.nextInt();
            
            assertFalse(board.isValidBoardCoordinate(-1, -1));
            assertTrue(board.isValidBoardCoordinate(0, 0));
            assertEquals(1 < width, board.isValidBoardCoordinate(1, 0));
            assertEquals(1 < height, board.isValidBoardCoordinate(0, 1));
            assertFalse(board.isValidBoardCoordinate(width, 0));
            assertFalse(board.isValidBoardCoordinate(0, height));
            
            
        } catch (FileNotFoundException e) {
            fail("Test board file not found.");
        }
    }
    
    @Test public void testFileBoardIsInStateMethodsNoMutation() {
        Board board = new Board(BOARD_FILE);
        
        try (
            Scanner in = new Scanner(new FileReader(BOARD_FILE));
        ){
            
            
            int width = in.nextInt();
            int height = in.nextInt();
            
            for(int x = 0; x < width; x++)
                for (int y = 0; y < height; y++) {
                    assertTrue(board.isUntouched(x, y));
                    assertFalse(board.isFlagged(x, y));
                    assertFalse(board.hasBeenDug(x, y));
                }
            
            
        } catch (FileNotFoundException e) {
            fail("Test board file not found.");
        }
    }
    
    @Test public void testFileBoardContainsMine() {
        Board board = new Board(BOARD_FILE);
        
        try (
            Scanner in = new Scanner(new FileReader(BOARD_FILE));
        ){
            
            
            int width = in.nextInt();
            int height = in.nextInt();
            
            for (int y = 0; y < height; y++)
                for(int x = 0; x < width; x++) 
                    assertEquals(in.nextInt() == 1, board.containsMine(x, y));
            
            
        } catch (FileNotFoundException e) {
            fail("Test board file not found.");
        }
    }
    
    @Test public void testFileBoardMineCount() {
        Board board = new Board(BOARD_FILE);
        
        try (
            Scanner in = new Scanner(new FileReader(MINECOUNTS_FILE));
        ){
            
            
            int width = board.getWidth();
            int height = board.getHeight();
            
            for (int y = 0; y < height; y++)
                for (int x = 0; x < width; x++)
                    assertEquals(in.nextInt(), board.getMineCount(x,y));
            
        } catch (FileNotFoundException e) {
            fail("Test board file not found.");
        }
    }
    
    @Test public void testFileBoardFlaggingMutationAndStates() {
        Board board = new Board(BOARD_FILE);
        
        for (int x = 0; x < board.getWidth(); x++) {
            for (int y = 0; y < board.getHeight(); y++) {
                
                board.flag(x,y);
                assertTrue(board.isFlagged(x,y));
                assertFalse(board.isUntouched(x,y));
                    
                board.deflag(x,y);
                assertFalse(board.isFlagged(x,y));
                assertTrue(board.isUntouched(x,y));
            }
        }
    }
    
    @Test public void testFileBoardDigMutationAndStates() {
        Board board = new Board(BOARD_FILE);
        
        for (int x = 0; x < board.getWidth(); x++) {
            for (int y = 0; y < board.getHeight(); y++) {
                
                boolean hasMine = board.containsMine(x,y);
                boolean returnedValue = board.dig(x,y);
                    
                assertEquals(hasMine, returnedValue);
                assertTrue(board.hasBeenDug(x,y));
                assertFalse(board.isUntouched(x,y));
                
                board.flag(x,y);
                assertFalse(board.isFlagged(x,y));
                
                board.deflag(x,y);
                assertFalse(board.isUntouched(x,y));
            }
        }
    }
    
    @Test public void testFileBoardDigMutationAndStates2() {
        Board board = new Board(BOARD_FILE);
        int width = board.getWidth(),
            height = board.getHeight();
        
        int cells = width*height;
        double flagRate = 0.4;
        
        int[] randomLocations = Board.randomIntegers(0, cells, (int)(cells*flagRate));
        
        for (int n : randomLocations) {
            int x = n % width, y = n / width;
            board.flag(x,y);
        }            
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                
                if (board.isFlagged(x,y)) {
                    board.dig(x,y);
                    
                } else {
                    board.dig(x,y);
                }
                
            }
        }
        
        for (int n : randomLocations) {
            int x = n % width, y = n / width;
            
            assertTrue(board.isFlagged(x,y));
            assertFalse(board.hasBeenDug(x,y));
        }
    }
    
    @Test public void testFileBoardMineString() {
        StringWriter s = new StringWriter();
        
        Board board = new Board(BOARD_FILE);
        
        try (
            Scanner in = new Scanner(new FileReader(BOARD_FILE));
            PrintWriter out = new PrintWriter(s);
        ){
            in.nextLine();  // Discard first line
            
            while(in.hasNextLine()) {
                String line = in.nextLine();
                line = line.replaceAll("0", "-");
                line = line.replaceAll("1", "*");
                out.println(line);
            }
        
        } catch (FileNotFoundException e) {
            fail("Test board file not found.");
        }
        
        String expected = s.toString().replaceAll("[\r\n]+$", "");
        assertEquals(expected, board.toMineString());
    }
    
    @Test public void testFileBoardToString() {
        // TODO
    }
}
