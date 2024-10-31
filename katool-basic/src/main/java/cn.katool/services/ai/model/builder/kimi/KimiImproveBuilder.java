package cn.katool.services.ai.model.builder.kimi;
import cn.katool.services.ai.model.entity.kimi.Kimi;
import cn.katool.util.AiServiceHttpUtil;

import java.util.Map;

public class KimiImproveBuilder extends KimiDefaultBuilder{
    public KimiImproveBuilder(KimiBuilder kimiBuilder) {
        super(kimiBuilder);
    }
    public KimiImproveBuilder() {
    }
    public Kimi build(AiServiceHttpUtil httpUtil) {
        return kimiBuilder.build(httpUtil);
    }

    public Kimi build(AiServiceHttpUtil httpUtil,Map<String, String> cacheHeaders) {
        return kimiBuilder.build(httpUtil,cacheHeaders);
    }
}
