package cn.katool.services.ai.model.builder.kimi;
public class KimiParamBuilder extends KimiImproveBuilder {
    KimiBuilder builder;
    public KimiParamBuilder(KimiBuilder builder) {
        this.builder = builder;
    }
    public KimiImproveBuilder content(){
        KimiBuilder resolve = (KimiBuilder) this.resolve(builder);
        return new KimiImproveBuilder(resolve);
    }
}
