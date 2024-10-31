package cn.katool.services.ai.model.dto.kimi.message;
import cn.katool.services.ai.model.dto.kimi.tools.ToolCalls;
import cn.katool.services.ai.model.entity.CommonAIMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class KimiAiMergeMessage extends CommonAIMessage {
    private Boolean partial = false;
    String tool_call_id;
    String name;
    List<ToolCalls> tool_calls;
}
