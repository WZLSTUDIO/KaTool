package cn.katool.services.ai.model.builder.kimi;
import cn.katool.services.ai.CommonAIService;
import cn.katool.services.ai.model.entity.ErrorMessage;
import cn.katool.services.ai.model.entity.kimi.Kimi;
import cn.katool.util.AiServiceHttpUtil;

import java.util.Map;

public class KimiBuilderBase extends KimiDefaultBuilder{
    public KimiBuilderBase(KimiBuilder kimiBuilder) {
        super(kimiBuilder);
    }
    public KimiBuilderBase() {
    }
    public Kimi build(AiServiceHttpUtil httpUtil) {
        return kimiBuilder.build(httpUtil);
    }

    public Kimi build(AiServiceHttpUtil httpUtil,Map<String, String> cacheHeaders) {
        return kimiBuilder.build(httpUtil,cacheHeaders);
    }
}
