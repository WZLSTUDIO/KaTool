package cn.katool.services.ai.acl.kimi;
import cn.katool.services.ai.model.dto.kimi.tools.functions.parameters.KimiToolParameters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
public class KimiGsonFactory {
    public static Gson create(){
        GsonBuilder resBuilder = new GsonBuilder();
        resBuilder.registerTypeAdapter(KimiToolParametersSerializer.class,new KimiToolParametersSerializer());
        resBuilder.registerTypeAdapter(KimiToolParametersPropertiesValueSerializer.class,new KimiToolParametersPropertiesValueSerializer());
        return resBuilder.setPrettyPrinting().create();
    }
}
