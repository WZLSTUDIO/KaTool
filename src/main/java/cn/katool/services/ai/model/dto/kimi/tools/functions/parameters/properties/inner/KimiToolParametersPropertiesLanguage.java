package cn.katool.services.ai.model.dto.kimi.tools.functions.parameters.properties.inner;

import cn.katool.services.ai.constant.kimi.KimiToolType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class KimiToolParametersPropertiesLanguage {
    String type= KimiToolType.STRING;
    List<String> enums= Arrays.asList(KimiToolLangauge.PYTHON, KimiToolLangauge.JAVA);
}
