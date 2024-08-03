package cn.katool.store.util.cache.policy;
public interface CachePolicy {
    Object  get(Object key);
    void    set(Object key, Object value);
    void update(Object key, Object value);
    void setOrUpdate(Object key, Object value);
    void    remove(Object key);
    void    clear();
    // 获取缓存大小
    Long size();
}
