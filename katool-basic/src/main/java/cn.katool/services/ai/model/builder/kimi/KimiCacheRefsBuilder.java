package cn.katool.services.ai.model.builder.kimi;
import lombok.Data;
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
