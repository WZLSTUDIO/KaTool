package cn.katool.common;
@FunctionalInterface
public interface MethodInterface<T> {
    T apply();
    default MethodInterface<T> andThen(MethodInterface<T> methodInterface){
        return ()->{
            apply();
            return methodInterface.apply();
        };
    }
}