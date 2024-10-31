package cn.katool.services.ai.model.dto.kimi.message;
import cn.katool.services.ai.model.dto.kimi.chat.KimiChatResponse;
import cn.katool.services.ai.model.dto.kimi.tools.ToolCalls;
import cn.katool.services.ai.model.entity.CommonAIMessage;
import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class KimiAiToolCallsMessage extends CommonAIMessage {
    String tool_call_id;
    @Expose(serialize = false)
    KimiChatResponse.Usage usage;
    @Expose(serialize = false)
    List<ToolCalls> tool_calls;
    String name;
}
