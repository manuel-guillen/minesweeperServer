package minesweeper.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Represents a graphic user interface for managing a
 * Minesweeper server.
 */
@SuppressWarnings("serial")
public class MinesweeperServerFrame extends JFrame {

    private JPanel contentPane;

    /**
     * Create the Minesweeper Server GUI.
     */
    public MinesweeperServerFrame() {
        setTitle("Minesweeper Server");
        setBounds(100, 100, 450, 300);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        
        setVisible(true);
    }
}
