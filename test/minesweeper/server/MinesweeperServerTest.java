/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper.server;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;

import main.Main;

/**
 * Tests for single client use of MinesweeperServer.
 * 
 * Server and connection code used from 6.005 published test.
 */
public class MinesweeperServerTest {
    
    private static final String LOCALHOST = "127.0.0.1";
    private static final int PORT = 1234;
    
    private static final int MAX_CONNECTION_ATTEMPTS = 10;
    
    private static final String BOARDS_FOLDER = "boards/";
    
    /**
     * Start a MinesweeperServer in debug mode with a board file from BOARDS_FOLDER.
     * @param boardFile board to load
     * @return thread running the server
     * @throws IOException if the board file cannot be found
     */
    private static Thread startMinesweeperServer(String boardFile) throws IOException {
        final String boardPath = new File(BOARDS_FOLDER + boardFile).getAbsolutePath();
        
        final String[] args = new String[] {
                "--debug",
                "--port", Integer.toString(PORT),
                "--file", boardPath
        };
        
        Thread serverThread = new Thread(() -> Main.main(args));
        serverThread.start();
        return serverThread;
    }
    
    /**
     * Connect to a MinesweeperServer and return the connected socket.
     * @param server abort connection attempts if the server thread dies
     * @return socket connected to the server
     * @throws IOException if the connection fails
     */
    private static Socket connectToMinesweeperServer(Thread server) throws IOException {
        int attempts = 0;
        while (true) {
            try {
                Socket socket = new Socket(LOCALHOST, PORT);
                socket.setSoTimeout(3000);
                return socket;
            
            } catch (ConnectException ce) {
                if (!server.isAlive())
                    throw new IOException("Server thread not running");
                
                if (++attempts > MAX_CONNECTION_ATTEMPTS)
                    throw new IOException("Exceeded max connection attempts", ce);
                
                try { 
                    Thread.sleep(attempts * 10); 
                } catch (InterruptedException ie) { }
            }
        }
    }
    
    @Test(timeout = 10000)
    public void mediumBoardTest() throws IOException {

        Thread thread = startMinesweeperServer("mediumBoard.txt");

        Socket socket = connectToMinesweeperServer(thread);
        
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        assertTrue("expected HELLO message", in.readLine().startsWith("Welcome"));

        out.println("look");
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());

        out.println("dig 3 1");
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - 4 - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());

        out.println("dig 2 2");
        assertEquals("BOOM!", in.readLine());   // debug mode is on - continue

        out.println("look"); 
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - 3 - - - - - - - -", in.readLine());
        assertEquals("- - 2 - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        
        out.println("flag 2 0");
        assertEquals("- - F - - - - - - - - -", in.readLine());
        assertEquals("- - - 3 - - - - - - - -", in.readLine());
        assertEquals("- - 2 - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        
        out.println("flag 3 0");
        assertEquals("- - F F - - - - - - - -", in.readLine());
        assertEquals("- - - 3 - - - - - - - -", in.readLine());
        assertEquals("- - 2 - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        
        out.println("flag 4 0");
        assertEquals("- - F F F - - - - - - -", in.readLine());
        assertEquals("- - - 3 - - - - - - - -", in.readLine());
        assertEquals("- - 2 - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        
        out.println("dig 4 0");
        assertEquals("- - F F F - - - - - - -", in.readLine());
        assertEquals("- - - 3 - - - - - - - -", in.readLine());
        assertEquals("- - 2 - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());

        out.println("deflag 4 0");
        assertEquals("- - F F - - - - - - - -", in.readLine());
        assertEquals("- - - 3 - - - - - - - -", in.readLine());
        assertEquals("- - 2 - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        
        out.println("dig 2 9");
        assertEquals("- - F F - - - - - - - -", in.readLine());
        assertEquals("- - - 3 - - - - - - - -", in.readLine());
        assertEquals("- - 2 - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- - - - - - - - - - - -", in.readLine());
        assertEquals("- 2 1 3 - - - - - - - -", in.readLine());
        assertEquals("- 1   2 - - - - - - - -", in.readLine());
        
        out.println("bye");
        socket.close();
    }
}