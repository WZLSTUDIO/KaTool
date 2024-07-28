package cn.katool.services.ai.model.builder.kimi;
import cn.katool.services.ai.CommonAIService;
import cn.katool.services.ai.model.entity.ErrorMessage;
import cn.katool.services.ai.model.entity.kimi.Kimi;
import com.alibaba.excel.util.StringUtils;
import cn.hutool.core.bean.BeanUtil;;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class KimiCommonBuilder {
    protected KimiBuilder devailParam(KimiBuilder builder,Object value){
        KimiBuilder res =KimiBuilder.create();
        BeanUtil.copyProperties(builder,res);
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
        BeanUtil.copyProperties(builder,kimiBuilder);
        kimiBuilder.setUrl(url);
        return kimiBuilder;
    }
    protected Object resolve(KimiBuilder builder){
        Object invoke = builder;
        try {
            Method method = KimiBuilder.class.getDeclaredMethod(Thread.currentThread().getStackTrace()[2].getMethodName());
            method.setAccessible(true);
            invoke = method.invoke(builder);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return invoke;
    }
    protected <T extends KimiDefaultBuilder> T resolve(KimiBuilder builder,Class<T> clazz){
        Object invoke = builder;
        T t;
        try {
            Method method = KimiBuilder.class.getDeclaredMethod(Thread.currentThread().getStackTrace()[2].getMethodName());
            method.setAccessible(true);
            invoke = method.invoke(builder);
            t = clazz.newInstance();
            t.kimiBuilder = (KimiBuilder) invoke;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
        return t;
    }

}
