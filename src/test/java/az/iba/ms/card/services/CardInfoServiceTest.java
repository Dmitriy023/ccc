package az.iba.ms.card.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import az.iba.ms.card.dtos.CardInfoDto;
import az.iba.ms.card.dtos.ResponseDto;
import az.iba.ms.card.dtos.flex.CardDetailViewDto;
import az.iba.ms.card.dtos.ufx.BalanceDto;
import az.iba.ms.card.enums.ReturnTypes;
import az.iba.ms.card.exceptions.FailedToGetSuccessfulResponseException;
import az.iba.ms.card.mappers.CardInfoDtoMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
class CardInfoServiceTest {

    private final String cifs = "123,456";
    private final String flexUrl =
            "http://localhost:8084/flex-card-reader/v1/cardDetailViews?cif-list=123,456";
    private final String ufxUrl =
            "http://localhost:8082/ufx-info-ms/v1/balances?card-list=4444555566667777,4444555566668888";
    @MockBean
    CardInfoDtoMapper cardInfoDtoMapper;

    @MockBean
    CacheService cacheService;

    @Mock
    CompletableFuture<Map<String, Optional<BalanceDto>>> future1;

    @Mock
    CompletableFuture<List<CardDetailViewDto>> future2;
    @Autowired
    private CardInfoService cardInfoService;
    @MockBean
    @Qualifier("ufxRestTemplate")
    private RestTemplate ufxRestTemplate;
    @MockBean
    @Qualifier("flexRestTemplate")
    private RestTemplate flexRestTemplate;
    private List<CardDetailViewDto> views;
    private ResponseDto<List<CardDetailViewDto>> viewResponseDto;
    private List<BalanceDto> balanceDtos;
    private ResponseDto<List<BalanceDto>> balanceResponseDto;

    @Mock
    ExecutionException executionException;

    @BeforeEach
    public void beforeEach() {
        ReflectionTestUtils.setField(
                cardInfoService, "ufxInfoMsEndpoint", "http://localhost:8082/ufx-info-ms");

        ReflectionTestUtils.setField(
                cardInfoService, "flexCardReaderEndpoint", "http://localhost:8084/flex-card-reader");

        views = new ArrayList<>();
        views.add(
                CardDetailViewDto.builder()
                        .customerAccountId("123")
                        .cardNumber("4444555566667777")
                        .build());
        views.add(
                CardDetailViewDto.builder()
                        .customerAccountId("456")
                        .cardNumber("4444555566668888")
                        .build());
        viewResponseDto = new ResponseDto<>(views, ReturnTypes.OK.toString());

        balanceDtos = new ArrayList<>();
        balanceDtos.add(BalanceDto.builder().cardNumber("4444555566667777").build());
        balanceResponseDto = new ResponseDto<>(balanceDtos, ReturnTypes.OK.toString());
    }

    @Test
    public void shouldReturnCardInfoDtos_WhenCacheDisabled() {

        ReflectionTestUtils.setField(cardInfoService, "enabled", false);

        when(flexRestTemplate.exchange(
                eq(flexUrl),
                eq(HttpMethod.GET),
                ArgumentMatchers.<HttpEntity<String>>any(),
                ArgumentMatchers
                        .<ParameterizedTypeReference<ResponseDto<List<CardDetailViewDto>>>>any()))
                .thenReturn(ResponseEntity.ok(viewResponseDto));

        when(ufxRestTemplate.exchange(
                eq(ufxUrl),
                eq(HttpMethod.GET),
                ArgumentMatchers.<HttpEntity<String>>any(),
                ArgumentMatchers.<ParameterizedTypeReference<ResponseDto<List<BalanceDto>>>>any()))
                .thenReturn(ResponseEntity.ok(balanceResponseDto));

        List<CardInfoDto> result = cardInfoService.getCardInfo(cifs);

        verify(cardInfoDtoMapper, times(1)).map(views.get(0), Optional.of(balanceDtos.get(0)));
        verify(cardInfoDtoMapper, times(1)).map(views.get(1), Optional.empty());

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void shouldReturnCardInfoDtos_WhenCacheEnabled() {

        ReflectionTestUtils.setField(cardInfoService, "enabled", true);

        views.add(
                CardDetailViewDto.builder()
                        .customerAccountId("123")
                        .cardNumber("4444888866667777")
                        .build());

        List<BalanceDto> balanceDtosOfCachedValues = new ArrayList<>();
        balanceDtosOfCachedValues.add(BalanceDto.builder().cardNumber("4444888866667777").build());
        ResponseDto<List<BalanceDto>> balanceResponseDtoOfCachedValues =
                new ResponseDto<>(balanceDtosOfCachedValues, ReturnTypes.OK.toString());
        String ufxUrlOfCachedValues = "http://localhost:8082/ufx-info-ms/v1/balances?card-list=4444888866667777";

        mockUfx(ufxUrlOfCachedValues, balanceResponseDtoOfCachedValues);
        mockFlex();
        mockUfx(ufxUrl, balanceResponseDto);
        mockCacheService();

        List<CardInfoDto> result = cardInfoService.getCardInfo(cifs);

        verify(cardInfoDtoMapper, times(1)).map(views.get(0), Optional.of(balanceDtos.get(0)));
        verify(cardInfoDtoMapper, times(1)).map(views.get(1), Optional.empty());
        verify(cardInfoDtoMapper, times(1))
                .map(views.get(2), Optional.of(balanceDtosOfCachedValues.get(0)));

        assertThat(result.size()).isEqualTo(3);
    }

    @Test
    public void shouldHandleInterruptedException() throws Exception {

        when(future1.get(anyLong(), eq(TimeUnit.MILLISECONDS)))
                .thenAnswer(
                        invocation -> {
                            throw new InterruptedException("ex");
                        });
        when(future2.get(anyLong(), eq(TimeUnit.MILLISECONDS)))
                .thenAnswer(
                        invocation -> {
                            throw new InterruptedException("ex");
                        });

        List<CardInfoDto> result =
                ReflectionTestUtils.invokeMethod(
                        cardInfoService, "collectAsyncResultsAndMergeThem", future1, future2);

        assertThat(result).isEmpty();
    }

    @Test
    public void shouldHandleTimeoutException() throws Exception {

        when(future1.get(anyLong(), eq(TimeUnit.MILLISECONDS)))
                .thenAnswer(
                        invocation -> {
                            throw new TimeoutException("ex");
                        });
        when(future2.get(anyLong(), eq(TimeUnit.MILLISECONDS)))
                .thenAnswer(
                        invocation -> {
                            throw new TimeoutException("ex");
                        });

        List<CardInfoDto> result =
                ReflectionTestUtils.invokeMethod(
                        cardInfoService, "collectAsyncResultsAndMergeThem", future1, future2);

        assertThat(result).isEmpty();
    }

    @Test
    public void shouldThrowResourceAccessException() throws Exception {

        when(executionException.getCause()).thenReturn(new ResourceAccessException("ex"));

        when(future1.get(anyLong(), eq(TimeUnit.MILLISECONDS)))
                .thenAnswer(
                        invocation -> {
                            throw executionException;
                        });
        when(future2.get(anyLong(), eq(TimeUnit.MILLISECONDS))).thenReturn(new ArrayList<>());

        assertThrows(
                ResourceAccessException.class,
                () ->
                        ReflectionTestUtils.invokeMethod(
                                cardInfoService, "collectAsyncResultsAndMergeThem", future1, future2));

        when(future1.get(anyLong(), eq(TimeUnit.MILLISECONDS))).thenReturn(new HashMap<>());
        when(future2.get(anyLong(), eq(TimeUnit.MILLISECONDS)))
                .thenAnswer(
                        invocation -> {
                            throw executionException;
                        });

        assertThrows(
                ResourceAccessException.class,
                () ->
                        ReflectionTestUtils.invokeMethod(
                                cardInfoService, "collectAsyncResultsAndMergeThem", future1, future2));
    }


    @Test
    public void shouldThrowFailedToGetSuccessfulResponseException() throws Exception {

        when(executionException.getCause()).thenReturn(new FailedToGetSuccessfulResponseException("ex"));

        when(future1.get(anyLong(), eq(TimeUnit.MILLISECONDS)))
                .thenAnswer(
                        invocation -> {
                            throw executionException;
                        });
        when(future2.get(anyLong(), eq(TimeUnit.MILLISECONDS))).thenReturn(new ArrayList<>());

        assertThrows(
                FailedToGetSuccessfulResponseException.class,
                () ->
                        ReflectionTestUtils.invokeMethod(
                                cardInfoService, "collectAsyncResultsAndMergeThem", future1, future2));

        when(future1.get(anyLong(), eq(TimeUnit.MILLISECONDS))).thenReturn(new HashMap<>());
        when(future2.get(anyLong(), eq(TimeUnit.MILLISECONDS)))
                .thenAnswer(
                        invocation -> {
                            throw executionException;
                        });

        assertThrows(
                FailedToGetSuccessfulResponseException.class,
                () ->
                        ReflectionTestUtils.invokeMethod(
                                cardInfoService, "collectAsyncResultsAndMergeThem", future1, future2));
    }

    @Test
    public void shouldThrowFailedToGetSuccessfulResponseException_WhenHandleExceptionsCalled() {

        when(executionException.getCause()).thenReturn(new FailedToGetSuccessfulResponseException("ex"));

        assertThrows(
                FailedToGetSuccessfulResponseException.class,
                () ->
                        ReflectionTestUtils.invokeMethod(
                                cardInfoService, "handleExceptions",
                                executionException));
    }

    @Test
    public void shouldThrowResourceAccessException_WhenHandleExceptionsCalled() {

        when(executionException.getCause()).thenReturn(new ResourceAccessException("ex"));

        assertThrows(
                ResourceAccessException.class,
                () ->
                        ReflectionTestUtils.invokeMethod(
                                cardInfoService, "handleExceptions",
                                executionException));
    }

    @Test
    public void shouldNotThrowException_WhenHandleExceptionsCalled() {

        ReflectionTestUtils.invokeMethod(
                cardInfoService, "handleExceptions",
                new TimeoutException("ex"));
    }

    private void mockCacheService() {
        List<String> cifList = new ArrayList<>();
        cifList.add("123");
        cifList.add("456");

        List<String> cachedCardNumbers = new ArrayList<>();
        cachedCardNumbers.add("4444888866667777");

        when(cacheService.getCachedCardNumbersOfCifList(cifList)).thenReturn(cachedCardNumbers);
    }

    private void mockFlex() {
        when(flexRestTemplate.exchange(
                eq(flexUrl),
                eq(HttpMethod.GET),
                ArgumentMatchers.<HttpEntity<String>>any(),
                ArgumentMatchers
                        .<ParameterizedTypeReference<ResponseDto<List<CardDetailViewDto>>>>any()))
                .thenReturn(ResponseEntity.ok(viewResponseDto));
    }

    private void mockUfx(
            String ufxUrlOfCachedValues,
            ResponseDto<List<BalanceDto>> balanceResponseDtoOfCachedValues
    ) {
        when(ufxRestTemplate.exchange(
                eq(ufxUrlOfCachedValues),
                eq(HttpMethod.GET),
                ArgumentMatchers.<HttpEntity<String>>any(),
                ArgumentMatchers.<ParameterizedTypeReference<ResponseDto<List<BalanceDto>>>>any()))
                .thenReturn(ResponseEntity.ok(balanceResponseDtoOfCachedValues));
    }

    @Test
    public void shouldReturnEmptyList_WhenCardNumbersAreBlank() {

        List<BalanceDto> balanceDtos = ReflectionTestUtils.invokeMethod(
                cardInfoService, "getBalance",
                "");

        assertThat(balanceDtos.isEmpty()).isTrue();
    }

    @Test
    public void shouldReturnEmptyList_WhenCifsAreBlank() {

        List<BalanceDto> balanceDtos = ReflectionTestUtils.invokeMethod(
                cardInfoService, "getCardDetailView",
                "");

        assertThat(balanceDtos.isEmpty()).isTrue();
    }
}
