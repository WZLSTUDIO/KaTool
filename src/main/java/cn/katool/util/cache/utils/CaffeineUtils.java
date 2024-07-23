package cn.katool.util.cache.utils;
import com.github.benmanes.caffeine.cache.Cache;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class CaffeineUtils<K,V> {
    @NotNull
    private Cache<K,V> cache;
    public Cache getCache() {
        return this.cache;
    }
    // =================================================================
    // 获取缓存
    /**
     * 依据key获取value, 如果未找到, 返回null
     *
     * @return Object
     */
    public V get(@NotNull K key) {
        // 就是相当于cache.getIfPresent(key)
        return cache.asMap().get(key);
    }
    /**
     * 依据key获取value, 如果未找到, 返回null
     *
     * @return Object
     */
    public V getIfPresent(@NotNull K key) {
        // 就是相当于get(key)
        return cache.getIfPresent(key);
    }
    /**
     * 批量依据key获取value
     *
     * @return Object
     */
    public Map<K, V> getBatch(@NotNull List<Object> key) {
        //
        return cache.getAllPresent(key);
    }
    /**
     * 得到缓存Map
     *
     * @return ConcurrentMap<K, V>
     */
    public ConcurrentMap<K, V> get() {
        return cache.asMap();
    }
    // =================================================================
    // 插入,修改缓存
    /**
     * 插入一个缓存
     *
     * @param key   key
     * @param value value
     */
    public void put(@NotNull K key, V value) {
        //
        cache.put(key, value);
    }
    /**
     * 插入缓存,如果不存在，则将value放入缓存
     *
     * @param key   key
     * @param value value
     */
    public V getIfNotExist(@NotNull K key, V value) {
        //
        return cache.get(key, k -> value);
    }
    /**
     * 将一个map插入或修改缓存
     */
    public void putBatch(@NotNull Map<? extends K, ? extends V> map) {
        //
        cache.asMap().putAll(map);
    }
    /**
     * 更新一个指定key的缓存
     *
     * @param key   key
     * @param value value
     */
    public void update(@NotNull K key, V value) {
        //
        cache.put(key, value);
    }
    // =================================================================
    // 判断缓存
    /**
     * 是否含有指定key的缓存
     *
     * @param key key
     */
    public boolean contains(@NotNull K key) {
        //
        return cache.asMap().containsKey(key);
    }
    // =================================================================
    // 删除缓存
    /**
     * 删除指定key的缓存
     *
     * @param key key
     */
    public void delete(@NotNull K key) {
        //
        cache.asMap().remove(key);
    }
    /**
     * 批量删除指定key的缓存
     *
     * @param key key
     */
    public void delete(@NotNull List<Object> key) {
        //
        cache.invalidateAll(key);
    }
    /**
     * 删除指定key的缓存
     *
     * @param key key
     */
    public void invalidate(@NotNull K key) {
        //
        cache.invalidate(key);
    }
    /**
     * 清除所有缓存
     */
    public void deleteAll() {
        //
        cache.invalidateAll();
    }
}
