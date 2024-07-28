package cn.katool.config.cache;
import cn.katool.util.cache.policy.CachePolicy;
import cn.katool.util.cache.utils.CaffeineUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;
@Slf4j
@Component
@Data
@RefreshScope
@ConfigurationProperties("katool.util.cache.caffeine")
public class CaffineConfig {
    Boolean  enable = false;
    @DependsOn("katool-cache")
    @Bean("katool-caffine-cache")
    @ConditionalOnExpression("${katool.cache.caffeine.enable:false}.equals('true')")
    public Cache Cache() {
        return (Cache) CacheConfig.getCache(CacheConfig.CAFFEINE);
    }
}
