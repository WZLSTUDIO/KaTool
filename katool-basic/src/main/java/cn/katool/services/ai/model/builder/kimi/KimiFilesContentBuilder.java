package cn.katool.services.ai.model.builder.kimi;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
public class KimiFilesContentBuilder extends KimiImproveBuilder {
    public KimiFilesContentBuilder(KimiBuilder kimiBuilder) {
        super(kimiBuilder);
    }
    public KimiBuilder content(){
        return (KimiBuilder) this.resolve(kimiBuilder);
    }
}
