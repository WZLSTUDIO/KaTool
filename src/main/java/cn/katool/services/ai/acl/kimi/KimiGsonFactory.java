package cn.katool.services.ai.acl.kimi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;


public class KimiGsonFactory {
    public static Gson create(){
        GsonBuilder resBuilder = new GsonBuilder();
        resBuilder.registerTypeAdapter(KimiToolParametersPropertiesGsonSerializer.class,new KimiToolParametersPropertiesGsonSerializer());
        return resBuilder.setPrettyPrinting().create();
    }
}
