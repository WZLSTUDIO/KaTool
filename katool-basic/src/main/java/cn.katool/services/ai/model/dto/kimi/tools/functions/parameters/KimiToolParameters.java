package cn.katool.services.ai.model.dto.kimi.tools.functions.parameters;
import cn.katool.services.ai.acl.kimi.KimiToolParametersPropertiesValueSerializer;
import cn.katool.services.ai.model.dto.kimi.tools.functions.parameters.properties.inner.KimiToolParametersPropertiesValue;
import com.google.gson.annotations.JsonAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class KimiToolParameters {
    @JsonAdapter(KimiToolParametersPropertiesValueSerializer.class)
    Map<String, KimiToolParametersPropertiesValue> properties;
    String type = "object";
    List<String> required = new ArrayList<>();
}
