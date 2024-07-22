package cn.katool.services.ai.model.dto.kimi.chat;

import cn.katool.services.ai.constant.kimi.KimiResponseFormatEnum;
import cn.katool.services.ai.model.dto.kimi.base.KimiBaseRequest;
import cn.katool.services.ai.model.dto.kimi.tools.KimiToolBody;
import cn.katool.services.ai.model.entity.CommonAIMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class KimiChatRequest extends KimiBaseRequest {
    Float temperature = 0.3f;
    Float top_p = 1.0f;
    Integer max_tokens = 1024;
    Integer n = 1;
    Float presence_penalty = 0f;
    Float frequency_penalty = 0f;
    ResponseFormat response_format =  new ResponseFormat(KimiResponseFormatEnum.TEXT);
    List<String> stop = null;
    Boolean stream = false;

    public void setResponse_format(KimiResponseFormatEnum  response_format) {
        this.response_format = new ResponseFormat(response_format);
    }

    public KimiChatRequest setStop(List<String> stop) {
        if (stop == null || stop.isEmpty()) {
            this.stop = null;
        } else {
            stop = stop.stream().filter(s -> s.getBytes(StandardCharsets.UTF_8).length < 32).collect(Collectors.toList());
            if (stop.size() > 5){
                stop = stop.subList(0,5);
            }
        }
        this.stop = stop;
        return this;
    }

    @Override
    public KimiChatRequest setModel(String model) {
        return (KimiChatRequest) super.setModel(model);
    }

    @Override
    public KimiChatRequest setMessages(List<CommonAIMessage> messages) {
        return (KimiChatRequest) super.setMessages(messages);
    }

    @Override
    public KimiChatRequest setTools(List<KimiToolBody> tools) {
        return (KimiChatRequest) super.setTools(tools);
    }
}
