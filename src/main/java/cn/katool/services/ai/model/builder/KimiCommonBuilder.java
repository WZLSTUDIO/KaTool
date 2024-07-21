package cn.katool.services.ai.model.builder;

import com.alibaba.excel.util.StringUtils;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class KimiCommonBuilder {

    protected KimiBuilder devailParam(KimiBuilder builder,Object value){
        KimiBuilder res =KimiBuilder.create();
        BeanUtils.copyProperties(builder,res);
        String url = builder.getUrl();
        if(url.charAt(url.length()-1) != '/'){
            url += "/";
        }
        url += value;
        res.setUrl(url);
        return res;
    }

    protected KimiBuilder devailArgAtrWithMethodName(KimiBuilder builder,Object value){
        return devailArgAtrWithMethodName(builder,null,value);
    }

    protected KimiBuilder devailArgAtrWithMethodName(KimiBuilder builder,String key,Object value){
        String argName = Thread.currentThread().getStackTrace()[2].getClassName();
        String url = builder.getUrl();
        if (url.lastIndexOf("?") > 0) {
            url += "&";
        } else {
            url += "?";
        }
        if (StringUtils.isBlank(key)){
            url += argName + "=" + value;
        }
        else {
            url += argName+ "[" +key+ "]" + "=" + value;
        }
        KimiBuilder kimiBuilder = KimiBuilder.create();
        BeanUtils.copyProperties(builder,kimiBuilder);
        kimiBuilder.setUrl(url);
        return kimiBuilder;
    }

    protected KimiCommonBuilder resolve(KimiBuilder builder){
        Object invoke = builder;
        try {
            Method method = KimiBuilder.class.getMethod(Thread.currentThread().getStackTrace()[2].getClassName());
            method.setAccessible(true);
            invoke = method.invoke(builder);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return (KimiCommonBuilder) invoke;
    }




}
