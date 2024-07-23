package cn.katool.services.ai.model.builder.kimi;
import lombok.Data;
@Data
public class KimiMeBuilder extends KimiDefaultBuilder {
    public KimiMeBuilder(KimiBuilder kimiBuilder) {
        super(kimiBuilder);
    }
    public KimiMeBuilder() {
    }
    public KimiBuilderBase balance(){
        KimiBuilder resolve = (KimiBuilder) this.resolve(kimiBuilder);
        return new KimiBuilderBase(resolve);
    }
}