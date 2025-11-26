package model;

public enum Difficulty {
    EASY(9, 9, 10),
    MEDIUM(16, 16, 40),
    HARD(24, 24, 99),
    EXTREME(30, 30, 180);

    public final int rows;
    public final int cols;
    public final int mines;

    Difficulty(int r, int c, int m) {
        this.rows = r;
        this.cols = c;
        this.mines = m;
    }
}

