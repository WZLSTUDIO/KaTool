package cn.katool.services.ai.model.dto.kimi.file;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.util.Date;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class KimiFileMeta {
    private String id;
    private String object;
    private Long bytes;
    private Long created_at;
    private String filename;
    private String purpose;
    private String status;
    private String status_details;
}