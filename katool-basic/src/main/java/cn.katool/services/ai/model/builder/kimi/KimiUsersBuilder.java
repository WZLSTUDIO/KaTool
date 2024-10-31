package cn.katool.services.ai.model.builder.kimi;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KimiUsersBuilder extends KimiImproveBuilder {
    KimiBuilder kimiBuilder;
    public KimiMeBuilder me(){
        return this.resolve(kimiBuilder,KimiMeBuilder.class);
    }
}
