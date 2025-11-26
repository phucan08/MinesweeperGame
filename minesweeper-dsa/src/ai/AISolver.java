package ai;

import model.Board;
import model.Cell;
import model.GameState;
import service.GameService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AISolver {

    private final GameService gameService;
    private final Random random = new Random();

    public AISolver(GameService service) {
        this.gameService = service;
    }

    /**
     * Thực hiện 1 "bước suy luận":
     * - Nếu suy được nước đi chắc chắn -> thực hiện và trả true.
     * - Nếu không suy được -> random 1 ô chưa mở/không flag để mở, trả true.
     * - Nếu hết nước đi -> false.
     */
    public boolean makeOneMove() {
        if (gameService.getState() != GameState.PLAYING) return false;

        Board board = gameService.getBoard();
        int rows = board.getRows();
        int cols = board.getCols();

        List<int[]> safeMoves = new ArrayList<>();
        List<int[]> mineMoves = new ArrayList<>();

        // 1. Rule-based logic
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board.getCell(r, c);
                if (!cell.isRevealed()) continue;
                int k = cell.getAdjacentMines();
                if (k == 0) continue;

                List<int[]> neighbors = gameService.getNeighbors(r, c);
                int flagged = 0;
                List<int[]> unknown = new ArrayList<>();

                for (int[] nb : neighbors) {
                    Cell nc = board.getCell(nb[0], nb[1]);
                    if (nc.isFlagged()) flagged++;
                    else if (!nc.isRevealed()) unknown.add(nb);
                }

                if (unknown.isEmpty()) continue;

                int need = k - flagged;
                if (need == 0) {
                    safeMoves.addAll(unknown);
                } else if (need == unknown.size()) {
                    mineMoves.addAll(unknown);
                }
            }
        }

        // 2. Gán cờ cho các ô chắc chắn là mìn
        if (!mineMoves.isEmpty()) {
            for (int[] mv : mineMoves) {
                gameService.toggleFlag(mv[0], mv[1]);
            }
            return true;
        }

        // 3. Mở ô chắc chắn an toàn
        if (!safeMoves.isEmpty()) {
            int[] mv = safeMoves.get(0);
            gameService.reveal(mv[0], mv[1]);
            return true;
        }

        // 4. Không suy được gì => random 1 ô chưa mở & chưa flag
        List<int[]> candidates = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board.getCell(r, c);
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    candidates.add(new int[]{r, c});
                }
            }
        }

        if (candidates.isEmpty()) return false;

        int[] pick = candidates.get(random.nextInt(candidates.size()));
        gameService.reveal(pick[0], pick[1]);
        return true;
    }
}

