package cn.katool.services.ai.acl.kimi;

import cn.katool.services.ai.model.dto.kimi.tools.functions.parameters.properties.KimiToolParametersProperties;
import cn.katool.services.ai.model.dto.kimi.tools.functions.parameters.properties.inner.KimiToolParametersPropertiesLanguage;
import cn.katool.services.ai.model.dto.kimi.tools.functions.parameters.properties.inner.KimiToolParametersPropertiesValue;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.StringJoiner;

public class KimiToolParametersPropertiesGsonSerializer implements JsonSerializer<KimiToolParametersProperties> {

    public static final TypeToken KimiToolParametersPropertiesGsonSerializerTypeToken = TypeToken.get(KimiToolParametersPropertiesGsonSerializer.class);

    private void resolveEnums(JsonObject res, KimiToolParametersPropertiesLanguage kimiToolParametersPropertiesLanguage) {
        if (kimiToolParametersPropertiesLanguage == null) {
            return;
        }
        JsonObject jsonObject = new JsonObject();
        List<String> enums = kimiToolParametersPropertiesLanguage.getEnums();
        jsonObject.add("type",new JsonPrimitive(kimiToolParametersPropertiesLanguage.getType()));
        JsonArray jsonElements = new JsonArray();
        enums.forEach(jsonElements::add);
        jsonObject.add("enum",jsonElements);
        res.add("language",jsonObject);
    }

    @Override
    public JsonElement serialize(KimiToolParametersProperties KimiToolParametersProperties, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject res = new JsonObject();
        KimiToolParametersPropertiesLanguage language = KimiToolParametersProperties.getLanguage();
        resolveEnums(res, language);
        KimiToolParametersPropertiesValue value = KimiToolParametersProperties.getValue();
        JsonObject valueJsonObject = new JsonObject();
        valueJsonObject.add("type",new JsonPrimitive(value.getType()));
        valueJsonObject.add("description",new JsonPrimitive(value.getDescription()));
        res.add(KimiToolParametersProperties.getKey(), valueJsonObject);
        return res;
    }
}
