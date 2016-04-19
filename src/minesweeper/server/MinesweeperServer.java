/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper.server;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import minesweeper.Board;

/**
 * Represents a server for thread-safe concurrent playing of Multiplayer Minesweeper over a network.
 */
public class MinesweeperServer {
    
    private static final int DEFAULT_PORT = 4444;   // Default server port.
    private static final int MAXIMUM_PORT = 65535;  // Maximum port number as defined by ServerSocket.
    private static final int DEFAULT_SIZE = 10;     // Default square board size.
    
    
    private final ServerSocket serverSocket;        // Socket for receiving incoming connections.
    private final boolean debug;                    // True if the server should *not* disconnect a client after a BOOM message.
    private final Board board;                      // Board of Minesweeper game being played on server.
    private final AtomicInteger players;            // A synchronized count of players on the server.
    
    /*
     *  Abstraction Function
     *  ====================
     *  Represents a server for a multiplayer Minesweeper game, with game board represented by board,
     *  with a server socket listening for incoming server connection requests, and with players number of players
     *  connected to the server.
     *  (debug represents whether or not server is in debug setting - see field comment)
     *  
     */
    
    /*
     *  Rep Invariant
     *  =============
     *  true
     */
    
    /*
     *  Safety from Rep Exposure
     *  ========================
     *  All field references are private and final
     *  No observer methods, all field references are kept within class, no references are leaked
     */
    
    /*
     *  System Thread Safety Argument
     *  =============================
     *  External threads:
     *  Synchronization is used to ensure this MinesweeperServer object is not called to serve()
     *  while already serving. Since this is the only object method, this prevents threads with access
     *  to this MinesweeperServer object to interfere with each other.
     *  
     *  Internal threads:
     *  Threads are initiated by this MinesweeperServer. Thread safety is implemented as follows:
     *      - serverSocket      Confinement             Threads initiated here do not have access to serverSocket
     *      - debug             Immutability            Threads cannot modify the immutable value of debug
     *      - board             Synchronization         Threads can only mutate board once at a time, since board is
     *                                                  a thread-safe data type (see Board spec and thread-safety argument)
     *      - players           Thread-safe datatype    AtomicInteger is a thread-safe data type that provides atomic method calls.
     */


    /**
     * Make a MinesweeperServer that listens for connections on port.
     * 
     * @param port port number, requires 0 <= port <= 65535
     * @param debug debug mode flag
     * @throws IOException if an error occurs opening the server socket
     */
    public MinesweeperServer(int port, boolean debug, Board board) throws IOException {
        serverSocket = new ServerSocket(port);
        this.debug = debug;
        this.board = board;
        this.players = new AtomicInteger();
    }

    /**
     * Run the server, listening for client connections and handling them.
     * Also prints IP address & port number for server connection in standard output,
     * as well as the distribution of mines on the Minesweeper board (private information
     * on the server)
     * Never returns unless an exception is thrown.
     * 
     * @throws IOException if the main server socket is broken
     *                     (IOExceptions from individual clients do *not* terminate serve())
     */
    public synchronized void serve() throws IOException {
        System.out.println("Server IP Address: " + Inet4Address.getLocalHost().getHostAddress());
        System.out.println("Server Port: " + serverSocket.getLocalPort());
        System.out.println("Board - Mine Layout:");
        System.out.println(board.toMineString());
        System.out.println();
        
        while (true) {
            Socket socket = serverSocket.accept();
            players.incrementAndGet();
            new Thread(new MinesweeperClientHandler(socket, debug, board, players)).start();
        }
    }

    // ========================================================STATIC METHODS=========================================================
    
    /**
     * Start a MinesweeperServer running on the specified port, with either a random new board or a
     * board loaded from a file.
     * 
     * @param debug The server will disconnect a client after a BOOM message if and only if debug is false.
     * @param file If file.isPresent(), start with a board loaded from the specified file,
     *             according to the input file format defined in the documentation for main(..).
     * @param sizeX If (!file.isPresent()), start with a random board with width sizeX
     *              (and require sizeX > 0).
     * @param sizeY If (!file.isPresent()), start with a random board with height sizeY
     *              (and require sizeY > 0).
     * @param port The network port on which the server should listen, requires 0 <= port <= 65535.
     * @throws IOException if a network error occurs
     */
    public static void runMinesweeperServer(boolean debug, Optional<File> file, int sizeX, int sizeY, int port) throws IOException {
        MinesweeperServer server = new MinesweeperServer(port, debug,
                                                file.isPresent() ? new Board(file.get()) : new Board(sizeX, sizeY));
        server.serve();
    }

    // ==========================================================MAIN METHOD==========================================================
    
    /**
     * Start a MinesweeperServer using the given arguments.
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
     * @param args arguments as described
     */
    public static void main(String[] args) {
        // Command-line argument parsing is provided. Do not change this method.
        boolean debug = false;
        int port = DEFAULT_PORT;
        int sizeX = DEFAULT_SIZE;
        int sizeY = DEFAULT_SIZE;
        Optional<File> file = Optional.empty();

        Queue<String> arguments = new LinkedList<String>(Arrays.asList(args));
        try {
            while ( ! arguments.isEmpty()) {
                String flag = arguments.remove();
                try {
                    if (flag.equals("--debug")) {
                        debug = true;
                    } else if (flag.equals("--no-debug")) {
                        debug = false;
                    } else if (flag.equals("--port")) {
                        port = Integer.parseInt(arguments.remove());
                        if (port < 0 || port > MAXIMUM_PORT) {
                            throw new IllegalArgumentException("port " + port + " out of range");
                        }
                    } else if (flag.equals("--size")) {
                        String[] sizes = arguments.remove().split(",");
                        sizeX = Integer.parseInt(sizes[0]);
                        sizeY = Integer.parseInt(sizes[1]);
                        file = Optional.empty();
                    } else if (flag.equals("--file")) {
                        sizeX = -1;
                        sizeY = -1;
                        file = Optional.of(new File(arguments.remove()));
                        if ( ! file.get().isFile()) {
                            throw new IllegalArgumentException("file not found: \"" + file.get() + "\"");
                        }
                    } else {
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
            runMinesweeperServer(debug, file, sizeX, sizeY, port);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

}
