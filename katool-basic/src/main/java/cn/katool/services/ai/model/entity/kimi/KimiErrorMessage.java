package cn.katool.services.ai.model.entity.kimi;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
public class KimiErrorMessage {
    Integer code;
    String message;
    String param;
    String type;
    String innererror;
}
