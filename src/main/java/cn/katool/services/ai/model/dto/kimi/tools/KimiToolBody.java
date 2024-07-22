package cn.katool.services.ai.model.dto.kimi.tools;

import cn.katool.services.ai.constant.kimi.KimiToolType;
import cn.katool.services.ai.model.dto.kimi.tools.functions.KimiFunctionBody;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class KimiToolBody {
    String type = KimiToolType.FUNCTION;
    KimiFunctionBody function;
}
