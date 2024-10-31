package cn.katool.services.ai.model.entity;
import cn.katool.services.ai.constant.CommonAIRoleEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class CommonAIMessage {
    private String role;
    private String content;
    public CommonAIMessage(String role, String message) {
        this.role = role;
        this.content = message;
    }
    public CommonAIMessage(CommonAIRoleEnum enume, String message) {
        this.role = enume.getRole();
        this.content = message;
    }
}
