package cn.katool.services.ai.model.dto.kimi.cache.tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.util.Date;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class KimiCacheTagMeta {
    private String cache_id;
    private Date created_at;
    private String object;
    private String owned_by;
    private String tag;
}
