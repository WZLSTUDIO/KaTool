package cn.katool.services.ai.model.builder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
