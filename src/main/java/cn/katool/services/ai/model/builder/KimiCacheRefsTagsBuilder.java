package cn.katool.services.ai.model.builder;

import cn.katool.services.ai.model.entity.Kimi;

public class KimiCacheRefsTagsBuilder extends KimiCacheBuilderInterface{
    public KimiCacheRefsTagsBuilder(KimiBuilder kimiBuilder) {
        this.kimiBuilder = kimiBuilder;
    }

    public KimiParamBuilder tagName(String yourTagName){
        KimiBuilder resBuild = this.devailParam(kimiBuilder, yourTagName);
        return new KimiParamBuilder(resBuild);
    }




}
