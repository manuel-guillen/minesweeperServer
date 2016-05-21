package main;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import minesweeper.gui.MinesweeperServerFrame;

/**
 * Entry-point for initiation of a MinesweeperServer. Main method initiates a Minesweeper server
 * 
 * <br> PORT is an integer in the range 0 to 65535 inclusive, specifying the port the server
 *      should be listening on for incoming connections, default set to 4444 on UI.
 * 
 * <br> FILE is an optional argument specifying a file pathname where a board has been stored. If this
 *      argument is given, the stored board should be loaded as the starting board.
 * 
 * <br> The board file format is specified by the following grammar:
 * <pre>
 *   FILE ::= BOARD LINE+
 *   BOARD ::= X SPACE Y NEWLINE
 *   LINE ::= (VAL SPACE)* VAL NEWLINE
 *   VAL ::= 0 | 1
 *   X ::= INT
 *   Y ::= INT
 *   SPACE ::= " "
 *   NEWLINE ::= "\n" | "\r" "\n"?
 *   INT ::= [0-9]+
 * </pre>
 *
 */
public class Main {

    /**
     * Start a MinesweeperServerFrame UI.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default look-and-feel
        }
        
        MinesweeperServerFrame.startMinesweeperServerFrameUI();
    }
    
}
