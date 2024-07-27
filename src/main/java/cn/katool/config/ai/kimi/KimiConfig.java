package cn.katool.config.ai.kimi;
import cn.katool.Exception.ErrorCode;
import cn.katool.Exception.KaToolException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
public class KimiConfig {
    public static final String KIMI_BASE_URL = "https://api.moonshot.cn/v1";
    public static List<String> KIMI_API_KEY = Arrays.asList("");
    public List<String> key = Arrays.asList("");
    @Bean
    public void KimiConfigInit(){
        log.info("KimiConfigInit");
        KIMI_API_KEY = key;
    }


    private volatile static Queue<String> keyUsingMap = new ConcurrentLinkedDeque<>();

    public static String getKimiKey(){
        List<String> reduce_key = KIMI_API_KEY.stream().filter(o -> !keyUsingMap.contains(o)).collect(Collectors.toList());
        List<String> expire_key = keyUsingMap.stream().filter(o -> !KIMI_API_KEY.contains(o)).collect(Collectors.toList());
        if (keyUsingMap.isEmpty()||keyUsingMap.size()!=KIMI_API_KEY.size()||!reduce_key.isEmpty()) {
            // init to map
            if (KIMI_API_KEY.isEmpty()) {
                throw new KaToolException(ErrorCode.PARAMS_ERROR, "请设置KimiAI-key");
            }
            for (String s : reduce_key) {
                keyUsingMap.add(s);
            }
        }
        if (!expire_key.isEmpty()){
            keyUsingMap.removeIf(v->expire_key.contains(v));
        }
        String t1 = "";
        synchronized (keyUsingMap) {
            t1 = keyUsingMap.poll();
            keyUsingMap.offer(t1);
        }
        return t1;
    }

}
