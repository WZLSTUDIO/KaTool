package cn.katool.services.ai.model.builder.kimi;
public class KimiCacheBuilderInterface extends KimiImproveBuilder {
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
