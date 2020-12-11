package az.iba.ms.card.services;

import az.iba.ms.card.dtos.CardInfoDto;
import java.util.List;

public interface CardInfoService {

    List<CardInfoDto> getCardInfo(String cifs);
}
