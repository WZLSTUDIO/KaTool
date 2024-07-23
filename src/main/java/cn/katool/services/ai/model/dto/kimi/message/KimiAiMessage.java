package cn.katool.services.ai.model.dto.kimi.message;

import cn.katool.services.ai.model.entity.CommonAIMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class KimiAiMessage extends CommonAIMessage {

    private Boolean partial = false;



}
