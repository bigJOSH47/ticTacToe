import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class tictactoe extends JFrame implements ActionListener {

    // Constants for the players
    private static final int COMPUTER = 1;
    private static final int PLAYER = -1;

    // Random number generator to decide the first player
    Random random = new Random();

    // GUI Components
    JPanel panel = new JPanel();           // Panel for the title text
    JPanel button_p = new JPanel();        // Panel for the game buttons (3x3 grid)
    JLabel text = new JLabel();            // Label to display game status or messages
    JButton[] button = new JButton[9];     // Array of buttons representing the Tic-Tac-Toe grid
    JLabel scoreXLabel = new JLabel("X: 0"); // Player X Score
    JLabel scoreOLabel = new JLabel("O: 0"); // Player O Score
    int xScore = 0, oScore = 0;            // Score variables
    boolean player_turn = true;  // true if it's Player X's turn, false if it's Player O's turn
    boolean playerVsComputer = false;      // Game mode flag (Player vs Computer or Player vs Player)

    // Sound-related fields
    Clip placeSound, winSound, drawSound;

    // Game state
    int[] board = new int[9];  // 1 for X, -1 for O, 0 for empty
    boolean gameOver = false;   // Flag to indicate if the game is over

    // Constructor to initialize the game window and GUI components
    public tictactoe() {
        // Setting up the main window
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Close operation
        this.setSize(900, 900);                               // Window size
        this.getContentPane().setBackground(new Color(0, 0, 0));  // Background color for a dark theme
        this.setLayout(new BorderLayout());                   // Layout manager
        this.setLocationRelativeTo(null);                     // Center the window on the screen
        this.setVisible(true);                                // Make the window visible

        // Load sound files
        loadSounds();

        // Setting up the title text label
        text.setBackground(new Color(255, 255, 255));               // Background color for label
        text.setForeground(new Color(0, 0, 255));         // Text color
        text.setFont(new Font("Arial", Font.BOLD, 75));     // Font settings
        text.setHorizontalTextPosition(JLabel.CENTER);        // Center the text
        text.setText("TIC TAC TOE");                          // Initial text
        text.setOpaque(true);                                 // Make the label opaque to see background

        // Adding the title label to the title panel
        panel.add(text);

        // Score panel
        JPanel scorePanel = new JPanel();
        scorePanel.setLayout(new GridLayout(1, 2));
        scorePanel.add(scoreXLabel);
        scorePanel.add(scoreOLabel);
        scorePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        // Adding the panel (with text) to the main frame at the top (NORTH)
        this.add(panel, BorderLayout.NORTH);
        this.add(scorePanel, BorderLayout.SOUTH);

        // Setting up the grid panel (3x3) for the buttons
        button_p.setLayout(new GridLayout(3, 3));             // Grid layout for Tic-Tac-Toe buttons
        panel.setBounds(0, 0, 800, 100);                      // Set the bounds for the panel

        // Adding the button panel to the main frame (CENTER)
        this.add(button_p, BorderLayout.CENTER);

        // Initialize buttons with custom designs and add them to the grid panel
        for (int i = 0; i < 9; i++) {
            button[i] = new JButton();                        // Create new button
            button_p.add(button[i]);                          // Add button to grid panel
            button[i].setFont(new Font("Arial", Font.BOLD, 120));  // Font settings for button text
            button[i].setFocusable(false);                    // Disable focus
            button[i].setBackground(new Color(255, 255, 255)); // Button background color
            button[i].setBorder(new LineBorder(Color.BLACK, 2));  // Add border
            button[i].addActionListener(this);                // Add action listener to the button
            button[i].addMouseListener(new MouseAdapter(){
                // Hover effect: change color when hovering over button
                @Override
                public void mouseEntered(MouseEvent e) {
                    JButton source = (JButton) e.getSource();
                    source.setBackground(new Color(200, 200, 255));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    JButton source = (JButton) e.getSource();
                    source.setBackground(new Color(255, 255, 255));
                }
            });
        }

        // Create menu bar
        createMenuBar();

        // Initialize the game
        firstTurn();
    }

    // Method to create the menu bar
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Create 'Game' menu
        JMenu gameMenu = new JMenu("Game");

        // New Game item
        JMenuItem newGameItem = new JMenuItem("New Game");
        newGameItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetGame();
            }
        });

        // Mode item for selecting between Player vs Player and Player vs Computer
        JMenu modeMenu = new JMenu("Mode");


        JMenuItem playerVsPlayerItem = new JMenuItem("Player vs Player");
        playerVsPlayerItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                playerVsComputer = false; // Set mode to Player vs Player
                resetGame();
            }
        });

        JMenuItem playerVsComputerItem = new JMenuItem("Player vs Computer");
        playerVsComputerItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                playerVsComputer = true; // Set mode to Player vs Computer
                resetGame();
            }
        });


        modeMenu.add(playerVsPlayerItem);
        modeMenu.add(playerVsComputerItem);

        // Exit item
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(modeMenu);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);

        // Create 'Help' menu
        JMenu helpMenu = new JMenu("Help");

        // Instructions item
        JMenuItem instructionsItem = new JMenuItem("Instructions");
        instructionsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showInstructions();
            }
        });

        helpMenu.add(instructionsItem);

        // Add menus to the menu bar
        menuBar.add(gameMenu);
        menuBar.add(helpMenu);

        // Set the menu bar for the frame
        this.setJMenuBar(menuBar);
    }

    // Method to display instructions
    private void showInstructions() {
        String instructions = """
                Tic-Tac-Toe Game Instructions:
                
                1. The game is played on a 3x3 grid.
                2. Player 1 uses 'X' and Player 2 uses 'O'.
                3. Players take turns to place their marks on the grid.
                4. The first player to get three of their marks in a row wins.
                5. If the grid is full and no one has won, the game is a draw.
                6. To start a new game, select 'New Game' from the Game menu.""";
        JOptionPane.showMessageDialog(this, instructions, "Instructions", JOptionPane.INFORMATION_MESSAGE);
    }

    // Method to randomly decide which player starts first
    public void firstTurn() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (random.nextInt(2) == 0) {
            player_turn = true;  // Player X starts
            text.setText("PLAYER X's TURN");
        } else {
            player_turn = false;  // Player O starts
            text.setText("PLAYER O's TURN");
            if (playerVsComputer) {
                computerMove();
            }
        }
    }

    // Load sound files for different actions
    private void loadSounds() {
        try {
            placeSound = AudioSystem.getClip();
            placeSound.open(AudioSystem.getAudioInputStream(new File("src/intelliJ files/Audio/place_sound.wav")));

            winSound = AudioSystem.getClip();
            winSound.open(AudioSystem.getAudioInputStream(new File("src/intelliJ files/Audio/win_sound.wav")));

            drawSound = AudioSystem.getClip();
            drawSound.open(AudioSystem.getAudioInputStream(new File("src/intelliJ files/Audio/draw_sound.wav")));
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    // Play the sound for placing a move
    private void playPlaceSound() {
        placeSound.setFramePosition(0);
        placeSound.start();
    }

    // Play the sound for winning
    private void playWinSound() {
        winSound.setFramePosition(0);
        winSound.start();
    }

    // Play the sound for a draw
    private void playDrawSound() {
        drawSound.setFramePosition(0);
        drawSound.start();
    }

    // Disable all buttons after a win or draw
    public void disableButtons() {
        for (int i = 0; i < 9; i++) {
            button[i].setEnabled(false);
        }
    }

    // Action listener for button clicks
    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;

        // Check which button was clicked
        JButton clickedButton = (JButton) e.getSource();
        int index = -1;
        for (int i = 0; i < 9; i++) {
            if (clickedButton == button[i]) {
                index = i;
                break;
            }
        }

        // If the cell is already taken, ignore the click
        if (board[index] != 0) return;

        // Mark the board and update the button
        board[index] = player_turn ? PLAYER : COMPUTER;
        if (player_turn) {
            clickedButton.setText("X");
            clickedButton.setForeground(new Color(255, 0, 0)); // Set color for X (Red)
        } else {
            clickedButton.setText("O");
            clickedButton.setForeground(new Color(0, 0, 255)); // Set color for O (Blue)
        }

        // Play the place sound
        playPlaceSound();

        // Immediately repaint and revalidate the UI to reflect the player's move
        this.repaint();
        this.revalidate();

        // Check if someone has won
        if (checkWinner()) {
            gameOver = true;
            text.setText(player_turn ? "PLAYER X WINS!" : "PLAYER O WINS!");
            playWinSound();
            updateScore();
            disableButtons();
            JOptionPane.showMessageDialog(this, player_turn ? "PLAYER X WINS!, Starting a new round" : "PLAYER O WINS!, Starting a new round");
            resetGame();
            return; //Stop further execution
        }

        // Check for draw
        if (checkDraw()) {
            gameOver = true;
            text.setText("DRAW!");
            playDrawSound();
            disableButtons();
            JOptionPane.showMessageDialog(this, "It's a DRAW, Starting a new round");
            resetGame();
            return; //Stop further execution
        }

        // Switch turns
        player_turn = !player_turn;
        text.setText(player_turn ? "PLAYER X's TURN" : "PLAYER O's TURN");

        // If Player vs Computer mode, make the computer move
        if (playerVsComputer && !player_turn) {
            // After the player's move has been rendered, make the computer move
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    // Wait for a short delay, then make the computer move
                    computerMove();
                }
            });

        }
    }

    // Check if there is a winner
    private boolean checkWinner() {
        // Winning combinations
        int[][] winPatterns = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // rows
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // columns
                {0, 4, 8}, {2, 4, 6}              // diagonals
        };

        for (int[] pattern : winPatterns) {
            if (board[pattern[0]] != 0 && board[pattern[0]] == board[pattern[1]] && board[pattern[0]] == board[pattern[2]]) {
                return true;
            }
        }
        return false;
    }

    // Check if the game is a draw
    private boolean checkDraw() {
        for (int i = 0; i < 9; i++) {
            if (board[i] == 0) return false; // There's still an empty cell
        }
        return true; // All cells are filled, and no winner
    }

    // Update the score
    private void updateScore() {
        if (player_turn) {
            xScore++;
            scoreXLabel.setText("X: " + xScore);
        } else {
            oScore++;
            scoreOLabel.setText("O: " + oScore);
        }
    }

    // Reset the game
    private void resetGame() {
        gameOver = false;
        for (int i = 0; i < 9; i++) {
            board[i] = 0;
            button[i].setText("");
            button[i].setEnabled(true);
        }
        firstTurn();
    }

    // Simple computer move (random move)
    private void computerMove() {
        // Add delay for smoother GUI experience
        try {
            Thread.sleep(1000); // Delay of 2 second (1000 milliseconds)
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        // Find all empty positions
        int[] emptyPositions = new int[9];
        int emptyCount = 0;
        for (int i = 0; i < 9; i++) {
            if (board[i] == 0) {
                emptyPositions[emptyCount++] = i;
            }
        }



        // Pick a random empty position
        int move = emptyPositions[random.nextInt(emptyCount)];
        board[move] = COMPUTER;
        button[move].setText("O");
        button[move].setForeground(new Color(0,0,255));
        playPlaceSound();

        // Check if the computer has won
        if (checkWinner()) {
            gameOver = true;
            text.setText("PLAYER O WINS!");
            playWinSound();
            updateScore();
        } else if (checkDraw()) {
            gameOver = true;
            text.setText("DRAW!");
            playDrawSound();
        } else {
            // Switch to Player X's turn
            player_turn = true;
            text.setText("PLAYER X's TURN");
        }
    }
}
