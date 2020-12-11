package az.iba.ms.card.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import az.iba.ms.card.dtos.CardInfoDto;
import az.iba.ms.card.dtos.flex.CardDetailViewDto;
import az.iba.ms.card.dtos.ufx.BalanceDto;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CardInfoDtoMapperTest {

    @Autowired
    private CardInfoDtoMapper cardInfoDtoMapper;

    @Test
    public void shouldMapToCardInfoDtoMapper() {

        CardDetailViewDto cardDetailViewDto = getCardDetailViewDto();
        BalanceDto balanceDto = getBalanceDto();

        checkBothValuesProvided(cardDetailViewDto, balanceDto);

        checkBalanceDtoEmpty(cardDetailViewDto);

        balanceDto = getBalanceDtoWithoutAmount();

        checkBalanceDtoAmountsEmpty(cardDetailViewDto, balanceDto);
    }

    private CardDetailViewDto getCardDetailViewDto() {
        return CardDetailViewDto.builder()
                .customerAccountId("6127961")
                .accountDescription("accountDesc")
                .cardNumber("412757425836263")
                .billNumber("44556677")
                .branchCode("1122")
                .cardName("apex")
                .cardOwnerName("AYDAN CAVADOVA")
                .cardProvider("VISA")
                .cardType("Debit")
                .customerAccountNumber("1231")
                .ibanAccountNumber("TR2134")
                .main("T")
                .productCode("C45")
                .todLimit(123)
                .todLimitEndDate("2025")
                .todLimitStartDate("2019")
                .todRate(2)
                .validFrom("05/13")
                .validTo("05/23")
                .build();
    }

    private BalanceDto getBalanceDto() {
        return BalanceDto.builder()
                .cardBin("444455")
                .contractNumber("N4444555566667777")
                .availableBalance(BigDecimal.valueOf(1200, 2))
                .blockedAmount(BigDecimal.valueOf(1300, 2))
                .bonus(BigDecimal.valueOf(1400, 2))
                .cardStatus("cardStatus")
                .cardStatusClass("cardStatusClass")
                .cardStatusCode("cardStatusCode")
                .cardTypeCode("cardTypeCode")
                .creditLimit(BigDecimal.valueOf(1600, 2))
                .currency("AZN")
                .extraInfo("extraInfo")
                .regNumber("regNumber")
                .build();
    }

    private BalanceDto getBalanceDtoWithoutAmount() {
        return BalanceDto.builder()
                .cardBin("444455")
                .cardStatus("cardStatus")
                .cardStatusClass("cardStatusClass")
                .cardStatusCode("cardStatusCode")
                .cardTypeCode("cardTypeCode")
                .currency("AZN")
                .extraInfo("extraInfo")
                .regNumber("regNumber")
                .build();
    }

    private void checkBothValuesProvided(CardDetailViewDto cardDetailViewDto, BalanceDto balanceDto) {
        CardInfoDto cardInfoDto = cardInfoDtoMapper.map(cardDetailViewDto, Optional.of(balanceDto));

        checkCardDetailViewDtoFields(cardInfoDto);

        assertThat(cardInfoDto.getContractNumber()).isEqualTo("N4444555566667777");

        checkBalanceDtoFields(cardInfoDto);
    }

    private void checkCardDetailViewDtoFields(CardInfoDto cardInfoDto) {
        assertThat(cardInfoDto.getCardAccountDto().getIban()).isEqualTo("TR2134");
        assertThat(cardInfoDto.getCardAccountDto().getName()).isEqualTo("accountDesc");
        assertThat(cardInfoDto.getCardAccountDto().getNumber()).isEqualTo("1231");
        assertThat(cardInfoDto.getCardAccountDto().getTodLimit()).isEqualTo(123);
        assertThat(cardInfoDto.getCardAccountDto().getTodLimitEndDate()).isEqualTo("2025");
        assertThat(cardInfoDto.getCardAccountDto().getTodLimitStartDate()).isEqualTo("2019");
        assertThat(cardInfoDto.getCardAccountDto().getTodRate()).isEqualTo(2);
        assertThat(cardInfoDto.getCardAccountDto().getBranch().getName()).isNull();
        assertThat(cardInfoDto.getCardAccountDto().getBranch().getCode()).isEqualTo("1122");
        assertThat(cardInfoDto.getCardHolderName()).isEqualTo("AYDAN CAVADOVA");
        assertThat(cardInfoDto.getCardNetwork()).isEqualTo("VISA");
        assertThat(cardInfoDto.getCardNumber()).isEqualTo("412757425836263");
        assertThat(cardInfoDto.getCif()).isEqualTo("6127961");
        assertThat(cardInfoDto.getExpiryDate()).isEqualTo("05/23");
        assertThat(cardInfoDto.getMain()).isEqualTo("T");
        assertThat(cardInfoDto.getOpenDate()).isEqualTo("05/13");
    }

    private void checkBalanceDtoFields(CardInfoDto cardInfoDto) {
        assertThat(cardInfoDto.getCurrency()).isEqualTo("AZN");
        assertThat(cardInfoDto.getRegNumber()).isEqualTo("regNumber");
        assertThat(cardInfoDto.getCardStatusClass()).isEqualTo("cardStatusClass");
        assertThat(cardInfoDto.getCardStatusCode()).isEqualTo("cardStatusCode");
        assertThat(cardInfoDto.getCardStatus()).isEqualTo("cardStatus");
        assertThat(cardInfoDto.getCardTypeCode()).isEqualTo("cardTypeCode");
        assertThat(cardInfoDto.getCardBin()).isEqualTo("444455");
        assertThat(cardInfoDto.getExtraInfo()).isEqualTo("extraInfo");
        assertThat(cardInfoDto.getAvailableBalance()).isEqualTo(BigDecimal.valueOf(1200, 2));
        assertThat(cardInfoDto.getBlockedAmount()).isEqualTo(BigDecimal.valueOf(1300, 2));
        assertThat(cardInfoDto.getCreditLimit()).isEqualTo(BigDecimal.valueOf(1600, 2));
        assertThat(cardInfoDto.getBonus()).isEqualTo(BigDecimal.valueOf(1400, 2));
    }

    private void checkBalanceDtoEmpty(CardDetailViewDto cardDetailViewDto) {
        CardInfoDto cardInfoDto = cardInfoDtoMapper.map(cardDetailViewDto, Optional.empty());

        checkCardDetailViewDtoFields(cardInfoDto);

        assertThat(cardInfoDto.getContractNumber()).isEqualTo("412757425836263");

        assertThat(cardInfoDto.getCurrency()).isNull();
        assertThat(cardInfoDto.getRegNumber()).isNull();
        assertThat(cardInfoDto.getCardStatusClass()).isNull();
        assertThat(cardInfoDto.getCardStatusCode()).isNull();
        assertThat(cardInfoDto.getCardStatus()).isNull();
        assertThat(cardInfoDto.getCardTypeCode()).isNull();
        assertThat(cardInfoDto.getCardBin()).isNull();
        assertThat(cardInfoDto.getExtraInfo()).isNull();
        assertThat(cardInfoDto.getAvailableBalance()).isEqualTo(BigDecimal.valueOf(0, 2));
        assertThat(cardInfoDto.getBlockedAmount()).isEqualTo(BigDecimal.valueOf(0, 2));
        assertThat(cardInfoDto.getCreditLimit()).isEqualTo(BigDecimal.valueOf(0, 2));
        assertThat(cardInfoDto.getBonus()).isNull();
    }

    private void checkBalanceDtoAmountsEmpty(CardDetailViewDto cardDetailViewDto, BalanceDto balanceDto) {
        CardInfoDto cardInfoDto = cardInfoDtoMapper.map(cardDetailViewDto, Optional.of(balanceDto));

        checkCardDetailViewDtoFields(cardInfoDto);

        assertThat(cardInfoDto.getContractNumber()).isEqualTo("412757425836263");

        assertThat(cardInfoDto.getAvailableBalance()).isEqualTo(BigDecimal.valueOf(0, 2));
        assertThat(cardInfoDto.getBlockedAmount()).isEqualTo(BigDecimal.valueOf(0, 2));
        assertThat(cardInfoDto.getCreditLimit()).isEqualTo(BigDecimal.valueOf(0, 2));
        assertThat(cardInfoDto.getBonus()).isNull();
    }
}
