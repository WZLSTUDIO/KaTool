package cn.katool.services.ai.model.dto.kimi.tools;

import cn.katool.services.ai.constant.kimi.KimiToolType;
import cn.katool.services.ai.model.dto.kimi.tools.functions.KimiFunctionBody;

public class KimiToolBody {
    String type = KimiToolType.FUNCTION;
    KimiFunctionBody function;
}
