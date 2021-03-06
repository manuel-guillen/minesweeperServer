package minesweeper.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import minesweeper.Board;

/**
 * Represents a server for thread-safe concurrent playing of Multiplayer Minesweeper over a network.
 */
public class MinesweeperServer {
    
    public static final int DEFAULT_PORT = 4444;
    private static final int MAXIMUM_PORT = 65535;
    
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
     *  No observer methods, serverSocket is confined to object (no references leaked)
     *  Field board is leaked to a constructor that does not maintain a reference to the object (see MinesweeperServerStatus class)
     */
    
    /*
     *  System Thread Safety Argument
     *  =============================
     *  External threads:
     *  Private visibility is used to ensure this MinesweeperServer object is not called to serve()
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
        if (port < 0 || port > MAXIMUM_PORT) throw new IllegalArgumentException("Port number outside valid range.");
        
        serverSocket = new ServerSocket(port);
        this.debug = debug;
        this.board = board;
    }
    
    /**
     * Run the server, listening for client connections and handling them.
     * Never returns unless main server socket is broken
     */
    private void serve() {
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                players.incrementAndGet();
                new Thread(new MinesweeperClientHandler(socket, debug, board, players)).start();
            }
        } catch (IOException e) {
            return;
        }
    }
    
    /**
     * Returns the number of players connected to the server
     * @return the number of players connected to this server
     */
    public int playerCount() {
        return players.get();
    }
    
    /**
     * Returns the width of the Minesweeper board of this server
     * @return the width of the Minesweeper board (the number of columns)
     */
    public int getBoardWidth() {
        return board.getWidth();
    }
    
    /**
     * Returns the height of the Minesweeper board of this server
     * @return the height of the Minesweeper board (the number of rows)
     */
    public int getBoardHeight() {
        return board.getHeight();
    }
    
    /**
     * Returns true if the Minesweeper board of this server has a mine at cell (x,y). Returns false otherwise.
     * @param x a nonnegative integer less than the width of the board
     * @param y a nonnegative integer less than the height of the board
     * @return true if the Minesweeper board of this server has a mine at cell (x,y); false otherwise.
     */
    public boolean boardHasMine(int x, int y) {
        return board.containsMine(x, y);
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
     * @throws IOException if an error occurs opening socket.
     */
    public static MinesweeperServer runMinesweeperServer(boolean debug, Optional<File> file, int sizeX, int sizeY, int port) throws IOException {
        MinesweeperServer server = new MinesweeperServer(port, debug, 
                                                    file.isPresent() ? new Board(file.get()) : new Board(sizeX, sizeY));
        new Thread(() -> server.serve()).start();
        return server;
    }

}
