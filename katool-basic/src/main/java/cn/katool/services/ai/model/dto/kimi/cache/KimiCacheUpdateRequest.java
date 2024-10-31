package cn.katool.services.ai.model.dto.kimi.cache;
import cn.katool.services.ai.model.entity.RequestBody;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.util.HashMap;
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
public class KimiCacheUpdateRequest implements RequestBody {
    String id;
    KimiCacheUpdateRequestMeta meta;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    @Accessors(chain = true)
    static public class KimiCacheUpdateRequestMeta{
        HashMap<String,String> metadata;
        Integer expire_at;
        Integer ttl;
    }
}
