package minesweeper.gui;

import java.awt.Font;
import java.net.Inet4Address;
import java.net.UnknownHostException;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Represents a graphic user interface for managing a
 * Minesweeper server.
 */
@SuppressWarnings("serial")
public class MinesweeperServerFrame extends JFrame {

    private JPanel contentPane;
    private JTextField addressField;
    private JTextField portField;
    private JCheckBox kickCheckBox;
    private JRadioButton randomBoardButton;
    private JRadioButton fileBoardButton;

    /**
     * Create the Minesweeper Server GUI.
     * @throws UnknownHostException 
     */
    public MinesweeperServerFrame() throws UnknownHostException {
        setTitle("Minesweeper Server");
        setBounds(100, 100, 460, 300);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(null);
        setContentPane(contentPane);
        
        JLabel headerLabel = new JLabel("Minesweeper Server Information");
        headerLabel.setBounds(12, 13, 305, 22);
        headerLabel.setFont(new Font("Tahoma", Font.BOLD, 18));
        contentPane.add(headerLabel);
        
        JLabel addressLabel = new JLabel("IP Address:");
        addressLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
        addressLabel.setBounds(12, 50, 74, 16);
        contentPane.add(addressLabel);
        
        JLabel portLabel = new JLabel("Port:");
        portLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
        portLabel.setBounds(221, 50, 36, 16);
        contentPane.add(portLabel);
        
        JLabel boardLabel = new JLabel("Board:");
        boardLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
        boardLabel.setBounds(12, 79, 47, 16);
        contentPane.add(boardLabel);
        
        addressField = new JTextField();
        addressField.setEditable(false);
        addressField.setText(Inet4Address.getLocalHost().getHostAddress().toString());
        addressField.setBounds(86, 48, 123, 22);
        contentPane.add(addressField);
        
        portField = new JTextField();
        portField.setText("4444");
        portField.setBounds(256, 48, 54, 22);
        contentPane.add(portField);
        
        kickCheckBox = new JCheckBox("Kick Upon Loss:");
        kickCheckBox.setFocusable(false);
        kickCheckBox.setSelected(true);
        kickCheckBox.setFont(new Font("Tahoma", Font.PLAIN, 14));
        kickCheckBox.setHorizontalTextPosition(SwingConstants.LEADING);
        kickCheckBox.setBounds(322, 46, 131, 25);
        contentPane.add(kickCheckBox);
        
        ButtonGroup buttonGroup = new ButtonGroup();
        
        randomBoardButton = new JRadioButton("Random");
        randomBoardButton.setSelected(true);
        randomBoardButton.setBounds(67, 76, 81, 25);
        buttonGroup.add(randomBoardButton);
        contentPane.add(randomBoardButton);
        
        fileBoardButton = new JRadioButton("File");
        fileBoardButton.setBounds(155, 76, 54, 25);
        buttonGroup.add(fileBoardButton);
        contentPane.add(fileBoardButton);
        
        setVisible(true);
    }
    
    public static void main(String[] args) throws UnknownHostException {
        new MinesweeperServerFrame();
    }
}
