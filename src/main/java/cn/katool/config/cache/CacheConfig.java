package cn.katool.config.cache;
import cn.katool.util.ExpDateUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.util.TimeUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.*;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@ComponentScan("cn.katool.*")
@Configuration("CacheConfig")
@ConfigurationProperties("katool.util.cache")
@RefreshScope
public class CacheConfig {
    public static final String CAFFEINE = "caffeine";
    public static final String EHCACHE = "ehcache";
    private Long expTime = 5*60*1000L;
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    private Integer maxCacheSize = 100000;
    @Value("${katool.cache.ehcache.enable-to-disk:false}")
    private Boolean  enableEhCacheToDisk = false;
    /**
     * 策略
     */
    private String policy="default";
    private static HashMap<String, Supplier> CACHE_POLICY_MAPPER = new HashMap<>();
    public static HashMap<String, Supplier> getCACHE_POLICY_MAPPER() {
        return CACHE_POLICY_MAPPER;
    }
    @Bean("katool-cache")
    @DependsOn({"KaTool-Init"})
    @ConditionalOnMissingBean({Cache.class,CacheManager.class})
    public Object Cache() {
        return CACHE_POLICY_MAPPER.getOrDefault(policy,CACHE_POLICY_MAPPER.get("default")).get();
    }
    public static Object getCache(String policy) {
        return CACHE_POLICY_MAPPER.getOrDefault(policy,CACHE_POLICY_MAPPER.get("default")).get();
    }
    {
        log.info("【KaTool::Bean Factory】katool-CacheConfig-cache => 初始化预备缓存策略【{}】","CACHE_POLICY_MAPPER");
        CACHE_POLICY_MAPPER.put("caffeine",()->{
            Cache<String, Object> caffeine = Caffeine.newBuilder()
                    .expireAfterAccess(expTime, timeUnit)
                    .maximumSize(1000)
                    .build();
            log.info("【KaTool::Bean Factory】katool-CacheConfig-cache => 使用Caffeine缓存策略 使用Caffeine缓存 建立Cache：{}",caffeine);
            return caffeine;
        });
        CACHE_POLICY_MAPPER.put("default",()->{
            log.info("【KaTool::Bean Factory】katool-CacheConfig-cache => Policy值为[{}]，加载默认Bean策略 使用Caffeine缓存",policy);
            return CACHE_POLICY_MAPPER.get("caffeine").get();
        });
        CACHE_POLICY_MAPPER.put("ehcache",()->{
            CacheManager cacheManager = CacheManager.create();
            net.sf.ehcache.Cache ehCache = cacheManager.getCache("katool-ehcache");
            if (ehCache == null){
                ehCache = new net.sf.ehcache.Cache("katool-ehcache", maxCacheSize, enableEhCacheToDisk, false, timeUnit.toSeconds(expTime), timeUnit.toSeconds(expTime));
                cacheManager.addCache(ehCache);
            }
            CacheConfiguration config = ehCache.getCacheConfiguration();
            config.setTimeToIdleSeconds(timeUnit.toSeconds(expTime));
            config.setTimeToLiveSeconds(timeUnit.toSeconds(expTime));
            log.info("【KaTool::Bean Factory】katool-CacheConfig-cache => 加载EhCache缓存策略 使用EhCache缓存 建立Cache：{}",ehCache);
            return cacheManager;
        });
    }
}
