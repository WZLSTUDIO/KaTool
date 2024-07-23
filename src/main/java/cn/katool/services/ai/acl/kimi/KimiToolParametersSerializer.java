package cn.katool.services.ai.acl.kimi;

import cn.katool.services.ai.model.dto.kimi.tools.functions.parameters.KimiToolParameters;
import cn.katool.services.ai.model.dto.kimi.tools.functions.parameters.properties.inner.KimiToolParametersPropertiesValue;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class KimiToolParametersSerializer  implements JsonSerializer<KimiToolParameters> {



    @Override
    public JsonElement serialize(KimiToolParameters kimiToolParameters, Type type, JsonSerializationContext jsonSerializationContext) {
        String types = kimiToolParameters.getType();
        List<String> required = kimiToolParameters.getRequired();
        Map<String, KimiToolParametersPropertiesValue> properties = kimiToolParameters.getProperties();

        JsonObject res = new JsonObject();
        res.add("type",new JsonPrimitive(types));
        res.add("required",jsonSerializationContext.serialize(required));
        JsonObject propertiesJson = new JsonObject();
        properties.forEach((key, value) -> {
            JsonElement element = new KimiToolParametersPropertiesValueSerializer().serialize(value, TypeToken.get(KimiToolParametersPropertiesValue.class).getType(), jsonSerializationContext);
            propertiesJson.add(key, element);
        });
        res.add("properties", propertiesJson);
        return res;
    }
}