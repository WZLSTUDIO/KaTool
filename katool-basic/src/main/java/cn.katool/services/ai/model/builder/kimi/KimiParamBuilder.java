package cn.katool.services.ai.model.builder.kimi;
public class KimiParamBuilder extends  KimiBuilderBase{
    KimiBuilder builder;
    public KimiParamBuilder(KimiBuilder builder) {
        this.builder = builder;
    }
    public KimiBuilderBase content(){
        KimiBuilder resolve = (KimiBuilder) this.resolve(builder);
        return new KimiBuilderBase(resolve);
    }
}
