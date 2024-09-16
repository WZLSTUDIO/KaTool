package cn.katool.Exception;
import lombok.Data;
public enum ErrorCode {
    PARAMS_ERROR(40000, "参数错误"),
    FILE_ERROR(50000, "文件错误"),
    OPER_ERROR(60000,"操作错误");
    private int code;
    private String message;
    public int getCode() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    ErrorCode(int code, String message){
            this.code=code;
            this.message=message;
    }
}
