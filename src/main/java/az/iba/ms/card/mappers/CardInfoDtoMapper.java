package az.iba.ms.card.mappers;

import az.iba.ms.card.dtos.BranchDto;
import az.iba.ms.card.dtos.CardAccountDto;
import az.iba.ms.card.dtos.CardInfoDto;
import az.iba.ms.card.dtos.flex.CardDetailViewDto;
import az.iba.ms.card.dtos.ufx.BalanceDto;
import java.math.BigDecimal;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class CardInfoDtoMapper {

    public CardInfoDto map(
            CardDetailViewDto cardDetailViewDto, Optional<BalanceDto> balanceDtoOptional) {

        CardInfoDto cardInfoDto = mapCardDetailViewDto(cardDetailViewDto);

        balanceDtoOptional.ifPresent(
                balanceDto -> mapBalanceDto(balanceDto, cardInfoDto));

        return cardInfoDto;
    }

    private CardInfoDto mapCardDetailViewDto(CardDetailViewDto cardDetailViewDto) {
        return CardInfoDto.builder()
                .cardAccountDto(
                        CardAccountDto.builder()
                                .number(cardDetailViewDto.getCustomerAccountNumber())
                                .name(cardDetailViewDto.getAccountDescription())
                                .iban(cardDetailViewDto.getIbanAccountNumber())
                                .todLimit(cardDetailViewDto.getTodLimit())
                                .todLimitStartDate(cardDetailViewDto.getTodLimitStartDate())
                                .todLimitEndDate(cardDetailViewDto.getTodLimitEndDate())
                                .todRate(cardDetailViewDto.getTodRate())
                                .branch(BranchDto.builder().code(cardDetailViewDto.getBranchCode()).build())
                                .build())
                .cardNumber(cardDetailViewDto.getCardNumber())
                .contractNumber(cardDetailViewDto.getCardNumber())
                .openDate(cardDetailViewDto.getValidFrom())
                .expiryDate(cardDetailViewDto.getValidTo())
                .cardHolderName(cardDetailViewDto.getCardOwnerName())
                .cif(cardDetailViewDto.getCustomerAccountId())
                .cardTypeName(cardDetailViewDto.getCardName())
                .main(cardDetailViewDto.getMain())
                .cardNetwork(cardDetailViewDto.getCardProvider())
                .cardType(cardDetailViewDto.getCardType())
                .availableBalance(BigDecimal.valueOf(0, 2))
                .blockedAmount(BigDecimal.valueOf(0, 2))
                .creditLimit(BigDecimal.valueOf(0, 2))
                .build();
    }

    private void mapBalanceDto(BalanceDto balanceDto, CardInfoDto cardInfoDto) {
        cardInfoDto.setCurrency(balanceDto.getCurrency());
        cardInfoDto.setRegNumber(balanceDto.getRegNumber());
        cardInfoDto.setCardStatusClass(balanceDto.getCardStatusClass());
        cardInfoDto.setCardStatusCode(balanceDto.getCardStatusCode());
        cardInfoDto.setCardStatus(balanceDto.getCardStatus());
        cardInfoDto.setCardBin(balanceDto.getCardBin());
        cardInfoDto.setCardTypeCode(balanceDto.getCardTypeCode());
        cardInfoDto.setExtraInfo(balanceDto.getExtraInfo());

        if (StringUtils.isNotBlank(balanceDto.getContractNumber())) {
            cardInfoDto.setContractNumber(balanceDto.getContractNumber());
        }

        if (balanceDto.getAvailableBalance() != null) {
            cardInfoDto.setAvailableBalance(balanceDto.getAvailableBalance());
        }

        if (balanceDto.getCreditLimit() != null) {
            cardInfoDto.setCreditLimit(balanceDto.getCreditLimit());
        }

        if (balanceDto.getBlockedAmount() != null) {
            cardInfoDto.setBlockedAmount(balanceDto.getBlockedAmount());
        }

        if (balanceDto.getBonus() != null) {
            cardInfoDto.setBonus(balanceDto.getBonus());
        }
    }
}
