package cn.katool.services.ai.model.builder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KimiMeBuilder extends KimiCommonBuilder {
    KimiBuilder kimiBuilder;

    public KimiBuilderBase balance(){
        KimiBuilder resolve = (KimiBuilder) this.resolve(kimiBuilder);
        return new KimiBuilderBase(resolve);
    }
}