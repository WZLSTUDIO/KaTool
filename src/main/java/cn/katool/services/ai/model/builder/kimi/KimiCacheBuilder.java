package cn.katool.services.ai.model.builder.kimi;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KimiCacheBuilder extends KimiCacheBuilderInterface {


    public KimiCacheBuilder(KimiBuilder kimiBuilder) {
        this.kimiBuilder = kimiBuilder;
    }



    public KimiBuilder id(String cacheId){
        return this.devailParam(kimiBuilder,cacheId);
    }

    public KimiCacheBuilder metadata(String biz_id,String value){
        this.devailArgAtrWithMethodName(kimiBuilder,biz_id,value);
        return this;
    }

    public KimiCacheRefsBuilder refs(){
        return this.resolve(kimiBuilder,KimiCacheRefsBuilder.class);
    }
}
