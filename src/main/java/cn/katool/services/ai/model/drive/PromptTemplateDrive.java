package cn.katool.services.ai.model.drive;

import cn.katool.services.ai.constant.CommonAIRoleEnum;
import cn.katool.services.ai.model.entity.CommonAIMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Data
public class PromptTemplateDrive {
    public PromptTemplateDrive() {
    }
    public PromptTemplateDrive(String template) {
        this.template = template;
    }



    private PromptTemplateDrive(String template, Map<String, String> insteadMapping) {
        this.template = template;
        this.insteadMapping = insteadMapping;
    }

    String template;


    Map<String,String> insteadMapping;

    public static PromptTemplateDrive create(String template,Map<String,String> insteadMapping){
        return new PromptTemplateDrive(template,insteadMapping);
    }

    public static PromptTemplateDrive create(String template){
        return new PromptTemplateDrive(template);
    }

    public CommonAIMessage generateTemplate(){
        AtomicReference<String> res= new AtomicReference<>(template);
        if (null != this.insteadMapping && !this.insteadMapping.isEmpty()) {
            this.insteadMapping.entrySet().forEach(item -> res.set(StringUtils.replace(this.template, "${" + item.getKey() + "}", item.getValue())));
        }
        return new CommonAIMessage(CommonAIRoleEnum.SYS, res.get());
    }

}
