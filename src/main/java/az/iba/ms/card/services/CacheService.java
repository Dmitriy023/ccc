package az.iba.ms.card.services;

import java.util.List;

public interface CacheService {

    List<String> getCachedCardNumbersOfCif(String cif);

    List<String> getCachedCardNumbersOfCifList(List<String> cifs);

    void cacheCardNumbers(String cif, List<String> cardNumbers);
}
