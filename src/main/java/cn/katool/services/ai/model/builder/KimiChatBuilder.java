package cn.katool.services.ai.model.builder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Method;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KimiChatBuilder extends KimiCommonBuilder {
    KimiBuilder kimiBuilder;


    public KimiBuilderBase completions(){
        KimiBuilder resolve = (KimiBuilder) this.resolve(kimiBuilder);
        return new KimiBuilderBase(resolve);
    }


}
