package colorclicker;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.Timer;

public class BoardGUI {

    private JButton[][] buttons;
    private Board board;
    private JPanel boardPanel;
    private JLabel timeLabel;
    private ArrayList<Point> points;

    private Random random = new Random();
    private int clickNum = 0;
    private long startTime;
    private Timer timer;
    private Color nextColor;
    private boolean colorBlindMode = false;
    private int boardSize;

    private static final String[] SHAPES = {"●", "■", "▲", "★", "◆", "✦", "⬟", "⬡"};
    private final int NUM_COLORED_FIELDS = 4;

    public BoardGUI(int boardSize) {
        this.boardSize = boardSize;
        board = new Board(boardSize);
        boardPanel = new JPanel();
        points = new ArrayList<>();
        boardPanel.setLayout(new GridLayout(boardSize, boardSize));
        buttons = new JButton[boardSize][boardSize];
        nextColor = randomColor();

        for (int i = 0; i < boardSize; ++i) {
            for (int j = 0; j < boardSize; ++j) {
                JButton button = new JButton();
                button.setOpaque(true);
                button.setBorderPainted(false);
                button.setPreferredSize(new Dimension(80, 40));
                button.setFont(new Font("Dialog", Font.BOLD, 14));
                final int fi = i, fj = j;
                button.addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) {
                        if (board.get(fi, fj).getColor() == null)
                            buttons[fi][fj].setBackground(nextColor.brighter());
                    }
                    @Override public void mouseExited(MouseEvent e) {
                        if (board.get(fi, fj).getColor() == null)
                            buttons[fi][fj].setBackground(null);
                    }
                });
                button.addActionListener(new ButtonListener(i, j));
                buttons[i][j] = button;
                boardPanel.add(button);
                points.add(new Point(i, j));
            }
        }
        Collections.shuffle(points);

        timeLabel = new JLabel(" ");
        timeLabel.setHorizontalAlignment(JLabel.RIGHT);
        timer = new Timer(10, e -> timeLabel.setText(elapsedTime() + " ms"));
        startTime = System.currentTimeMillis();
        timer.start();
    }

    private Color randomColor() {
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    public long elapsedTime() {
        return System.currentTimeMillis() - startTime;
    }

    public void setColorBlindMode(boolean enabled) {
        this.colorBlindMode = enabled;
        for (int i = 0; i < boardSize; i++)
            for (int j = 0; j < boardSize; j++)
                refresh(i, j);
    }

    public void refresh(int x, int y) {
        JButton button = buttons[x][y];
        Field field = board.get(x, y);
        button.setBackground(field.getColor());
        if (field.getColor() != null) {
            int num = field.getNumber();
            String shape = colorBlindMode ? SHAPES[num % SHAPES.length] + " " : "";
            button.setText(shape + num);
            button.setForeground(contrastColor(field.getColor()));
        } else {
            button.setText("");
        }
    }

    private Color contrastColor(Color bg) {
        double lum = 0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue();
        return lum > 128 ? Color.BLACK : Color.WHITE;
    }

    public JPanel getBoardPanel() { return boardPanel; }
    public JLabel getTimeLabel() { return timeLabel; }

    private void playTone(int hz, int ms) {
        new Thread(() -> {
            try {
                AudioFormat af = new AudioFormat(44100, 8, 1, true, false);
                SourceDataLine line = AudioSystem.getSourceDataLine(af);
                line.open(af, 4096);
                line.start();
                byte[] buf = new byte[(int)(44100 * ms / 1000.0)];
                for (int i = 0; i < buf.length; i++)
                    buf[i] = (byte)(Math.sin(2 * Math.PI * i * hz / 44100) * 80);
                line.write(buf, 0, buf.length);
                line.drain();
                line.close();
            } catch (Exception ignored) {}
        }).start();
    }

    private void animateChain(java.util.List<Point> chain, Color color, Runnable onDone) {
        Timer anim = new Timer(60, null);
        int[] idx = {0};
        anim.addActionListener(e -> {
            if (idx[0] < chain.size()) {
                Point p = chain.get(idx[0]++);
                buttons[p.x][p.y].setBackground(color.darker());
                Timer restore = new Timer(120, ev -> {
                    refresh(p.x, p.y);
                    ((Timer) ev.getSource()).stop();
                });
                restore.setRepeats(false);
                restore.start();
            } else {
                anim.stop();
                onDone.run();
            }
        });
        anim.start();
    }

    class ButtonListener implements ActionListener {
        private int x, y;
        public ButtonListener(int x, int y) { this.x = x; this.y = y; }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (board.get(x, y).getColor() != null) return;

            Color color = nextColor;
            nextColor = randomColor();

            board.get(x, y).setColor(color);
            board.get(x, y).setNumber(++clickNum);
            refresh(x, y);
            playTone(440 + clickNum * 20, 80);

            java.util.List<Point> chain = new ArrayList<>();
            for (int i = 0; i < NUM_COLORED_FIELDS; ) {
                Point point = points.remove(points.size() - 1);
                if (board.get(point).getColor() == null) {
                    board.get(point).setColor(color);
                    board.get(point).setNumber(clickNum);
                    chain.add(point);
                    i++;
                }
            }

            animateChain(chain, color, () -> {
                if (board.isOver()) {
                    timer.stop();
                    long elapsed = elapsedTime();
                    playTone(880, 150);
                    new Timer(160, ev -> { playTone(1100, 200); ((Timer)ev.getSource()).stop(); }).start();
                    HighScoreManager.save(boardSize, elapsed);
                    JOptionPane.showMessageDialog(boardPanel,
                        "You won in " + elapsed + " ms!\n\n" + HighScoreManager.getDisplay(boardSize),
                        "Congrats!", JOptionPane.PLAIN_MESSAGE);
                }
            });
        }
    }
}
