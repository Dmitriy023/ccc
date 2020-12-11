package az.iba.ms.card.dtos;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExtraDto {

    private String type;

    private String currency;

    private BigDecimal amount;

    private String details;
}
