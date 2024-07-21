package cn.katool.services.ai.model.dto.kimi.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class KimiCacheResponse {
    String id;
    String status;
    String object;
    String created_at;
    String expired_at;
    String tokens;
    KimiCacheErrorMsg error;
}
