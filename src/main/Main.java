package main;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Queue;

import minesweeper.server.MinesweeperServer;

/**
 * Entry-point for initiation of a MinesweeperServer. Main method initiates a Minesweeper server
 *
 * <br> Usage:
 *      MinesweeperServer [--debug | --no-debug] [--port PORT] [--size SIZE_X,SIZE_Y | --file FILE]
 * 
 * <br> The --debug argument means the server should run in debug mode. The server should disconnect a
 *      client after a BOOM message if and only if the --debug flag was NOT given.
 *      Using --no-debug is the same as using no flag at all.
 * <br> E.g. "MinesweeperServer --debug" starts the server in debug mode.
 * 
 * <br> PORT is an optional integer in the range 0 to 65535 inclusive, specifying the port the server
 *      should be listening on for incoming connections.
 * <br> E.g. "MinesweeperServer --port 1234" starts the server listening on port 1234.
 * 
 * <br> SIZE_X and SIZE_Y are optional positive integer arguments, specifying that a random board of size
 *      SIZE_X*SIZE_Y should be generated.
 * <br> E.g. "MinesweeperServer --size 42,58" starts the server initialized with a random board of size
 *      42*58.
 * 
 * <br> FILE is an optional argument specifying a file pathname where a board has been stored. If this
 *      argument is given, the stored board should be loaded as the starting board.
 * <br> E.g. "MinesweeperServer --file boardfile.txt" starts the server initialized with the board stored
 *      in boardfile.txt.
 * 
 * <br> The board file format, for use with the "--file" option, is specified by the following grammar:
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
 * <br> If neither --file nor --size is given, generate a random board of size 10x10.
 * 
 * <br> Note that --file and --size may not be specified simultaneously.
 *
 */
public class Main {

    /**
     * Start a MinesweeperServer using the given argument.
     * 
     * @param args arguments as described
     */
    public static void main(String[] args) {
        boolean debug = false;
        int port = 0, sizeX = 0, sizeY = 0;
        Optional<File> file = Optional.empty();

        Queue<String> arguments = new LinkedList<String>(Arrays.asList(args));
        try {
            while (!arguments.isEmpty()) {
                String flag = arguments.remove();
                try {
                    switch(flag) {
                    case "--debug":     debug = true;   break;
                    case "--no-debug":  debug = false;  break;
                        
                    case "--port":      
                        port = Integer.parseInt(arguments.remove());
                        if (port < 0 || port > MinesweeperServer.MAXIMUM_PORT)
                            throw new IllegalArgumentException("port " + port + " out of range");
                        break;
                    
                    case "--size":
                        String[] sizes = arguments.remove().split(",");
                        sizeX = Integer.parseInt(sizes[0]);
                        sizeY = Integer.parseInt(sizes[1]);
                        break;
                    
                    case "--file":
                        file = Optional.of(new File(arguments.remove()));
                        if (!file.get().isFile())
                            throw new IllegalArgumentException("file not found: \"" + file.get() + "\"");
                        break;
                    
                    default:
                        throw new IllegalArgumentException("unknown option: \"" + flag + "\"");
                    }

                } catch (NoSuchElementException nsee) {
                    throw new IllegalArgumentException("missing argument for " + flag);
                    
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("unable to parse number for " + flag);
                }
            }
            
        } catch (IllegalArgumentException iae) {
            System.err.println(iae.getMessage());
            System.err.println("usage: MinesweeperServer [--debug | --no-debug] [--port PORT] [--size SIZE_X,SIZE_Y | --file FILE]");
            return;
        }

        try {
            MinesweeperServer.runMinesweeperServer(debug, file, sizeX, sizeY, port);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    
}
