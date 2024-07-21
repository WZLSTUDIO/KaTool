package cn.katool.services.ai.model.builder;

import cn.hutool.core.bean.BeanUtil;
import cn.katool.services.ai.model.entity.Kimi;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
        return (KimiCacheRefsBuilder) this.resolve(kimiBuilder);
    }
}
