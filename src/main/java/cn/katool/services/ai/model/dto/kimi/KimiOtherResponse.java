package cn.katool.services.ai.model.dto.kimi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.N;

@Data
@AllArgsConstructor
@N
public class KimiOtherResponse {
    Integer code;
    String scode;
    Boolean staus;
    KimiOtherResponseData data;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class KimiOtherResponseData{
        Long total_tokens;
        Double available_balance;
        Double voucher_balance;
        Double cash_balance;
    }

}


