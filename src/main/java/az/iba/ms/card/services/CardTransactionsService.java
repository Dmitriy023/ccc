package az.iba.ms.card.services;

import az.iba.ms.card.dtos.ufx.TransactionDto;
import java.util.List;

public interface CardTransactionsService {

    List<TransactionDto> getCardTransactions(
            String contractNumber, String from, String to, int pageSize, int page);
}
