package edu.cust.secad.model.chat;

/**
 * @ClassDescription:
 * @Author: Nvgu
 * @Created: 2024/12/10 1:05
 * @Updated: 2024/12/10 1:05
 */
public class CacheItem {
    private byte[][] data;
    private long timestamp;

    public CacheItem(byte[][] data) {
        this.data = data;
        this.timestamp = System.currentTimeMillis(); // 设置当前时间戳
    }

    public byte[][] getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
