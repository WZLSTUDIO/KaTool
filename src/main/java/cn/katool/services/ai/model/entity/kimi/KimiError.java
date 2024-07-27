package cn.katool.services.ai.model.entity.kimi;
import cn.katool.services.ai.model.entity.ErrorMessage;
import cn.katool.services.ai.model.entity.RequestBody;
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
public class KimiError<T extends RequestBody> extends ErrorMessage<T>{
    KimiErrorMessage error;

}
