package cn.katool.services.ai.common;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;
import java.util.Map;
import java.util.function.Function;
public class KimiTuples {
    public static Tuple3<String,String, Function<Map<String, String>, String>> of(String functionName, String functionDescription, Function<Map<String, String>, String> function){
        Tuple3<String, String, Function<Map<String, String>, String>> of = Tuples.of(functionName, functionDescription, function);
        return of;
    }
    public static <T> Tuple3<String,String,T> of(String paramsName,String paramsSchemaKey,T paramsSchemaValue){
        return Tuples.of(paramsName,paramsSchemaKey,paramsSchemaValue);
    }
    public static <T> Tuple4<String,String,T,String> of(String paramsName, String paramsSchemaKey, T paramsSchemaValue, String paramsType){
        return Tuples.of(paramsName,paramsSchemaKey,paramsSchemaValue,paramsType);
    }
}
