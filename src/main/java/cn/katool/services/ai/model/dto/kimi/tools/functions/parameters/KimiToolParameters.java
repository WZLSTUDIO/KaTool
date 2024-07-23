package cn.katool.services.ai.model.dto.kimi.tools.functions.parameters;

import cn.katool.services.ai.acl.kimi.KimiToolParametersPropertiesGsonSerializer;
import cn.katool.services.ai.model.dto.kimi.tools.functions.parameters.properties.KimiToolParametersProperties;
import com.google.gson.annotations.JsonAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class KimiToolParameters {

    KimiToolParametersProperties properties;
    String type = "object";
    List<String> required = new ArrayList<>();
}
