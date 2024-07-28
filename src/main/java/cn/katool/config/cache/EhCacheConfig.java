package cn.katool.config.cache;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
@Slf4j
@Component
@Data
@RefreshScope
@ConfigurationProperties("katool.util.cache.ehcache")
public class EhCacheConfig {
    Boolean  enable = false;
    @DependsOn("katool-cache")
    @Bean("katool-ehcache-cache")
    @ConditionalOnExpression("${katool.cache.ehcache.enable:false}.equals('true')")
    public Cache Cache() {
        return (Cache) CacheConfig.getCache(CacheConfig.EHCACHE);
    }
}
