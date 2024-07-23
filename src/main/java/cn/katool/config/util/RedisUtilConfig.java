package cn.katool.config.util;
import cn.katool.common.MethodInterface;
import cn.katool.config.cache.CacheConfig;
import cn.katool.util.cache.policy.impl.EhCacheCachePolicy;
import cn.katool.util.lock.RedisLockUtil;
import cn.katool.util.cache.policy.CachePolicy;
import cn.katool.util.cache.policy.impl.CaffeineCachePolicy;
import cn.katool.util.cache.policy.impl.DefaultCachePolicy;
import cn.katool.util.database.nosql.RedisUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.core.RedisTemplate;
import javax.annotation.Resource;
import java.util.HashMap;
@Slf4j
@Configuration("RedisUtilConfig")
@ConfigurationProperties("katool.util.redis")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ComponentScan("cn.katool.*")
@Scope("singleton")            //  开启单例模式
public class RedisUtilConfig {
    @Resource
    CacheConfig cacheConfig;
    private String policy="default";    // 不写采用默认策略，默认情况下缓存Cache使用的策略
    private static HashMap<String, MethodInterface<CachePolicy>>
            REDIS_UTIL_CACHE_POLICY_MAPPER = new HashMap<>();
    // 采用的缓存策略
    @Bean("katool-redisutil-cachepolicy")
    @DependsOn({"KaTool-Init"})
    @ConditionalOnMissingBean({CachePolicy.class})
    public CachePolicy cachePolicy() {
        MethodInterface<CachePolicy> runMehtod = REDIS_UTIL_CACHE_POLICY_MAPPER.get(policy);
        return runMehtod.apply();
    }
    @Resource
    RedisTemplate redisTemplate;
    @Bean
    @DependsOn({"KaTool-Init"})
    public RedisUtils RedisUtils(){
        return RedisUtils.getInstance(redisTemplate);
    }
    @Bean
    @DependsOn({"KaTool-Init"})
    public RedisLockUtil LockUtil(){
        return RedisLockUtil.getInstance();
    }
    {
        log.info("【KaTool::Bean Factory】katool-redisutil-cachepolicy => 初始化预备缓存策略【{}】","REDIS_UTIL_CACHE_POLICY_MAPPER");
        REDIS_UTIL_CACHE_POLICY_MAPPER.put("caffeine", ((MethodInterface)()->{
            log.info("【KaTool::Bean Factory】katool-redisutil-cachepolicy => 使用Caffeine缓存策略");
            return null;
        }).andThen(() -> new CaffeineCachePolicy()));
        REDIS_UTIL_CACHE_POLICY_MAPPER.put("default", ((MethodInterface)()->{
            log.info("【KaTool::Bean Factory】katool-redisutil-cachepolicy => 使用默认策略，直接走redis");
            return null;
        }).andThen(() -> new DefaultCachePolicy()));
        REDIS_UTIL_CACHE_POLICY_MAPPER.put("ehcache", ((MethodInterface)()->{
            log.info("【KaTool::Bean Factory】katool-redisutil-cachepolicy => 使用Ehcache缓存策略");
            return null;
        }).andThen(() -> new EhCacheCachePolicy()));
    }
}