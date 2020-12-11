package az.iba.ms.card.dtos.ufx;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDto {

    private BigDecimal amount;

    private String currency;

    private String date;

    private String description;

    private String mcc;

    @JsonProperty("trnTypeID")
    private String trnTypeId;

    private String trnTypeCode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String rrn;

    private String status;

    private BigDecimal localAmount;

    private String trnType;

    private String descriptionExt;

    @JsonProperty("postingDetails")
    private PostingDetailsDto postingDetailsDto;

    @JsonProperty("billing")
    private BillingDto billingDto;
}
