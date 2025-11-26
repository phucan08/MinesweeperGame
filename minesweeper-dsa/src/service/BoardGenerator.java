package service;

import model.Board;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class BoardGenerator {

    public static void generate(Board board, int mines) {
        Random r = new Random();
        int rows = board.getRows();
        int cols = board.getCols();

        Set<Integer> used = new HashSet<>();

        while (used.size() < mines) {
            int id = r.nextInt(rows * cols);
            used.add(id);
        }

        for (int id : used) {
            int row = id / cols;
            int col = id % cols;
            board.getCell(row, col).setMine(true);
        }

        computeAdjacent(board);
    }

    private static void computeAdjacent(Board b) {
        int r = b.getRows(), c = b.getCols();

        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                if (b.getCell(i, j).isMine()) continue;

                int count = 0;

                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        int nr = i + dr, nc = j + dc;
                        if (0 <= nr && nr < r && 0 <= nc && nc < c)
                            if (b.getCell(nr, nc).isMine())
                                count++;
                    }
                }
                b.getCell(i, j).setAdjacentMines(count);
            }
        }
    }
}
