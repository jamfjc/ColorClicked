package colorclicker;

import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.*;

public class ColorClickerGUI {

    private JFrame frame;
    private BoardGUI boardGUI;
    private int currentBoardSize;
    private boolean colorBlindMode = false;

    private final int INITIAL_BOARD_SIZE = 10;

    public ColorClickerGUI() {
        currentBoardSize = INITIAL_BOARD_SIZE;
        frame = new JFrame("Color Clicker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        boardGUI = new BoardGUI(currentBoardSize);
        frame.getContentPane().add(boardGUI.getBoardPanel(), BorderLayout.CENTER);
        frame.getContentPane().add(boardGUI.getTimeLabel(), BorderLayout.SOUTH);

        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        // Game menu
        JMenu gameMenu = new JMenu("Game");
        menuBar.add(gameMenu);

        JMenu newMenu = new JMenu("New");
        gameMenu.add(newMenu);
        for (int boardSize : new int[]{5, 10, 15, 20}) {
            JMenuItem item = new JMenuItem(boardSize + "x" + boardSize);
            newMenu.add(item);
            item.addActionListener(e -> newGame(boardSize));
        }

        JMenuItem highScores = new JMenuItem("High Scores");
        gameMenu.add(highScores);
        highScores.addActionListener(e ->
            JOptionPane.showMessageDialog(frame,
                HighScoreManager.getDisplay(currentBoardSize),
                "High Scores", JOptionPane.INFORMATION_MESSAGE));

        gameMenu.addSeparator();
        JMenuItem exitItem = new JMenuItem("Exit");
        gameMenu.add(exitItem);
        exitItem.addActionListener(e -> System.exit(0));

        // Accessibility menu
        JMenu accessMenu = new JMenu("Accessibility");
        menuBar.add(accessMenu);
        JCheckBoxMenuItem cbMode = new JCheckBoxMenuItem("Color Blindness Mode");
        accessMenu.add(cbMode);
        cbMode.addActionListener(e -> {
            colorBlindMode = cbMode.isSelected();
            boardGUI.setColorBlindMode(colorBlindMode);
        });

        frame.pack();
        frame.setVisible(true);
    }

    private void newGame(int boardSize) {
        currentBoardSize = boardSize;
        frame.getContentPane().remove(boardGUI.getBoardPanel());
        frame.getContentPane().remove(boardGUI.getTimeLabel());
        boardGUI = new BoardGUI(boardSize);
        boardGUI.setColorBlindMode(colorBlindMode);
        frame.getContentPane().add(boardGUI.getBoardPanel(), BorderLayout.CENTER);
        frame.getContentPane().add(boardGUI.getTimeLabel(), BorderLayout.SOUTH);
        frame.pack();
        frame.revalidate();
        frame.repaint();
    }
}
