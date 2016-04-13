package minesweeper.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import minesweeper.Board;

/**
 * Represents a handler for a single Minesweeper Server client.
 */
public class MinesweeperClientHandler implements Runnable {

    private final Socket socket;
    private final Board board;
    private final boolean debug;
    
    /*
     *  Abstraction Function
     *  ====================
     *  Represents a handler for a multiplayer Minesweeper Server game client, where
     *  the server's game board is represented by board, and the endpoint of the communication
     *  channel with the client is represented by socket
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
    
    @SuppressWarnings("serial")
    private class ClientDisconnectException extends RuntimeException {}
    
    public MinesweeperClientHandler(Socket socket, boolean debug, Board board) {
        this.socket = socket;
        this.debug = debug;
        this.board = board;
    }
    
    /**
     * Handle the single client connection. Terminate when client disconnects.
     * 
     * @param socket socket where the client is connected
     */
    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        ){
            out.println("Welcome to Minesweeper. Board: " + board.getWidth() + " columns by " + board.getHeight() + " rows." +
                        " Players: " + (Thread.activeCount()-1) + " including you. Type 'help' for help.");
            
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                String output = handleRequest(line);
                out.println(output);
                if (output.equals("BOOM!") || !debug && output.equals("BYE"))
                    throw new ClientDisconnectException();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClientDisconnectException e) {
            System.out.println("User Disconnected.");
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