package cn.katool.util.cache.policy.impl;
import cn.katool.util.cache.policy.CachePolicy;
import cn.katool.util.cache.utils.CaffeineUtils;
import javax.annotation.Resource;
public class CaffeineCachePolicy implements CachePolicy {
    @Resource()
    private CaffeineUtils<Object,Object>  caffeineUtils;
    @Override
    public Object get(Object key) {
        return caffeineUtils.get(key);
    }
    @Override
    public void set(Object key, Object value) {
        caffeineUtils.put(key,value);;
    }
    @Override
    public void update(Object key, Object value) {
        caffeineUtils.update(key,value);
    }
    @Override
    public void setOrUpdate(Object key, Object value) {
        if (caffeineUtils.contains(key)){
            caffeineUtils.update(key,value);
        }
        else{
            caffeineUtils.put(key,value);
        }
    }
    @Override
    public void remove(Object key) {
        caffeineUtils.delete(key);
    }
    @Override
    public void clear() {
        caffeineUtils.deleteAll();
    }
    @Override
    public Long size() {
        return caffeineUtils.getCache().estimatedSize();
    }
}
