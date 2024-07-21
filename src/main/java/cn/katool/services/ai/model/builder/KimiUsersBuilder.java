package cn.katool.services.ai.model.builder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KimiUsersBuilder extends KimiBuilderBase {

    KimiBuilder kimiBuilder;

    public KimiMeBuilder me(){
        return (KimiMeBuilder) this.resolve(kimiBuilder);
    }
}


