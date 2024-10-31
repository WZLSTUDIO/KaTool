package cn.katool.services.ai.acl.kimi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class KimiGsonFactory {
    public static Gson create(){
        GsonBuilder resBuilder = new GsonBuilder();
        resBuilder.registerTypeAdapter(KimiToolParametersSerializer.class,new KimiToolParametersSerializer());
        resBuilder.registerTypeAdapter(KimiToolParametersPropertiesValueSerializer.class,new KimiToolParametersPropertiesValueSerializer());
        return resBuilder.setPrettyPrinting().create();
    }
}
