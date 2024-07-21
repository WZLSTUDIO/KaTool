package cn.katool.services.ai.acl.kimi;

import cn.katool.services.ai.model.dto.kimi.tools.functions.parameters.properties.KimiToolParametersProperties;
import cn.katool.services.ai.model.dto.kimi.tools.functions.parameters.properties.inner.KimiToolParametersPropertiesLanguage;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.StringJoiner;

public class KimiToolsLanguageEnumGsonSerializer implements JsonSerializer<KimiToolParametersProperties> {

    public static final TypeToken KimiToolsLanguageEnumGsonSerializerTypeToken = new TypeToken<KimiToolParametersPropertiesLanguage>() {};

    private void resolveEnums(JsonObject res, KimiToolParametersPropertiesLanguage kimiToolParametersPropertiesLanguage) {
        JsonObject jsonObject = new JsonObject();
        List<String> enums = kimiToolParametersPropertiesLanguage.getEnums();
        jsonObject.add("type",new JsonPrimitive(kimiToolParametersPropertiesLanguage.getType()));
        StringJoiner enumWrapper = new StringJoiner(",", "[", "]");
        enums.forEach(enumWrapper::add);
        jsonObject.add("enum",new JsonPrimitive(enumWrapper.toString()));
        res.add("language",jsonObject);
    }

    @Override
    public JsonElement serialize(KimiToolParametersProperties KimiToolParametersProperties, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject res = new JsonObject();
        KimiToolParametersPropertiesLanguage language = KimiToolParametersProperties.getLanguage();
        resolveEnums(res, language);
        res.add(KimiToolParametersProperties.getKey(), new JsonPrimitive(new Gson().toJson(KimiToolParametersProperties.getValue())));
        return res;
    }
}
