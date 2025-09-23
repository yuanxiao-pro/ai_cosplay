package edu.cust.secad.chat.component;

import edu.cust.secad.model.chat.CacheItem;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CacheCleaner {

    private static final long EXPIRATION_TIME = 3 * 60 * 1000; // 设置过期时间为10分钟
    private final ConcurrentHashMap<String, CacheItem> cache = new ConcurrentHashMap<>();

    // 向缓存中添加数据
    public void put(String key, byte[][] data) {
        cache.put(key, new CacheItem(data));
    }

    // 向缓存中删除数据
    public void delete(String key) {
        cache.remove(key);
    }

    // 从缓存中获取数据
    public byte[][] get(String key) {
        CacheItem item = cache.get(key);
        if (item != null) {
            return item.getData();
        }
        return null;
    }

    // 定时清理过期的条目
    @Scheduled(fixedRate = 60 * 1000)  // 每分钟执行一次清理任务
    public void cleanExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        Iterator<String> iterator = cache.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            CacheItem item = cache.get(key);
            if (item != null && (currentTime - item.getTimestamp() > EXPIRATION_TIME)) {
                // 如果条目已过期，移除它
                System.out.println("Removing expired entry for key: " + key);
                iterator.remove();
            }
        }
    }
}
