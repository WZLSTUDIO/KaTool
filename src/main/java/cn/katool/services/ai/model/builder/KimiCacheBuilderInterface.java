package cn.katool.services.ai.model.builder;

import cn.katool.services.ai.model.entity.Kimi;

public class KimiCacheBuilderInterface extends KimiBuilderBase {


    public KimiCacheBuilderInterface limit(Integer limit){
        this.devailArgAtrWithMethodName(kimiBuilder, limit);
        return this;
    }

    public KimiCacheBuilderInterface order(boolean isAsc){
        this.devailArgAtrWithMethodName(kimiBuilder, isAsc ?"asc" : "desc");
        return this;
    }

    public KimiCacheBuilderInterface after(String idOrTag){
        this.devailArgAtrWithMethodName(kimiBuilder, idOrTag);
        return this;
    }
    public KimiCacheBuilderInterface before(String idOrTag){
        this.devailArgAtrWithMethodName(kimiBuilder, idOrTag);
        return this;
    }

}
