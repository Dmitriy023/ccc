package az.iba.ms.card.services.impl;

import az.iba.ms.card.dtos.ResponseDto;
import az.iba.ms.card.dtos.ufx.TransactionDto;
import az.iba.ms.card.services.CardTransactionsService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class CardTransactionsServiceImpl implements CardTransactionsService {

    @Autowired
    private RestTemplate ufxRestTemplate;

    @Autowired
    private HttpHeaders headers;

    @Value("${endpoints.ufx-info-ms}")
    private String ufxInfoMsEndpoint;

    @Override
    public List<TransactionDto> getCardTransactions(
            String contractNumber, String from, String to, int pageSize, int page) {

        String url =
                UriComponentsBuilder.fromHttpUrl(
                        ufxInfoMsEndpoint + "/v1/cards/" + contractNumber + "/transactions")
                        .queryParam("from", from)
                        .queryParam("to", to)
                        .queryParam("pagesize", pageSize)
                        .queryParam("page", page)
                        .toUriString();

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<ResponseDto<List<TransactionDto>>> response =
                ufxRestTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        request,
                        new ParameterizedTypeReference<ResponseDto<List<TransactionDto>>>() {
                        });

        return response.getBody().getData();
    }
}
