package cn.katool.store.util.cache.utils;
import javax.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class EhCacheUtils<K,V> {
    @Resource
    private CacheManager cacheManager;
    private final String DEFAULT_CACHE_NAME = "katool-ehcache";
    /**
     * 获取缓存
     */
    public Cache getDefaultCache() {
        return getCache(DEFAULT_CACHE_NAME);
    }
    public void clear(){
        getDefaultCache().removeAll();
    }
    /**
     * 获取缓存
     */
    private Cache getCache(String cacheName) {
        return cacheManager.getCache(cacheName);
    }
    /**
     * 获取值
     *
     * @param key
     * @return
     */
    public Element get(K key) {
        return getDefaultCache().get(key);
    }
    /**
     * 获取值，并反序列化
     */
    public Object getValue(K key) {
        return getDefaultCache().get(key).getObjectValue();
    }
    public Boolean countain(K key){
        return  getDefaultCache().isKeyInCache(key);
    }
    /**
     * 存放一个数据
     *
     * @param key   key
     * @param value value
     */
    public void put(K key, V value) {
        getDefaultCache().put(new Element(key, value));
    }
    /**
     * 存在时不添加
     * @param key key
     * @param value value
     */
    public void putIfAbsent(K key, V value) {
        getDefaultCache().putIfAbsent(new Element(key, value));
    }
    /**
     * 更新操作
     * @param key key
     * @param value value
     */
    public void update(K key, V value){
        put(key, value);
    }
    public void updateCrr(K key, V value){
        synchronized (key){
            remove(key);
            put(key, value);
        }
    }
    /**
     * 移除对应key的数据
     *
     * @param key key
     */
    public void remove(K key) {
        getDefaultCache().remove(key);
    }
    public Long getSize(){
        return (long) getDefaultCache().getSize();
    }
}