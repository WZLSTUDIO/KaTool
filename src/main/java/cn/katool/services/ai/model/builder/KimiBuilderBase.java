package cn.katool.services.ai.model.builder;

import cn.katool.services.ai.model.entity.Kimi;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


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
