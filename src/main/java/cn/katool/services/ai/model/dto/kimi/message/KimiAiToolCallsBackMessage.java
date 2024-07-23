package cn.katool.services.ai.model.dto.kimi.message;

import cn.katool.services.ai.model.dto.kimi.tools.ToolCalls;
import cn.katool.services.ai.model.entity.CommonAIMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class KimiAiToolCallsBackMessage extends CommonAIMessage {
    ToolCalls tool_calls;
}
