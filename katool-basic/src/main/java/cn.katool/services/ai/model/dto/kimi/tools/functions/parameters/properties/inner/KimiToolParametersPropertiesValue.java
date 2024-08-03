package cn.katool.services.ai.model.dto.kimi.tools.functions.parameters.properties.inner;
import cn.hutool.core.lang.Pair;
import cn.katool.services.ai.constant.kimi.KimiToolType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class KimiToolParametersPropertiesValue<T> {
    String type= KimiToolType.STRING;
    Pair<String,T> schema;
    public KimiToolParametersPropertiesValue<T> setSchema(String parma, T decription) {
        this.schema = Pair.of(parma, decription);
        return this;
    }
}
