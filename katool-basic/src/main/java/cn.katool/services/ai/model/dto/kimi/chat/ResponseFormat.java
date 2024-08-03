package cn.katool.services.ai.model.dto.kimi.chat;
import cn.katool.services.ai.constant.kimi.KimiResponseFormatEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ResponseFormat {
    String type;
    public ResponseFormat(KimiResponseFormatEnum enume) {
        this.type=enume.getType();
    }
}
