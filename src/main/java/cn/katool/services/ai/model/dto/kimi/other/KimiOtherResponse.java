package cn.katool.services.ai.model.dto.kimi.other;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class KimiOtherResponse {
    Integer code;
    String scode;
    Boolean status;
    KimiOtherResponseData data;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class KimiOtherResponseData implements AiQueryMoneyResponse {
        Long total_tokens;
        Double available_balance;
        Double voucher_balance;
        Double cash_balance;
    }
}
