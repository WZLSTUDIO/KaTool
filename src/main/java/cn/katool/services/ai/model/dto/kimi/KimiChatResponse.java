package cn.katool.services.ai.model.dto.kimi;

import cn.katool.services.ai.model.entity.CommonAIMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KimiChatResponse {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;


    // Getters and setters for each field
    @Data
    @AllArgsConstructor
    @NoArgsConstructor

    public static class Choice {
        private int index;
        private CommonAIMessage message;
        private String finish_reason;

        // Getters and setters for each field inside Choice
    }
    public static class Usage {
        private int prompt_tokens;
        private int completion_tokens;
        private int total_tokens;

        // Getters and setters for each field inside Usage
    }
}