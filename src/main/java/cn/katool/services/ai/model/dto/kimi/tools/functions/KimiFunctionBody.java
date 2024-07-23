package cn.katool.services.ai.model.dto.kimi.tools.functions;
import cn.katool.services.ai.acl.kimi.KimiToolParametersSerializer;
import cn.katool.services.ai.model.dto.kimi.tools.functions.parameters.KimiToolParameters;
import com.google.gson.annotations.JsonAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class KimiFunctionBody {
    String name;
    String description;
    @JsonAdapter(KimiToolParametersSerializer.class)
    KimiToolParameters parameters;
}
