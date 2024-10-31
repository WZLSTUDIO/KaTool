package cn.katool.services.ai.model.dto.kimi.cache.tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
public class KimiCacheTagCreateRequest {
    String tag;
    String cache_id;
}
