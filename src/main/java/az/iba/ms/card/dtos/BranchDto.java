package az.iba.ms.card.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BranchDto {

    @JsonProperty("code")
    private String code;

    @JsonProperty("name")
    private String name;
}
