package model;

import java.io.Serializable;

public class Board implements Serializable {
    private int rows;
    private int cols;
    private Cell[][] grid;

    public Board(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        grid = new Cell[rows][cols];

        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                grid[i][j] = new Cell();
    }

    public Cell getCell(int r, int c) {
        return grid[r][c];
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
}
