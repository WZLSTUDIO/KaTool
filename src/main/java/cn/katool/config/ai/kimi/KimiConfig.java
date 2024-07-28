package cn.katool.config.ai.kimi;
import cn.katool.Exception.ErrorCode;
import cn.katool.Exception.KaToolException;

import jdk.nashorn.internal.ir.annotations.Reference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.Pair;


import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@ComponentScan("cn.katool.*")
@Configuration("KimiConfig")
@ConfigurationProperties("katool.util.ai.kimi")
@RefreshScope
public class KimiConfig {
    public static final String KIMI_BASE_URL = "https://api.moonshot.cn/v1";
    public static List<String> KIMI_API_KEY = Arrays.asList("");
    public static Boolean KIMI_AUTO_UPGRADE = false;
    public List<String> key = Arrays.asList("");
    public Boolean enableAutoUpgrade = true;

    @Value("${katool.util.ai.kimi.key}")
    public void KimiConfigInit(List<String> key){
        KIMI_API_KEY = this.key = key;
    }

    @Value("${katool.util.ai.kimi.enableAutoUpgrade}")
    public void KimiConfigInit(Boolean enableAutoUpgrade){
        KIMI_AUTO_UPGRADE = this.enableAutoUpgrade = enableAutoUpgrade;
    }





}
