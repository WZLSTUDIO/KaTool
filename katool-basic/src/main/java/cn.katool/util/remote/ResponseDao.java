package cn.katool.util.remote;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ResponseDao<T> {
    private Class<T> clazz;
    public Integer code;
    public String msg;

    public T data;
    public ResponseDao(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public ResponseDao(T data) {
        this.data = data;
    }
    public ResponseDao(int code, T data, String msg) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    public static <T> ResponseDao<T> success(T data) {
        return new ResponseDao<>(data);
    }

    public static ResponseDao success() {
        return new ResponseDao(0,"success");
    }

    public static ResponseDao<String> error(Exception e) {
        return new ResponseDao<String>(20000,e.getMessage(),"系统异常，建议重启应用再试！");
    }
    public static ResponseDao<String> error(String e) {
        return new ResponseDao<String>(20000,e);
    }
    public static ResponseDao<String> error(Integer code,String e) {
        return new ResponseDao<String>(code,e);
    }

    public static <T> ResponseDao<T> fail(T obj) {
        return new ResponseDao<T>(21350,obj,"fail");
    }
    public static <T> ResponseDao<T> fail(T obj,String msg) {
        return new ResponseDao<T>(21350,obj,msg);
    }
    public static <T> ResponseDao<T> fail(Integer code,T obj,String msg) {
        return new ResponseDao<T>(code,obj,msg);
    }
}
