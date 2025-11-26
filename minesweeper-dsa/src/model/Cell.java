package model;

import java.io.Serializable;

public class Cell implements Serializable {
    private boolean isMine;
    private boolean isRevealed;
    private boolean isFlagged;
    private int adjacentMines;

    public Cell() {
        this.isMine = false;
        this.isRevealed = false;
        this.isFlagged = false;
        this.adjacentMines = 0;
    }

    public boolean isMine() { return isMine; }
    public void setMine(boolean mine) { this.isMine = mine; }

    public boolean isRevealed() { return isRevealed; }
    public void reveal() { this.isRevealed = true; }
    public void hide() { this.isRevealed = false; }
    public void setRevealed(boolean revealed) { this.isRevealed = revealed; }

    public boolean isFlagged() { return isFlagged; }
    public void toggleFlag() { this.isFlagged = !this.isFlagged; }
    public void setFlagged(boolean flagged) { this.isFlagged = flagged; }

    public int getAdjacentMines() { return adjacentMines; }
    public void setAdjacentMines(int count) { this.adjacentMines = count; }
}
