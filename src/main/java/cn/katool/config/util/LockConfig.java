/**
 * Title
 *
 * @ClassName: LockConfig
 * @Description:
 * @author: 巫宗霖
 * @date: 2023/4/15 13:22
 * @Blog: https://www.wzl1.top/
 */
package cn.katool.config.util;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import java.util.concurrent.TimeUnit;
@Configuration("LockConfig")
@ToString
@Data
//@ConfigurationProperties("katool.lock")
@AllArgsConstructor
@NoArgsConstructor
@ComponentScan("cn.katool.*")
@Scope("singleton")            //  开启单例模式
@ConfigurationProperties("katool.util.redis.lock")
@DependsOn({"KaTool-Init"})
public class LockConfig {
//    @Value("katool.lock")
    private Long internalLockLeaseTime = 30L;
    private TimeUnit timeUnit = TimeUnit.SECONDS;
}
