package cn.katool.services.ai.model.builder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KimiFilesBuilder extends KimiBuilderBase{

    public KimiFilesBuilder(KimiBuilder kimiBuilder) {
        super(kimiBuilder);
    }

    public KimiBuilderBase id(String cacheId){
        KimiBuilder resBuilder = this.devailParam(kimiBuilder, cacheId);
        return new KimiBuilderBase(resBuilder);
    }

}
