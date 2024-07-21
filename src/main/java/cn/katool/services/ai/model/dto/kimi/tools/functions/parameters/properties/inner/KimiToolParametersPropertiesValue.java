package cn.katool.services.ai.model.dto.kimi.tools.functions.parameters.properties.inner;

import cn.katool.services.ai.constant.kimi.KimiToolType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class KimiToolParametersPropertiesValue {
    String type= KimiToolType.STRING;
    String description;
}
