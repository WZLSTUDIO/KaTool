package cn.katool.store.util.cache.policy.impl;
import cn.katool.store.util.cache.policy.CachePolicy;
import cn.katool.store.util.cache.utils.EhCacheUtils;

import javax.annotation.Resource;
public class EhCacheCachePolicy implements CachePolicy {
    @Resource
    EhCacheUtils<Object,Object> ehCacheUtils;
    @Override
    public Object get(Object key) {
        return ehCacheUtils.getValue(key);
    }
    @Override
    public void set(Object key, Object value) {
        ehCacheUtils.put(key, value);
    }
    @Override
    public void update(Object key, Object value) {
        ehCacheUtils.update(key, value);
    }
    @Override
    public void setOrUpdate(Object key, Object value) {
        if (ehCacheUtils.countain(key)) {
            ehCacheUtils.updateCrr(key, value);
        }
        update(key, value);
    }
    @Override
    public void remove(Object key) {
        ehCacheUtils.remove(key);
    }
    @Override
    public void clear() {
        ehCacheUtils.clear();
    }
    @Override
    public Long size() {
        return ehCacheUtils.getSize();
    }
}
