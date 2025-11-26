package service;

import model.Board;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class FloodFill {

    /**
     * Trả về danh sách các ô (r,c) sẽ được mở khi flood-fill từ (r,c)
     * Hàm này không tự reveal, chỉ tính toán và trả về list.
     */
    public static List<int[]> reveal(Board board, int r, int c) {
        List<int[]> opened = new ArrayList<>();

        if (board.getCell(r, c).getAdjacentMines() > 0) {
            opened.add(new int[]{r, c});
            return opened;
        }

        Queue<int[]> q = new LinkedList<>();
        boolean[][] visited = new boolean[board.getRows()][board.getCols()];

        q.add(new int[]{r, c});
        visited[r][c] = true;

        while (!q.isEmpty()) {
            int[] cur = q.poll();
            int x = cur[0], y = cur[1];
            opened.add(cur);

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int nx = x + dx, ny = y + dy;

                    if (nx < 0 || ny < 0 || nx >= board.getRows() || ny >= board.getCols()) continue;
                    if (visited[nx][ny]) continue;

                    visited[nx][ny] = true;

                    if (board.getCell(nx, ny).getAdjacentMines() == 0) {
                        q.add(new int[]{nx, ny});
                    } else {
                        opened.add(new int[]{nx, ny});
                    }
                }
            }
        }

        return opened;
    }
}
