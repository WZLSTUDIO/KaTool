package cn.katool.services.ai.model.dto.kimi.tools;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ToolCalls {
    String id;
    ToolCallsFuntion function;
    String type;
}
