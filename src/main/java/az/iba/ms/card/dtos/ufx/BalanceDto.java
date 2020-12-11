package az.iba.ms.card.dtos.ufx;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BalanceDto {

    private String cardNumber;

    private String contractNumber;

    private String currency;

    private String cardTypeCode;

    private BigDecimal availableBalance;

    private BigDecimal creditLimit;

    private BigDecimal bonus;

    private String extraInfo;

    private String regNumber;

    private String cardStatusClass;

    private String cardStatusCode;

    private String cardStatus;

    private BigDecimal blockedAmount;

    private String cardBin;
}
