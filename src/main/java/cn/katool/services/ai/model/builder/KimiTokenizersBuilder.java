package cn.katool.services.ai.model.builder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KimiTokenizersBuilder extends KimiBuilderBase {

    KimiBuilder kimiBuilder;

    private KimiBuilder estimate_token_count(){
        return (KimiBuilder) this.resolve(kimiBuilder);
    }

    public KimiBuilder estimateTokenCount(){
        return this.estimate_token_count();
    }
}
