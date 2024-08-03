package cn.katool.services.ai.constant.kimi;
public enum KimiResponseFormatEnum {
    TEXT("text"),
    DEFAULT("text"), // 默认值，用于未知格式
    JSON("json_object");
    String type;
    KimiResponseFormatEnum(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String toJson(){
        return "{\"type\":\"" + this.type + "\"}";
    }
}
