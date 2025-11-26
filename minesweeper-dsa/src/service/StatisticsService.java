package service;

import java.io.*;
import java.util.*;
import model.Difficulty;

/**
 * Lưu best time theo từng độ khó.
 * Có khả năng:
 *  - Quản lý top 10 (PriorityQueue + EnumMap)
 *  - Lưu ra file + đọc vào (Serializable)
 */
public class StatisticsService implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int MAX_RECORDS = 10;

    // Mỗi độ khó có 1 priority queue: lưu 10 thời gian nhỏ nhất (ms)
    private final Map<Difficulty, PriorityQueue<Long>> bestTimes;

    // --------- Constructor mặc định ---------

    public StatisticsService() {
        bestTimes = new EnumMap<>(Difficulty.class);
        Comparator<Long> desc = (a, b) -> Long.compare(b, a); // max-heap

        for (Difficulty diff : Difficulty.values()) {
            bestTimes.put(diff, new PriorityQueue<>(desc));
        }
    }

    // --------- API thêm / lấy dữ liệu ---------

    /**
     * Thêm 1 record cho độ khó diff.
     * Giữ lại tối đa 10 thời gian nhỏ nhất.
     */
    public void addRecord(Difficulty diff, long timeMillis) {
        PriorityQueue<Long> pq = bestTimes.get(diff);
        if (pq == null) {
            Comparator<Long> desc = (a, b) -> Long.compare(b, a);
            pq = new PriorityQueue<>(desc);
            bestTimes.put(diff, pq);
        }

        if (pq.size() < MAX_RECORDS) {
            pq.offer(timeMillis);
        } else {
            Long worstBest = pq.peek(); // lớn nhất trong top hiện tại
            if (worstBest != null && timeMillis < worstBest) {
                pq.poll();         // bỏ record tệ nhất
                pq.offer(timeMillis);
            }
        }
    }

    /**
     * Trả về list thời gian (ms) đã sắp xếp tăng dần cho 1 độ khó.
     */
    public List<Long> getTopTimes(Difficulty diff) {
        PriorityQueue<Long> pq = bestTimes.get(diff);
        if (pq == null || pq.isEmpty()) return Collections.emptyList();

        List<Long> list = new ArrayList<>(pq);
        list.sort(Long::compareTo);
        return list;
    }

    public boolean hasRecords(Difficulty diff) {
        PriorityQueue<Long> pq = bestTimes.get(diff);
        return pq != null && !pq.isEmpty();
    }

    // --------- Lưu / Đọc file ---------

    /**
     * Lưu thống kê ra file, dùng ObjectOutputStream.
     */
    public void saveToFile(String path) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(this);
        } catch (IOException e) {
            System.err.println("Failed to save statistics: " + e.getMessage());
        }
    }

    /**
     * Đọc thống kê từ file. Nếu lỗi hoặc file không tồn tại → trả về instance mới.
     */
    public static StatisticsService loadFromFile(String path) {
        File f = new File(path);
        if (!f.exists()) {
            return new StatisticsService();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            Object obj = ois.readObject();
            if (obj instanceof StatisticsService s) {
                return s;
            } else {
                return new StatisticsService();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load statistics: " + e.getMessage());
            return new StatisticsService();
        }
    }
}
