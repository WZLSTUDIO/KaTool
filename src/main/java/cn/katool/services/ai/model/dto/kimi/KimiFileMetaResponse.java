package cn.katool.services.ai.model.dto.kimi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KimiFileMetaResponse{
    private String object;

    List<KimiFileMeta> data;

}
