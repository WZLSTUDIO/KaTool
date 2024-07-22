package cn.katool.services.ai.model.dto.kimi.tools.functions.parameters;

import cn.katool.services.ai.model.dto.kimi.tools.functions.parameters.properties.KimiToolParametersProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class KimiToolParameters {
    KimiToolParametersProperties properties;
    String type = "object";
}
