package cn.katool.services.ai.model.dto.kimi.base;
import cn.katool.services.ai.constant.kimi.KimiModel;
import cn.katool.services.ai.model.dto.kimi.tools.KimiToolBody;
import cn.katool.services.ai.model.entity.CommonAIMessage;
import cn.katool.services.ai.model.entity.RequestBody;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.util.List;
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class KimiBaseRequest<T extends CommonAIMessage> implements RequestBody {
    String model = KimiModel.MOONSHOT_V1_8K;
    List<T> messages;
    List<KimiToolBody> tools;
}
