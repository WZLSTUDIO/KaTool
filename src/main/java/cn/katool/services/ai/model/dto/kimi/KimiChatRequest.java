package cn.katool.services.ai.model.dto.kimi;

import cn.katool.services.ai.constant.KimiModel;
import cn.katool.services.ai.constant.KimiResponseFormatEnum;
import cn.katool.services.ai.model.entity.CommonAIMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Data
//@Builder
@Accessors(chain = true)
public class KimiChatRequest {
    String model = KimiModel.MOONSHOT_V1_8K;
    List<CommonAIMessage> messages;
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
}
