package minesweeper.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import minesweeper.Board;

/**
 * Represents a handler for a single Minesweeper Server client.
 */
public class MinesweeperClientHandler implements Runnable {

    private final Socket socket;
    private final Board board;
    private final boolean debug;
    private final AtomicInteger players;
    
    /*
     *  Abstraction Function
     *  ====================
     *  Represents a handler for a multiplayer Minesweeper Server game client, where
     *  the server's game board is represented by board, the server's client count is represented
     *  by players, and the endpoint of the communication channel with the client is represented by socket
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
     *  This handler represents a thread in the MinesweeperServer system.
     *  It does not initiate any threads, nor does it interfere with other threads
     *  as it is only used by MinesweeperServer to concurrently process clients.
     *  It shares board and players with other threads, but these fields are instance
     *  of threadsafe datatypes. (See Thread safety argument for MinesweeperServer)
     */
    
    /**
     * Creates a new MinesweeperClientHandler to handle the client at the other
     * end of the connection given access to by socket, with debug flag set by debug
     * and the Minesweeper board that is being updated by the communication with the
     * client.
     * 
     * @param socket the Socket at our end of the communication with the client
     * @param debug the flag denoting if debug mode is on
     * @param board the Minesweeper board being updated by the communication.
     */
    public MinesweeperClientHandler(Socket socket, boolean debug, Board board, AtomicInteger players) {
        this.socket = socket;
        this.debug = debug;
        this.board = board;
        this.players = players;
    }
    
    @SuppressWarnings("serial")
    // Represents client disconnection to exit run()
    private class ClientDisconnectException extends RuntimeException {}
    
    /**
     * Handle the single client connection. Terminate when client disconnects.
     * 
     * This method should not be directly called. Since this object is a Runnable,
     * this method is meant to executed concurrently by a call to its wrapping Thread
     * with start().
     * 
     * @param socket socket where the client is connected
     */
    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        ){  
            System.out.println("User Connected. " + players.get() + " player" + (players.get() == 1 ? "" : "s") + " connected.");
            
            out.println("Welcome to Minesweeper. Board: " + board.getWidth() + " columns by " + board.getHeight() + " rows." +
                        " Players: " + players.get() + " including you. Type 'help' for help.");
            
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                String output = handleRequest(line);
                if (!output.equals("BYE")) 
                    out.println(output);
                
                if (output.equals("BYE") || !debug && output.equals("BOOM!"))
                    throw new ClientDisconnectException();
            }
            
        } catch (ClientDisconnectException e) {
            // Intentional client disconnection.
            // Disconnection addressed in finally statement.
        } catch (Exception e) {
            e.printStackTrace();    // Unintentional client disconnection.
                                    // Disconnection addressed in finally statement.
        } finally {
            int playersLeft = players.decrementAndGet();
            System.out.println("User Disconnected. " + playersLeft + " player" + (playersLeft == 1 ? "" : "s") + " connected.");
        }
    }
    
    /**
     * Handler for client input, performing requested operations on board and returning an output message.
     * 
     * @param input message from client
     * @return message to client, or null if none
     */
    private String handleRequest(String input) {
        String regex = "(look)|(help)|(bye)|"
                     + "(dig -?\\d+ -?\\d+)|(flag -?\\d+ -?\\d+)|(deflag -?\\d+ -?\\d+)";
        if ( ! input.matches(regex)) {
            return "COMMANDS: look | dig x y | flag x y | deflag x y | help | bye";
        }
        String[] tokens = input.split(" ");
        if (tokens[0].equals("look")) {
            return board.toString();
            
        } else if (tokens[0].equals("help")) {
            return "COMMANDS: look | dig x y | flag x y | deflag x y | help | bye";
                    
        } else if (tokens[0].equals("bye")) {
            return "BYE";
            
        } else {
            int x = Integer.parseInt(tokens[1]);
            int y = Integer.parseInt(tokens[2]);
            if (tokens[0].equals("dig")) {
                boolean dead = board.dig(x, y);
                if (dead) return "BOOM!";
                
            } else if (tokens[0].equals("flag")) {
                board.flag(x, y);
                
            } else if (tokens[0].equals("deflag")) {
                board.deflag(x, y);
                
            }
            return board.toString();
        }

    }
    
}