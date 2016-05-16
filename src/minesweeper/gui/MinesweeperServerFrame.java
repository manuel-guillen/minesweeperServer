package minesweeper.gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Inet4Address;
import java.net.UnknownHostException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JSeparator;

/**
 * Represents a graphic user interface for managing a
 * Minesweeper server.
 */
@SuppressWarnings("serial")
public class MinesweeperServerFrame extends JFrame implements ActionListener, ChangeListener {
    
    private static final Font HEADER_FONT = new Font("Tahoma", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Tahoma", Font.PLAIN, 14);
    private static final Font RESULT_FONT = new Font("Tahoma", Font.PLAIN, 13);
    
    private static final int DEFAULT_BOARD_SIZE = 10;
    private static final int MAX_BOARD_SIZE = 30;
    
    private final JPanel contentPane;
    private final JTextField portField;
    private final JCheckBox kickCheckBox;
    private final JRadioButton randomBoardButton, fileBoardButton;
    private final JButton browseButton;
    private final JLabel fileLabel;
    private final JLabel rowsLabel, columnsLabel, rowNumLabel, columnNumLabel;
    private final JSlider rowSlider, columnSlider;
    private final JTextField playerCountField;
    private final JLabel playersLabel;
    
    private final JPanel panel;
    private final JButton runButton;
    
    /**
     * Create the Minesweeper Server GUI.
     * @throws UnknownHostException 
     */
    public MinesweeperServerFrame() throws UnknownHostException {
        setTitle("Minesweeper Server");
        setBounds(100, 100, 475, 300);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(null);
        setContentPane(contentPane);
        
        JLabel headerLabel = new JLabel("Minesweeper Server Information");
        headerLabel.setBounds(12, 13, 305, 22);
        headerLabel.setFont(HEADER_FONT);
        contentPane.add(headerLabel);
        
        JLabel addressLabel = new JLabel("IP Address:");
        addressLabel.setFont(LABEL_FONT);
        addressLabel.setBounds(12, 50, 74, 16);
        contentPane.add(addressLabel);
        
        JLabel portLabel = new JLabel("Port:");
        portLabel.setFont(LABEL_FONT);
        portLabel.setBounds(221, 50, 36, 16);
        contentPane.add(portLabel);
        
        JLabel boardLabel = new JLabel("Board:");
        boardLabel.setFont(LABEL_FONT);
        boardLabel.setBounds(12, 81, 47, 16);
        contentPane.add(boardLabel);
        
        JTextField addressField = new JTextField();
        addressField.setEditable(false);
        addressField.setText(Inet4Address.getLocalHost().getHostAddress().toString());
        addressField.setBounds(86, 48, 113, 22);
        contentPane.add(addressField);
        
        portField = new JTextField();
        portField.setText("4444");
        portField.setBounds(256, 48, 54, 22);
        contentPane.add(portField);
        
        kickCheckBox = new JCheckBox("Kick Upon Loss:");
        kickCheckBox.setFocusable(false);
        kickCheckBox.setSelected(true);
        kickCheckBox.setFont(LABEL_FONT);
        kickCheckBox.setHorizontalTextPosition(SwingConstants.LEADING);
        kickCheckBox.setBounds(338, 46, 126, 25);
        contentPane.add(kickCheckBox);
        
        ButtonGroup buttonGroup = new ButtonGroup();
        
        randomBoardButton = new JRadioButton("Random");
        randomBoardButton.setSelected(true);
        randomBoardButton.setBounds(67, 78, 81, 25);
        randomBoardButton.addActionListener(this);
        buttonGroup.add(randomBoardButton);
        contentPane.add(randomBoardButton);
        
        fileBoardButton = new JRadioButton("File");
        fileBoardButton.setBounds(155, 78, 54, 25);
        fileBoardButton.addActionListener(this);
        buttonGroup.add(fileBoardButton);
        contentPane.add(fileBoardButton);
        
        rowsLabel = new JLabel("Rows:");
        rowsLabel.setFont(LABEL_FONT);
        rowsLabel.setBounds(221, 81, 47, 16);
        contentPane.add(rowsLabel);
        
        columnsLabel = new JLabel("Columns:");
        columnsLabel.setFont(LABEL_FONT);
        columnsLabel.setBounds(221, 107, 64, 16);
        contentPane.add(columnsLabel);
        
        rowSlider = new JSlider(1,MAX_BOARD_SIZE,DEFAULT_BOARD_SIZE);
        rowSlider.setBounds(284, 78, 158, 26);
        rowSlider.setFocusable(false);
        rowSlider.setMajorTickSpacing(10);
        rowSlider.setMinorTickSpacing(2);
        rowSlider.setPaintTicks(true);
        rowSlider.addChangeListener(this);
        contentPane.add(rowSlider);
        
        columnSlider = new JSlider(1,MAX_BOARD_SIZE,DEFAULT_BOARD_SIZE);
        columnSlider.setBounds(284, 105, 158, 26);
        columnSlider.setFocusable(false);
        columnSlider.setMajorTickSpacing(10);
        columnSlider.setMinorTickSpacing(2);
        columnSlider.setPaintTicks(true);
        columnSlider.addChangeListener(this);
        contentPane.add(columnSlider);
        
        rowNumLabel = new JLabel(DEFAULT_BOARD_SIZE + "");
        rowNumLabel.setFont(RESULT_FONT);
        rowNumLabel.setBounds(445, 83, 22, 16);
        contentPane.add(rowNumLabel);
        
        columnNumLabel = new JLabel(DEFAULT_BOARD_SIZE + "");
        columnNumLabel.setFont(RESULT_FONT);
        columnNumLabel.setBounds(445, 110, 22, 16);
        contentPane.add(columnNumLabel);
        
        browseButton = new JButton("Browse...");
        browseButton.setBounds(220, 78, 97, 25);
        browseButton.addActionListener(this);
        browseButton.setVisible(false);
        contentPane.add(browseButton);
        
        fileLabel = new JLabel("default.txt");
        fileLabel.setFont(RESULT_FONT);
        fileLabel.setBounds(328, 78, 114, 25);
        fileLabel.setVisible(false);
        contentPane.add(fileLabel);
        
        playersLabel = new JLabel("Players Connected:");
        playersLabel.setFont(LABEL_FONT);
        playersLabel.setBounds(12, 108, 126, 16);
        playersLabel.setVisible(false);
        contentPane.add(playersLabel);
        
        playerCountField = new JTextField("0");
        playerCountField.setEditable(false);
        playerCountField.setBounds(135, 105, 64, 22);
        playerCountField.setVisible(false);
        contentPane.add(playerCountField);
        
        panel = new JPanel();
        panel.setBounds(12, 140, 445, 108);
        panel.setLayout(null);
        contentPane.add(panel);
        
        JSeparator separator = new JSeparator();
        separator.setBounds(0, 29, 445, 2);
        panel.add(separator);
        
        runButton = new JButton("Start");
        runButton.setBounds(348, 0, 97, 25);
        runButton.addActionListener(this);
        panel.add(runButton);
        
        JLabel boardLayoutLabel = new JLabel("Board Layout:");
        boardLayoutLabel.setFont(LABEL_FONT);
        boardLayoutLabel.setBounds(0, 37, 93, 16);
        panel.add(boardLayoutLabel);
        
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
       if (e.getSource() == randomBoardButton || e.getSource() == fileBoardButton) {
           rowsLabel.setVisible(e.getSource() == randomBoardButton);
           rowSlider.setVisible(e.getSource() == randomBoardButton);
           rowNumLabel.setVisible(e.getSource() == randomBoardButton);
           columnsLabel.setVisible(e.getSource() == randomBoardButton);
           columnSlider.setVisible(e.getSource() == randomBoardButton);
           columnNumLabel.setVisible(e.getSource() == randomBoardButton);
           
           browseButton.setVisible(e.getSource() == fileBoardButton);
           fileLabel.setVisible(e.getSource() == fileBoardButton);
           
           panel.setLocation(12, e.getSource() == fileBoardButton ? 108 : 140);
       } 
       
       else if (e.getSource() == runButton) {
           runButton.setText("Running...");
           runButton.setEnabled(false);
           runButton.setFocusable(false);
           
           portField.setEditable(false);
           
           kickCheckBox.setEnabled(false);
           randomBoardButton.setEnabled(false);
           fileBoardButton.setEnabled(false);
           rowSlider.setEnabled(false);
           columnSlider.setEnabled(false);
           browseButton.setEnabled(false);
           
           playersLabel.setVisible(true);
           playerCountField.setVisible(true);
       }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == rowSlider)
            rowNumLabel.setText(rowSlider.getValue() + "");
        else if (e.getSource() == columnSlider)
            columnNumLabel.setText(columnSlider.getValue() + "");
    }
    
    public static void main(String[] args) throws UnknownHostException {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e){
            // Use default look and feel.
        }
        
        new MinesweeperServerFrame();
    }
}
