package cn.katool.services.ai.model.builder.kimi;

import lombok.Data;

@Data
public class KimiTokenizersBuilder extends KimiBuilderBase {
    public KimiTokenizersBuilder(KimiBuilder kimiBuilder) {
        super(kimiBuilder);
    }

    public KimiTokenizersBuilder() {
    }

    private KimiBuilder estimate_token_count(){
        return (KimiBuilder) this.resolve(kimiBuilder);
    }

    public KimiBuilder estimateTokenCount(){
        return this.estimate_token_count();
    }
}
