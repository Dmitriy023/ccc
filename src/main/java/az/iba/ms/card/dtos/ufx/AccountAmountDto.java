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
public class AccountAmountDto {

    private String type;

    private BigDecimal amount;

    private String currency;
}
