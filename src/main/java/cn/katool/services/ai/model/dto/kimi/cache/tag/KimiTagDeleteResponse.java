package cn.katool.services.ai.model.dto.kimi.cache.tag;
import cn.katool.services.ai.model.dto.kimi.base.KimiBaseDeleteResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class KimiTagDeleteResponse extends KimiBaseDeleteResponse {
    String tag;
}
