package cn.katool.services.ai.model.builder.kimi;

import cn.katool.services.ai.model.entity.kimi.Kimi;


public class KimiBuilderBase extends KimiDefaultBuilder{
    public KimiBuilderBase(KimiBuilder kimiBuilder) {
        super(kimiBuilder);
    }

    public KimiBuilderBase() {
    }

    public Kimi build() {
        return kimiBuilder.build();
    }
}
