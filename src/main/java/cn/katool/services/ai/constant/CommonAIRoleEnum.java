package cn.katool.services.ai.constant;
public enum CommonAIRoleEnum {
    SYS("system"),
    USER("user"),
    ASSISTANT("assistant"),
    TOOL("tool"),
    ;
    private String role;
    CommonAIRoleEnum(String role) {
        this.role = role;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
}
