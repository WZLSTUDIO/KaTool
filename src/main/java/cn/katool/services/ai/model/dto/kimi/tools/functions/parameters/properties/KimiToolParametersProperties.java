package cn.katool.services.ai.model.dto.kimi.tools.functions.parameters.properties;


import cn.katool.services.ai.model.dto.kimi.tools.functions.parameters.properties.inner.KimiToolParametersPropertiesLanguage;
import cn.katool.services.ai.model.dto.kimi.tools.functions.parameters.properties.inner.KimiToolParametersPropertiesValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class KimiToolParametersProperties {
    KimiToolParametersPropertiesLanguage language;

    String key;
    KimiToolParametersPropertiesValue value;
}
