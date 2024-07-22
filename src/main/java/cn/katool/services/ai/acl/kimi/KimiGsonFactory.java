package cn.katool.services.ai.acl.kimi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;


public class KimiGsonFactory {
    public static Gson create(TypeToken ... types){
        GsonBuilder resBuilder = new GsonBuilder();
        for (TypeToken type : types) {
            try {
                resBuilder.registerTypeAdapter(type.getType(), type.getRawType().newInstance());
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return resBuilder.create();
    }

    public static Gson create(){
        return create(KimiToolParametersPropertiesGsonSerializer.KimiToolParametersPropertiesGsonSerializerTypeToken);
    }
}
