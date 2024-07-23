package cn.katool.common;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.SneakyThrows;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class CopyOnTransmittableThreadLocal<T> extends TransmittableThreadLocal<T> {
    @SneakyThrows
    @Override
    public T copy(T parentValue) {

        // 获取泛型类别
        // 反射创建对象
        Type genericSuperclass = this.getClass().getGenericSuperclass();
        T res = null;
        if (genericSuperclass instanceof ParameterizedType){
            Type actualTypeArgument = ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
            res = ((Class<T>) actualTypeArgument).newInstance();
        }
        BeanUtil.copyProperties(parentValue, res);
        return res;
    }
}
