package cn.katool.common;

import lombok.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@NoArgsConstructor
public class SessionPackageTheadLocalAdaptor<T> {

    @Setter
    @Getter
    volatile Boolean multi = false;
    volatile T value;
    CopyOnTransmittableThreadLocal<T> threadLocal;

    @SneakyThrows
    public SessionPackageTheadLocalAdaptor(Boolean multi) {
        this.multi = multi;
        if (multi) {
            this.threadLocal = new CopyOnTransmittableThreadLocal<T>(){
                @Override
                @SneakyThrows
                protected T initialValue() {
                    // 对T的初始化，避免空指针
                    Type genericSuperclass = this.getClass().getGenericSuperclass();
                    T res = null;
                    if (genericSuperclass instanceof ParameterizedType){
                        Type actualTypeArgument = ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
                        res = ((Class<T>) actualTypeArgument).newInstance();
                    }
                    return res;
                }
            };
        } else {
            Type genericSuperclass = this.getClass().getGenericSuperclass();
            T res = null;
            if (genericSuperclass instanceof ParameterizedType){
                Type actualTypeArgument = ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
                res = ((Class<T>) actualTypeArgument).newInstance();
            }
            this.value = res;
        }
    }

    public SessionPackageTheadLocalAdaptor set(T value) {
        if (multi) {
            threadLocal.set(value);
        } else {
            this.value = value;
        }
        return this;
    }

    public T get() {
        if (multi) {
            return threadLocal.get();
        } else {
            return value;
        }
    }
    public void remove() {
        if (multi) {
            threadLocal.remove();
        }
    }
}
