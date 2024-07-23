package cn.katool.util.cache.policy.impl;
import cn.katool.util.cache.policy.CachePolicy;
public class SpringCachePolicy implements CachePolicy {
    @Override
    public Object get(Object key) {
        return null;
    }
    @Override
    public void set(Object key, Object value) {
    }
    @Override
    public void update(Object key, Object value) {
    }
    @Override
    public void setOrUpdate(Object key, Object value) {
    }
    @Override
    public void remove(Object key) {
    }
    @Override
    public void clear() {
    }
    @Override
    public Long size() {
        return null;
    }
}
