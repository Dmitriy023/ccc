package az.iba.ms.card.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import az.iba.ms.card.dtos.BranchDto;
import az.iba.ms.card.dtos.CardAccountDto;
import az.iba.ms.card.dtos.CardInfoDto;
import az.iba.ms.card.dtos.ResponseDto;
import az.iba.ms.card.services.CardInfoService;
import az.iba.ms.card.validators.CifListSizeValidator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CardInfoController.class)
public class CardInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CardInfoController cardInfoController;

    @MockBean
    private CardInfoService cardInfoService;

    @Mock
    private CifListSizeValidator cifListSizeValidator;

    @Test
    public void shouldReturn200_WhenValidParamsProvided() throws Exception {
        mockMvc
                .perform(get("/v1/cards?cif-list=1234567").contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturn400_WhenParamsNotProvided() throws Exception {
        mockMvc
                .perform(get("/v1/cards").contentType("application/json"))
                .andExpect(status().isBadRequest());
        mockMvc
                .perform(get("/v1/cards?ciflist=23121").contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturn400_WhenNotValidCifProvided() throws Exception {
        mockMvc
                .perform(get("/v1/cards?cif-list=123").contentType("application/json"))
                .andExpect(status().isBadRequest());
        mockMvc
                .perform(get("/v1/cards?cif-list=23121a").contentType("application/json"))
                .andExpect(status().isBadRequest());
        mockMvc
                .perform(get("/v1/cards?cif-list=23121,123121,").contentType("application/json"))
                .andExpect(status().isBadRequest());
        mockMvc
                .perform(get("/v1/cards?cif-list=,").contentType("application/json"))
                .andExpect(status().isBadRequest());
        mockMvc
                .perform(get("/v1/cards?cif-list,").contentType("application/json"))
                .andExpect(status().isBadRequest());
        mockMvc
                .perform(get("/v1/cards?cif-list=,1231").contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturn400_WhenCifListExceedsAllowedSize() throws Exception {

        when(cifListSizeValidator.isValid(eq("1,2,3,4"), any())).thenReturn(false);

        mockMvc
                .perform(get("/v1/cards?ciflist=1,2,3,4").contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnCardInfo_WhenCardFoundForCif() {
        String cif = "1234567,1234568";

        CardInfoDto dto = getCardInfoDto(cif);

        List<CardInfoDto> cards = new ArrayList<>();
        cards.add(dto);
        cards.add(dto);

        when(cardInfoService.getCardInfo(cif)).thenReturn(cards);

        ResponseEntity<ResponseDto<List<CardInfoDto>>> responseEntity =
                cardInfoController.getCardInfo(cif);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getData().get(0)).isEqualTo(dto);
        assertThat(responseEntity.getBody().getData().get(1)).isEqualTo(dto);
    }

    @Test
    public void shouldReturnEmptyList_WhenNoCardFoundForCif() {
        String cif = "1234567";

        List<CardInfoDto> cards = new ArrayList<>();

        when(cardInfoService.getCardInfo(cif)).thenReturn(cards);

        ResponseEntity<ResponseDto<List<CardInfoDto>>> responseEntity =
                cardInfoController.getCardInfo(cif);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getData().isEmpty()).isTrue();
    }

    private CardInfoDto getCardInfoDto(String cif) {
        return CardInfoDto.builder()
                .cardNumber("4444555566667777")
                .cardNetwork("cardNetwork")
                .cardHolderName("cardHolderName")
                .cardAccountDto(getCardAccountDto())
                .cardBin("cardBin")
                .contractNumber("contractNumber")
                .availableBalance(BigDecimal.valueOf(1200, 2))
                .blockedAmount(BigDecimal.valueOf(1300, 2))
                .bonus(BigDecimal.valueOf(1400, 2))
                .cardStatus("cardStatus")
                .cardStatusClass("cardStatusClass")
                .cardStatusCode("cardStatusCode")
                .cardType("cardType")
                .cardTypeCode("cardTypeCode")
                .cardTypeName("cardTypeName")
                .cif(cif)
                .creditLimit(BigDecimal.valueOf(1600, 2))
                .currency("AZN")
                .expiryDate("02/21")
                .openDate("02/20")
                .extraInfo("extraInfo")
                .main("F")
                .regNumber("regNumber")
                .build();
    }

    private CardAccountDto getCardAccountDto() {
        return CardAccountDto.builder()
                .todRate(2)
                .name("cardAccountName")
                .branch(BranchDto.builder().name("branchName").code("branchCode").build())
                .build();
    }
}
