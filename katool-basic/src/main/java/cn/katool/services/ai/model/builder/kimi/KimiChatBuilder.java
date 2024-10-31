package cn.katool.services.ai.model.builder.kimi;
import cn.katool.services.ai.model.builder.CommonApiBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KimiChatBuilder extends CommonApiBuilder {
    KimiBuilder kimiBuilder;
    public KimiImproveBuilder completions(){
        KimiBuilder resolve = (KimiBuilder) this.resolve(kimiBuilder);
        return new KimiImproveBuilder(resolve);
    }
}
