package minesweeper.gui;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import minesweeper.server.MinesweeperServer;

/**
 * Represents a graphic user interface for managing a
 * Minesweeper server.
 */
@SuppressWarnings("serial")
public class MinesweeperServerFrame extends JFrame implements ActionListener, ChangeListener {
    
    /**
     * Creates a MinesweeperServerFrame.
     */
    public static void startMinesweeperServerFrameUI() {
        try {
            new MinesweeperServerFrame();
        } catch (UnknownHostException e) {
            System.err.println("Could not determine host IP address.");
        }
    }
    
    /**
     * Returns image object for the image file called filename (in media folder).
     * @param filename name of image file (in media folder)
     * @return image object corresponding to the read image file.
     */
    private static BufferedImage getPic(String filename) {
        try {
            return ImageIO.read(new File("media/" + filename));
        } catch (IOException e) {
            throw new RuntimeException("Missing " + filename + " file.");
        }
    }
    
    // =====================================================================================
    
    private static final Font HEADER_FONT = new Font("Tahoma", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Tahoma", Font.PLAIN, 14);
    private static final Font RESULT_FONT = new Font("Tahoma", Font.PLAIN, 13);
    
    private static final int DEFAULT_BOARD_SIZE = 10;
    private static final int MAX_BOARD_SIZE = 30;
    
    private static final BufferedImage EMPTY = getPic("empty.png");
    private static final BufferedImage MINE = getPic("mine.png");
    
    private final JTextField portField;
    private final JCheckBox kickCheckBox;
    private final JRadioButton randomBoardButton, fileBoardButton;
    private final JButton browseButton;
    private final JLabel fileLabel;
    private final JLabel rowsLabel, columnsLabel, rowNumLabel, columnNumLabel;
    private final JSlider rowSlider, columnSlider;
    private final JTextField playerCountField;
    private final JLabel playersLabel;
    
    private final JFileChooser fileChooser = new JFileChooser();
    private Optional<File> file = Optional.empty();
    
    private final JPanel panel;
    private final JButton runButton;
    private final JSeparator separator;
    private final JLabel boardLayoutLabel;
    
    private MinesweeperServer server;
    
    /**
     * Creates and initializes the Minesweeper Server GUI.
     * @throws UnknownHostException if unable to determine host IP address.
     */
    private MinesweeperServerFrame() throws UnknownHostException {
        setTitle("Minesweeper Server");
        setBounds(100, 100, 475, 210); 
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel contentPane = new JPanel();
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
        portField.setText(MinesweeperServer.DEFAULT_PORT + "");
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
        
        fileLabel = new JLabel();
        fileLabel.setFont(RESULT_FONT);
        fileLabel.setBounds(328, 78, 114, 25);
        fileLabel.setVisible(false);
        contentPane.add(fileLabel);
        
        playersLabel = new JLabel("Players Connected:");
        playersLabel.setFont(LABEL_FONT);
        playersLabel.setBounds(12, 108, 126, 16);
        playersLabel.setVisible(false);
        contentPane.add(playersLabel);
        
        playerCountField = new JTextField("0") {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                if (server != null) playerCountField.setText(server.playerCount() + "");
            }
        };
        playerCountField.setEditable(false);
        playerCountField.setBounds(135, 105, 64, 22);
        playerCountField.setVisible(false);
        contentPane.add(playerCountField);
        
        panel = new JPanel() {
            public void paint(Graphics g) {
                super.paint(g);
                drawBoardLayout((Graphics2D) g);
            }
        };
        panel.setBounds(12, 140, 452, 262);
        panel.setLayout(null);
        contentPane.add(panel);
        
        runButton = new JButton("Start");
        runButton.setBounds(348, 0, 97, 25);
        runButton.addActionListener(this);
        panel.add(runButton);
        
        separator = new JSeparator();
        separator.setBounds(0, 29, 445, 2);
        separator.setVisible(false);
        panel.add(separator);
        
        boardLayoutLabel = new JLabel("Board Layout:");
        boardLayoutLabel.setFont(LABEL_FONT);
        boardLayoutLabel.setBounds(0, 37, 93, 16);
        boardLayoutLabel.setVisible(false);
        panel.add(boardLayoutLabel);
        
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
       if (e.getSource() == randomBoardButton || e.getSource() == fileBoardButton) {
           setVisibleSizeOptionComponents(e.getSource() == randomBoardButton);
           setVisibleFileOptionComponents(e.getSource() == fileBoardButton);
           
           panel.setLocation(panel.getX(), e.getSource() == fileBoardButton ? 108 : 140);
           this.setSize(this.getWidth(), e.getSource() == fileBoardButton ? 175 : 210);
       }
       
       else if (e.getSource() == browseButton) {
           int status = fileChooser.showOpenDialog(this);
           if (status == JFileChooser.APPROVE_OPTION) {
               File file = fileChooser.getSelectedFile();
               fileLabel.setText(file.getName());
               this.file = Optional.of(file);
           }
       }
       
       else if (e.getSource() == runButton) {
           try {
               if (randomBoardButton.isSelected())
                   server = MinesweeperServer.runMinesweeperServer(kickCheckBox.isSelected(), Optional.empty(), columnSlider.getValue(), rowSlider.getValue(), Integer.parseInt(portField.getText()));
               else
                   server = MinesweeperServer.runMinesweeperServer(kickCheckBox.isSelected(), file, 0, 0, Integer.parseInt(portField.getText()));
               
               runButton.setText("Running...");
               disableInputComponents();
               setVisibleStartedComponents(true);
               
               int rows = server.getBoardHeight();
               int columns = server.getBoardWidth();
               
               int width = panel.getWidth() + 21*Math.max(columns-22,0);
               int height = panel.getHeight() + 20*Math.max(rows-10,0);
              
               panel.setSize(width, height);
               this.setSize(panel.getWidth()+20, panel.getHeight() + (fileBoardButton.isSelected() ? 155 : 190));
               // see drawBoardLayout()
               
           } catch (Exception e1) {
               server = null;
           }
       }
    }
    
    /**
     * Draws the board layout, the distribution of mines on the running Minesweeper server's board.
     * @param g The graphics context to be used for drawing
     */
    private void drawBoardLayout(Graphics2D g) {
        if (server == null) return;
        
        g.translate(0, 60);
        for (int y = 0; y < server.getBoardHeight(); y++)
            for (int x = 0; x < server.getBoardWidth(); x++) {
                BufferedImage image = (server.boardHasMine(x,y)) ? MINE : EMPTY;
                g.drawImage(image,image.getWidth()*x,image.getHeight()*y,null);
            }
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == rowSlider)
            rowNumLabel.setText(rowSlider.getValue() + "");
        else if (e.getSource() == columnSlider)
            columnNumLabel.setText(columnSlider.getValue() + "");
    }
    
    /**
     * Sets the visibility of the board sizing components (rows & column labels and sliders)
     * @param visible true to make the components visible; false to make invisible.
     */
    private void setVisibleSizeOptionComponents(boolean visible) {
        rowsLabel.setVisible(visible);
        rowSlider.setVisible(visible);
        rowNumLabel.setVisible(visible);
        columnsLabel.setVisible(visible);
        columnSlider.setVisible(visible);
        columnNumLabel.setVisible(visible);
    }
    
    /**
     * Sets the visibility of the file selection components (file label & file browse button)
     * @param visible true to make the components visibile; false the make invisible.
     */
    private void setVisibleFileOptionComponents(boolean visible) {
        browseButton.setVisible(visible);
        fileLabel.setVisible(visible);
    }
    
    /**
     * Sets the visibility of the components to be visible after the server has initiated.
     * @param visible true to make the components visibile; false the make invisible.
     */
    private void setVisibleStartedComponents(boolean visible) {
        playersLabel.setVisible(visible);
        playerCountField.setVisible(visible);
        separator.setVisible(visible);
        boardLayoutLabel.setVisible(visible);
    }
    
    /**
     * Disables the input components used to initiate the Minesweeper server.
     */
    private void disableInputComponents() {
        runButton.setEnabled(false);
        runButton.setFocusable(false);
        
        portField.setEditable(false);
        kickCheckBox.setEnabled(false);
        randomBoardButton.setEnabled(false);
        fileBoardButton.setEnabled(false);
        rowSlider.setEnabled(false);
        columnSlider.setEnabled(false);
        browseButton.setEnabled(false);
    }
}