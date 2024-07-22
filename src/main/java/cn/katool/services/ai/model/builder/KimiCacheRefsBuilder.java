package cn.katool.services.ai.model.builder;

import cn.katool.services.ai.model.entity.Kimi;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class KimiCacheRefsBuilder extends KimiBuilderBase {
    public KimiCacheRefsBuilder() {
    }

    public KimiCacheRefsBuilder(KimiBuilder kimiBuilder) {
        super(kimiBuilder);
    }

    public KimiCacheRefsTagsBuilder tags() {
        return (KimiCacheRefsTagsBuilder) this.resolve(kimiBuilder,KimiCacheRefsTagsBuilder.class);
    }

}
