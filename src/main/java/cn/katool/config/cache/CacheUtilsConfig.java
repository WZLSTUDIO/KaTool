package cn.katool.config.cache;
import cn.katool.util.cache.policy.CachePolicy;
import cn.katool.util.cache.utils.CaffeineUtils;
import cn.katool.util.cache.utils.EhCacheUtils;
import cn.katool.util.classes.SpringContextUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;
@Slf4j
@Component
@RefreshScope
@DependsOn({"katool-cache"})
public class CacheUtilsConfig {
    @Resource
    CacheConfig cacheConfig;
    @Resource(name = "katool-cache")
    Object cache;
    @Bean("CaffeineUtils")
    @DependsOn({"katool-cache"})
    @ConditionalOnMissingBean({CaffeineUtils.class, CachePolicy.class})
    public CaffeineUtils getCaffeineCache(){
        Object innterCache = cache;
        log.info("【KaTool::Bean Factory】CaffeineUtils  =>  初始化 CaffeineUtils 实例 {}",innterCache);
        if (cache == null || !cache.getClass().getName().equals("com.github.benmanes.caffeine.cache.BoundedLocalCache$BoundedLocalManualCache")){
            log.info("【KaTool::Bean Factory】CaffeineUtils  =>  cache实例不符，修改当前 CaffeineUtils 实例 {}",innterCache);
            innterCache = Caffeine.newBuilder()
                    .expireAfterAccess(cacheConfig.getExpTime(), TimeUnit.MILLISECONDS)
                    .maximumSize(cacheConfig.getMaxCacheSize())
                    .build();
            SpringContextUtils.regBean("CaffeineUtils",innterCache);
            log.info("【KaTool::Bean Factory】CaffeineUtils  =>  cache实例不符，修改为 CaffeineUtils 实例 {}",innterCache);
        }
        return new CaffeineUtils<String,Object>((Cache<String,Object>)innterCache);
    }
    @Bean("EhCacheUtils")
    @DependsOn({"katool-cache"})
    @ConditionalOnMissingBean({EhCacheUtils.class, CachePolicy.class})
    public EhCacheUtils getEhCache(){
        Object innterCache = cache;
        log.info("【KaTool::Bean Factory】EhCacheUtils  =>  初始化 EhCacheUtils 实例 {}",innterCache);
        if (cache == null || !cache.getClass().getName().equals("net.sf.ehcache.CacheManager")){
            log.info("【KaTool::Bean Factory】EhCacheUtils  =>  cache实例不符，修改当前 EhCacheUtils 实例 {}",innterCache);
            CacheManager cacheManager = CacheManager.create();
            net.sf.ehcache.Cache ehCache = new net.sf.ehcache.Cache("katool-ehcache", cacheConfig.getMaxCacheSize(), cacheConfig.getEnableEhCacheToDisk(), false, cacheConfig.getTimeUnit().toSeconds(cacheConfig.getExpTime()), cacheConfig.getTimeUnit().toSeconds(cacheConfig.getExpTime()));
            cacheManager.addCache(ehCache);
            CacheConfiguration config = ehCache.getCacheConfiguration();
            config.setTimeToIdleSeconds(cacheConfig.getTimeUnit().toSeconds(cacheConfig.getExpTime()));
            config.setTimeToLiveSeconds(cacheConfig.getTimeUnit().toSeconds(cacheConfig.getExpTime()));
            innterCache = cacheManager;
            SpringContextUtils.regBean("katool-ehcache", innterCache);
            log.info("【KaTool::Bean Factory】EhCacheUtils  =>  cache实例不符，修改为 EhCacheUtils 实例 {}",innterCache);
        }
        EhCacheUtils<Object, Object> ehCacheUtils = new EhCacheUtils<>((CacheManager) innterCache);
        return ehCacheUtils;
    }
}
