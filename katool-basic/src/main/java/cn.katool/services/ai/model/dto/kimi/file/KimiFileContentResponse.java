package cn.katool.services.ai.model.dto.kimi.file;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class KimiFileContentResponse {
    String content;
    String file_type;
    String filename;
    String title;
    String type;
}
