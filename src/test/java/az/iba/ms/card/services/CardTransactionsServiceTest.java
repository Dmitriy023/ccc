package az.iba.ms.card.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import az.iba.ms.card.dtos.ResponseDto;
import az.iba.ms.card.dtos.ufx.TransactionDto;
import az.iba.ms.card.enums.ReturnTypes;
import az.iba.ms.card.exceptions.FailedToGetSuccessfulResponseException;
import az.iba.ms.card.handlers.RestTemplateResponseErrorHandler;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
class CardTransactionsServiceTest {

    private final String contractNumber = "4127621579665690";
    private final String from = "2020-04-20";
    private final String to = "2020-05-21";
    private final int pageSize = 100;
    private final int page = 1;
    private final String url =
            "http://localhost:8082/ufx-info-ms/v1/cards/4127621579665690/transactions"
                    + "?from=2020-04-20&to=2020-05-21&pagesize=100&page=1";

    @Autowired
    private CardTransactionsService cardTransactionsService;

    @MockBean
    @Qualifier("ufxRestTemplate")
    private RestTemplate ufxRestTemplate;

    @BeforeEach
    public void beforeEach() {
        ReflectionTestUtils.setField(
                cardTransactionsService, "ufxInfoMsEndpoint", "http://localhost:8082/ufx-info-ms");
    }

    @Test
    public void shouldReturnTransactions_WhenUfxServiceReturnSuccess() {

        List<TransactionDto> transactionDtos = new ArrayList<>();
        transactionDtos.add(TransactionDto.builder().build());
        ResponseDto<List<TransactionDto>> responseDto =
                new ResponseDto<>(transactionDtos, ReturnTypes.OK.toString());

        when(ufxRestTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                ArgumentMatchers.<HttpEntity<String>>any(),
                ArgumentMatchers.<ParameterizedTypeReference<ResponseDto<List<TransactionDto>>>>any()))
                .thenReturn(ResponseEntity.ok(responseDto));

        List<TransactionDto> result =
                cardTransactionsService.getCardTransactions(
                        contractNumber, from, to, pageSize, page);

        assertThat(result).isEqualTo(transactionDtos);
    }

    @Test
    @DirtiesContext
    public void shouldThrowException_WhenUfxServiceReturns4xx() {

        RestTemplate restTemplate =
                new RestTemplateBuilder()
                        .errorHandler(new RestTemplateResponseErrorHandler("ufx-info-ms"))
                        .build();
        ReflectionTestUtils.setField(cardTransactionsService, "ufxRestTemplate", restTemplate);

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer
                .expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withBadRequest());

        Throwable thrown =
                assertThrows(
                        FailedToGetSuccessfulResponseException.class,
                        () ->
                                cardTransactionsService.getCardTransactions(
                                        contractNumber, from, to, pageSize, page));

        assertThat(thrown.getMessage()).isEqualTo("ufx-info-ms - Got client error on rest response");

        mockServer.verify();
    }

    @Test
    @DirtiesContext
    public void shouldThrowException_WhenUfxServiceReturns5xx() {

        RestTemplate restTemplate =
                new RestTemplateBuilder()
                        .errorHandler(new RestTemplateResponseErrorHandler("ufx-info-ms"))
                        .build();
        ReflectionTestUtils.setField(cardTransactionsService, "ufxRestTemplate", restTemplate);

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer
                .expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        Throwable thrown =
                assertThrows(
                        FailedToGetSuccessfulResponseException.class,
                        () ->
                                cardTransactionsService.getCardTransactions(
                                        contractNumber, from, to, pageSize, page));

        assertThat(thrown.getMessage()).isEqualTo("ufx-info-ms - Got server error on rest response");

        mockServer.verify();
    }
}
