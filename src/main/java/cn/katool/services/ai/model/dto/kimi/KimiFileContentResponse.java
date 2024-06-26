package cn.katool.services.ai.model.dto.kimi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KimiFileContentResponse {
    String content;
    String file_type;
    String filename;
    String title;
    String type;
}
