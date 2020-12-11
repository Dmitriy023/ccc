package az.iba.ms.card.dtos;

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
public class CardInfoDto {

    @JsonProperty("card_number")
    private String cardNumber;

    @JsonProperty("contract_number")
    private String contractNumber;

    @JsonProperty("open_date")
    private String openDate;

    @JsonProperty("expiry_date")
    private String expiryDate;

    @JsonProperty("card_holder_name")
    private String cardHolderName;

    @JsonProperty("cif")
    private String cif;

    @JsonProperty("card_type_name")
    private String cardTypeName;

    @JsonProperty("main")
    private String main;

    @JsonProperty("card_network")
    private String cardNetwork;

    @JsonProperty("card_type")
    private String cardType;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("card_type_code")
    private String cardTypeCode;

    @JsonProperty("available_balance")
    private BigDecimal availableBalance;

    @JsonProperty("credit_limit")
    private BigDecimal creditLimit;

    @JsonProperty("bonus")
    private BigDecimal bonus;

    @JsonProperty("extra_info")
    private String extraInfo;

    @JsonProperty("reg_number")
    private String regNumber;

    @JsonProperty("card_status_class")
    private String cardStatusClass;

    @JsonProperty("card_status_code")
    private String cardStatusCode;

    @JsonProperty("card_status")
    private String cardStatus;

    @JsonProperty("blocked_amount")
    private BigDecimal blockedAmount;

    @JsonProperty("card_bin")
    private String cardBin;

    @JsonProperty("card_account")
    private CardAccountDto cardAccountDto;
}
