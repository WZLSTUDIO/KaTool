package cn.katool.services.ai.model.builder.kimi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KimiChatBuilder extends KimiCommonBuilder {
    KimiBuilder kimiBuilder;


    public KimiBuilderBase completions(){
        KimiBuilder resolve = (KimiBuilder) this.resolve(kimiBuilder);
        return new KimiBuilderBase(resolve);
    }


}
