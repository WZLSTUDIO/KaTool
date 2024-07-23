/**
 * Title
 *
 * @ClassName: qQiniuConfig
 * @Description: 七牛云配置
 * @author: Karos
 * @date: 2022/10/19 8:32
 * @Blog: https://www.wzl1.top/
 */
package cn.katool.config.store;
import com.qiniu.common.Zone;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Component;
@Configuration("ALiYunConfig")
@Component
@ToString
@ConfigurationProperties("katool.util.store.aliyun")
@AllArgsConstructor
@NoArgsConstructor
@Data
@ComponentScan("cn.katool.*")
@Scope("singleton")
public class AliyunConfig {
    /**
     * ak
     */
    private String accessKey="******";
    /**
     * sk
     */
    private String secretKey="******";
    /**
     * 所属空间
     */
    private String zone="huabei";
    /**
     * 基础上传目录
     */
    private String basedir="/katool";
    /**
     * 配置空间的存储区域
     * <p>
     * 注意所有Bean首字母小写后，昵称不能一样
     */
}