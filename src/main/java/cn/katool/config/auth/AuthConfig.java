package cn.katool.config.auth;
import cn.katool.config.common.KaToolInit;
import cn.katool.constant.AuthConstant;
import cn.katool.util.auth.AuthUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@ComponentScan("cn.katool.*")
@Configuration("AuthConfig")
@ConfigurationProperties("katool.util.auth")
public class AuthConfig{
    private long expTime = 3600 * 24 * 7 * 1000;
    private String saltKey =  "katool.salt.version::" + KaToolInit.version;
    private String tokenHeader="Authorization";
    @Bean
    public void initAuthUtils(){
        AuthUtil.setExpireTime(getExpTime());
        AuthUtil.setSaltKey(getSaltKey());
        AuthConstant.TOKEN_HEADER=getTokenHeader();
    }
}
