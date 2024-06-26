package cn.katool.config.ai.kimi;

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
@Configuration("KimiConfig")
@ConfigurationProperties("katool.util.ai.kimi")
public class KimiConfig {
    public static final String KIMI_BASE_URL = "https://api.moonshot.cn/v1";
    public static String KIMI_API_KEY = "";

    public String key = "";

    @Bean
    public void KimiConfigInit(){
        log.info("KimiConfigInit");
        KIMI_API_KEY = key;
    }
}
