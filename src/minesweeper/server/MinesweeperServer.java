package minesweeper.server;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import minesweeper.Board;

/**
 * Represents a server for thread-safe concurrent playing of Multiplayer Minesweeper over a network.
 */
public class MinesweeperServer {
    
    /** Maximum allowed port for server connection. */
    public static final int MAXIMUM_PORT = 65535;  
    
    private static final int DEFAULT_PORT = 4444;               // Default server port.
    private static final int DEFAULT_SIZE = 10;                 // Default square board size.
    
    private final ServerSocket serverSocket;                    // Socket for receiving incoming connections.
    private final boolean debug;                                // True if the server should *not* disconnect a client after a BOOM message.
    private final Board board;                                  // Board of Minesweeper game being played on server.
    private final AtomicInteger players = new AtomicInteger();  // A synchronized count of players on the server.
    
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
     * @param board the Minesweeper board object to be used by the server
     * @throws IOException if an error occurs opening the server socket
     */
    private MinesweeperServer(int port, boolean debug, Board board) throws IOException {
        serverSocket = new ServerSocket(port);
        this.debug = debug;
        this.board = board;
    }

    /**
     * Run the server, listening for client connections and handling them.
     * Never returns unless an exception is thrown.
     * 
     * @throws IOException if the main server socket is broken
     *                     (IOExceptions from individual clients do *not* terminate serve())
     */
    private synchronized void serve() throws IOException {
        while (true) {
            Socket socket = serverSocket.accept();
            players.incrementAndGet();
            new Thread(new MinesweeperClientHandler(socket, debug, board, players)).start();
        }
    }
    
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
        if (port == 0) port = DEFAULT_PORT;
        if (sizeX == 0 || sizeY == 0) {
            sizeX = DEFAULT_SIZE;
            sizeY = DEFAULT_SIZE;
        }
        
        new MinesweeperServer(port, debug, 
                file.isPresent() ? new Board(file.get()) : new Board(sizeX, sizeY)).serve();
    }

}
