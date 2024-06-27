package cn.katool.services.ai.model.dto.kimi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
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