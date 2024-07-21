package cn.katool.services.ai.model.builder;

import cn.katool.services.ai.model.entity.Kimi;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class KimiBuilderBase extends KimiCommonBuilder{
    KimiBuilder kimiBuilder;
    public Kimi build() {
        return kimiBuilder.build();
    }
}
