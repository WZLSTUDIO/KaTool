package cn.katool.config.ai.kimi;
import ch.qos.logback.classic.util.CopyOnInheritThreadLocal;
import cn.katool.common.CopyOnTransmittableThreadLocal;
import cn.katool.services.ai.model.builder.kimi.KimiBuilder;
import cn.katool.services.ai.model.entity.kimi.Kimi;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@ComponentScan("cn.katool.*")
@Configuration("KimiProxyConfig")
@ConfigurationProperties("katool.util.ai.kimi.proxy")
public class KimiProxyConfig {
    public static Boolean ENABLE = false;
    public static String HOST = "127.0.0.1";
    public static Integer PORT = 80;
    Boolean enable;
    String host;
    Integer port;
    @Bean
    public void KimiProxyConfigInit(){
        ENABLE = enable;
        HOST = host;
        PORT = port;
    }
}
