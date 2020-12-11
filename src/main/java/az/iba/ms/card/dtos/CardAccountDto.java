package az.iba.ms.card.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CardAccountDto {

    @JsonProperty("number")
    private String number;

    @JsonProperty("name")
    private String name;

    @JsonProperty("iban")
    private String iban;

    @JsonProperty("branch")
    private BranchDto branch;

    @JsonProperty("tod_limit_start_date")
    private String todLimitStartDate;

    @JsonProperty("tod_limit_end_date")
    private String todLimitEndDate;

    @JsonProperty("tod_limit")
    private Integer todLimit;

    @JsonProperty("tod_rate")
    private Integer todRate;
}
