package cn.katool.services.ai.model.dto.kimi.cache;
import cn.katool.services.ai.model.dto.kimi.base.KimiBaseRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.util.HashMap;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class KimiCacheRequest extends KimiBaseRequest {
    String name;
    String description;
    HashMap<String,String> metadata;
    Integer expire_at;
    Integer ttl;
}
