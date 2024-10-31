package cn.katool.services.ai.acl.kimi;
import cn.katool.services.ai.model.dto.kimi.tools.functions.parameters.properties.inner.KimiToolParametersPropertiesValue;
import com.google.gson.*;

import java.lang.reflect.Type;

public class KimiToolParametersPropertiesValueSerializer implements JsonSerializer<KimiToolParametersPropertiesValue> {
    @Override
    public JsonElement serialize(KimiToolParametersPropertiesValue kimiToolParametersProperties, Type type, JsonSerializationContext jsonSerializationContext) {
        String types = kimiToolParametersProperties.getType();
        String key = (String) kimiToolParametersProperties.getSchema().getKey();
        Object value = kimiToolParametersProperties.getSchema().getValue();
        JsonObject res = new JsonObject();
        res.add("type",new JsonPrimitive(types));
        if (value instanceof String){
            res.add(key,new JsonPrimitive(value.toString()));
        }
        else {
            res.add(key, jsonSerializationContext.serialize(value));
        }
        return res;
    }
}
