package cn.katool.services.ai.model.entity;

import cn.katool.services.ai.constant.CommonAIRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonAIMessage {
    private String role;
    private String content;
    private Boolean partial = false;

    public CommonAIMessage(String role, String message) {
        this.role = role;
        this.content = message;
    }

    public CommonAIMessage(CommonAIRoleEnum enume, String message) {
        this.role = enume.getRole();
        this.content = message;
    }
}
