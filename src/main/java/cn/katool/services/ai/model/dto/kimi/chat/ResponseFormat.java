package cn.katool.services.ai.model.dto.kimi.chat;

import cn.katool.services.ai.constant.kimi.KimiResponseFormatEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseFormat {
    String type;

    public ResponseFormat(KimiResponseFormatEnum enume) {
        this.type=enume.getType();
    }
}
