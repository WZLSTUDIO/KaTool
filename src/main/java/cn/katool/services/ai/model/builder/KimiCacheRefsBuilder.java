package cn.katool.services.ai.model.builder;

import cn.katool.services.ai.model.entity.Kimi;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KimiCacheRefsBuilder extends KimiBuilderBase {

    KimiBuilder kimiBuilder;



    public KimiCacheRefsTagsBuilder tags() {
        return (KimiCacheRefsTagsBuilder) this.resolve(kimiBuilder);
    }

}
