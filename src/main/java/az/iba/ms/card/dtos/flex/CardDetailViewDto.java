package az.iba.ms.card.dtos.flex;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDetailViewDto {

    private String customerAccountId;

    private String cardNumber;

    private String cardOwnerName;

    private String cardName;

    private String cardProvider;

    private String cardType;

    private String main;

    private String productCode;

    private String billNumber;

    private String customerAccountNumber;

    private String ibanAccountNumber;

    private String accountDescription;

    private String branchCode;

    private String todLimitStartDate;

    private String todLimitEndDate;

    private Integer todLimit;

    private Integer todRate;

    private String validFrom;

    private String validTo;
}
