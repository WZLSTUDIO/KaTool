package cn.katool.services.ai.model.dto.kimi.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class KimiBaseDeleteResponse {
    Boolean deleted;
    String object;
}
