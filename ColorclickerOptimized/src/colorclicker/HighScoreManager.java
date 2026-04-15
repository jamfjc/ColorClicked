package colorclicker;

import java.io.*;
import java.util.*;

public class HighScoreManager {

    private static final String FILE = System.getProperty("user.home") + "/colorclicker_scores.txt";
    private static final int MAX_SCORES = 5;

    public static void save(int boardSize, long timeMs) {
        List<Long> scores = load(boardSize);
        scores.add(timeMs);
        Collections.sort(scores);
        if (scores.size() > MAX_SCORES) scores = scores.subList(0, MAX_SCORES);
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE, true))) {
            pw.println(boardSize + "," + timeMs);
        } catch (IOException ignored) {}
    }

    public static List<Long> load(int boardSize) {
        List<Long> scores = new ArrayList<>();
        File f = new File(FILE);
        if (!f.exists()) return scores;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2 && Integer.parseInt(parts[0]) == boardSize)
                    scores.add(Long.parseLong(parts[1]));
            }
        } catch (IOException ignored) {}
        Collections.sort(scores);
        return scores.size() > MAX_SCORES ? scores.subList(0, MAX_SCORES) : scores;
    }

    public static String getDisplay(int boardSize) {
        List<Long> scores = load(boardSize);
        if (scores.isEmpty()) return "No scores yet for " + boardSize + "x" + boardSize;
        StringBuilder sb = new StringBuilder("Best times for " + boardSize + "x" + boardSize + ":\n");
        for (int i = 0; i < scores.size(); i++)
            sb.append((i + 1)).append(". ").append(scores.get(i)).append(" ms\n");
        return sb.toString();
    }
}
