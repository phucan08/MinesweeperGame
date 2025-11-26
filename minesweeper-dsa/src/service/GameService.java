package service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import model.*;

public class GameService {

    private Board board;
    private GameState state;
    private Difficulty difficulty;
    private boolean firstClick = true;

    // ----- Undo / Redo -----

    private enum ActionType {
        REVEAL,
        FLAG_TOGGLE
    }

    private static class CellChange {
        int row, col;
        boolean prevRevealed, prevFlagged;
        boolean newRevealed, newFlagged;

        CellChange(int row, int col,
                   boolean prevRevealed, boolean prevFlagged,
                   boolean newRevealed, boolean newFlagged) {
            this.row = row;
            this.col = col;
            this.prevRevealed = prevRevealed;
            this.prevFlagged = prevFlagged;
            this.newRevealed = newRevealed;
            this.newFlagged = newFlagged;
        }
    }

    private static class GameAction {
        ActionType type;
        List<CellChange> changes = new ArrayList<>();
        GameState prevState;
        GameState newState;

        GameAction(ActionType type) {
            this.type = type;
        }
    }

    private Deque<GameAction> undoStack = new ArrayDeque<>();
    private Deque<GameAction> redoStack = new ArrayDeque<>();

    // -----------------------

    public GameService(Difficulty diff) {
        this.difficulty = diff;
        reset();
    }

    public void reset() {
        board = new Board(difficulty.rows, difficulty.cols);
        BoardGenerator.generate(board, difficulty.mines);
        state = GameState.PLAYING;
        firstClick = true;

        undoStack.clear();
        redoStack.clear();
    }

    public GameState getState() { return state; }
    public Board getBoard() { return board; }
    public Difficulty getDifficulty() { return difficulty; }

    public boolean inBounds(int r, int c) {
        return 0 <= r && r < board.getRows() && 0 <= c && c < board.getCols();
    }

    // ========== FLAG ==========

    public void toggleFlag(int r, int c) {
        if (!inBounds(r, c)) return;
        if (state != GameState.PLAYING) return;

        Cell cell = board.getCell(r, c);
        if (cell.isRevealed()) return;

        GameAction action = new GameAction(ActionType.FLAG_TOGGLE);
        action.prevState = state;

        boolean prevRevealed = cell.isRevealed();
        boolean prevFlagged = cell.isFlagged();

        cell.toggleFlag();

        boolean newRevealed = cell.isRevealed();
        boolean newFlagged = cell.isFlagged();

        if (prevRevealed != newRevealed || prevFlagged != newFlagged) {
            action.changes.add(new CellChange(
                    r, c,
                    prevRevealed, prevFlagged,
                    newRevealed, newFlagged
            ));
        }

        action.newState = state; // state không đổi với flag
        pushAction(action);
    }

    // ========== REVEAL ==========

    public void reveal(int r, int c) {
        if (!inBounds(r, c)) return;
        if (state != GameState.PLAYING) return;

        Cell cell = board.getCell(r, c);
        if (cell.isRevealed() || cell.isFlagged()) return;

        GameAction action = new GameAction(ActionType.REVEAL);
        action.prevState = state;

        // Đảm bảo first click không dính mìn (optional)
        if (firstClick) {
            firstClick = false;
            if (cell.isMine()) {
                do {
                    board = new Board(difficulty.rows, difficulty.cols);
                    BoardGenerator.generate(board, difficulty.mines);
                    cell = board.getCell(r, c);
                } while (cell.isMine());
            }
        }

        // Nếu là mìn
        if (cell.isMine()) {
            boolean prevRevealed = cell.isRevealed();
            boolean prevFlagged = cell.isFlagged();

            cell.reveal();

            action.changes.add(new CellChange(
                    r, c,
                    prevRevealed, prevFlagged,
                    cell.isRevealed(), cell.isFlagged()
            ));

            state = GameState.LOST;
            action.newState = state;
            pushAction(action);
            return;
        }

        // Nếu là ô số 0 => FloodFill
        if (cell.getAdjacentMines() == 0) {
            List<int[]> opened = FloodFill.reveal(board, r, c);

            for (int[] p : opened) {
                int rr = p[0], cc = p[1];
                Cell ccCell = board.getCell(rr, cc);

                if (!ccCell.isRevealed()) {
                    boolean prevRevealed = ccCell.isRevealed();
                    boolean prevFlagged = ccCell.isFlagged();

                    ccCell.reveal();

                    boolean newRevealed = ccCell.isRevealed();
                    boolean newFlagged = ccCell.isFlagged();

                    if (prevRevealed != newRevealed || prevFlagged != newFlagged) {
                        action.changes.add(new CellChange(
                                rr, cc,
                                prevRevealed, prevFlagged,
                                newRevealed, newFlagged
                        ));
                    }
                }
            }
        } else {
            // ô số > 0
            boolean prevRevealed = cell.isRevealed();
            boolean prevFlagged = cell.isFlagged();

            cell.reveal();

            boolean newRevealed = cell.isRevealed();
            boolean newFlagged = cell.isFlagged();

            if (prevRevealed != newRevealed || prevFlagged != newFlagged) {
                action.changes.add(new CellChange(
                        r, c,
                        prevRevealed, prevFlagged,
                        newRevealed, newFlagged
                ));
            }
        }

        if (checkWin()) {
            state = GameState.WON;
        }
        action.newState = state;

        if (!action.changes.isEmpty()) {
            pushAction(action);
        }
    }

    public boolean checkWin() {
        int total = board.getRows() * board.getCols();
        int mines = 0;
        int revealed = 0;

        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                Cell c = board.getCell(i, j);
                if (c.isMine()) mines++;
                if (c.isRevealed()) revealed++;
            }
        }
        return revealed == total - mines;
    }

    public List<int[]> getNeighbors(int r, int c) {
        List<int[]> list = new ArrayList<>();
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = r + dr, nc = c + dc;
                if (inBounds(nr, nc)) list.add(new int[]{nr, nc});
            }
        }
        return list;
    }

    // ---------- Thông tin mìn cho GUI ----------

    public int getTotalMines() {
        return difficulty.mines;
    }

    public int getFlagCount() {
        int flags = 0;
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                if (board.getCell(i, j).isFlagged()) {
                    flags++;
                }
            }
        }
        return flags;
    }

    public int getRemainingMines() {
        return getTotalMines() - getFlagCount();
    }

    // ---------- Undo / Redo API ----------

    private void pushAction(GameAction action) {
        undoStack.push(action);
        // mỗi action mới thì clear redo
        redoStack.clear();
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public boolean undo() {
        if (undoStack.isEmpty()) return false;

        GameAction action = undoStack.pop();
        // revert cells về prev
        for (CellChange ch : action.changes) {
            Cell cell = board.getCell(ch.row, ch.col);
            cell.setRevealed(ch.prevRevealed);
            cell.setFlagged(ch.prevFlagged);
        }
        // revert state
        state = action.prevState;

        redoStack.push(action);
        return true;
    }

    public boolean redo() {
        if (redoStack.isEmpty()) return false;

        GameAction action = redoStack.pop();
        // set cells về new
        for (CellChange ch : action.changes) {
            Cell cell = board.getCell(ch.row, ch.col);
            cell.setRevealed(ch.newRevealed);
            cell.setFlagged(ch.newFlagged);
        }
        // set state về newState
        state = action.newState;

        undoStack.push(action);
        return true;
    }
}
