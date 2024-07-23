package cn.katool.services.ai.model.builder.kimi;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KimiFilesBuilder extends KimiBuilderBase{

    public KimiFilesBuilder(KimiBuilder kimiBuilder) {
        super(kimiBuilder);
    }

    public KimiFilesContentBuilder id(String cacheId){
        KimiBuilder resBuilder = this.devailParam(kimiBuilder, cacheId);
        return new KimiFilesContentBuilder(resBuilder);
    }

}
