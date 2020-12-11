package az.iba.ms.card.services.impl;

import az.iba.ms.card.dtos.CardInfoDto;
import az.iba.ms.card.dtos.ResponseDto;
import az.iba.ms.card.dtos.flex.CardDetailViewDto;
import az.iba.ms.card.dtos.ufx.BalanceDto;
import az.iba.ms.card.exceptions.FailedToGetSuccessfulResponseException;
import az.iba.ms.card.mappers.CardInfoDtoMapper;
import az.iba.ms.card.services.CacheService;
import az.iba.ms.card.services.CardInfoService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class CardInfoServiceImpl implements CardInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CardInfoServiceImpl.class);

    @Autowired
    CardInfoDtoMapper cardInfoDtoMapper;

    @Autowired
    CacheService cacheService;

    @Autowired
    private RestTemplate flexRestTemplate;

    @Autowired
    private RestTemplate ufxRestTemplate;

    @Autowired
    private HttpHeaders headers;

    @Value("${endpoints.ufx-info-ms}")
    private String ufxInfoMsEndpoint;

    @Value("${endpoints.flex-card-reader}")
    private String flexCardReaderEndpoint;

    @Value("${restTemplate.connectTimeout}")
    private int connectTimeout;

    @Value("${caching.enabled}")
    private boolean enabled;

    @Override
    public List<CardInfoDto> getCardInfo(String cifs) {
        if (enabled) {
            return getCardInfoWithCache(cifs);
        } else {
            return getCardInfoWithoutCache(cifs);
        }
    }

    private List<CardInfoDto> getCardInfoWithoutCache(String cifs) {
        List<CardDetailViewDto> cardDetailViewDtos = getCardDetailView(cifs);

        String cardNumbers =
                cardDetailViewDtos.stream()
                        .map(CardDetailViewDto::getCardNumber)
                        .collect(Collectors.joining(","));

        List<BalanceDto> balanceDtos = getBalance(cardNumbers);

        return cardDetailViewDtos.stream()
                .map(
                        cardDetailViewDto -> {
                            Optional<BalanceDto> balanceDtoOptional =
                                    balanceDtos.stream()
                                            .filter(b -> b.getCardNumber().equals(cardDetailViewDto.getCardNumber()))
                                            .findFirst();
                            return cardInfoDtoMapper.map(cardDetailViewDto, balanceDtoOptional);
                        })
                .collect(Collectors.toList());
    }

    private List<CardInfoDto> getCardInfoWithCache(String cifs) {

        CompletableFuture<Map<String, Optional<BalanceDto>>> balanceFuture =
                getAsyncBalanceUsingCache(cifs);

        CompletableFuture<List<CardDetailViewDto>> cardDetailViewFuture = getAsyncCardDetailView(cifs);

        return collectAsyncResultsAndMergeThem(balanceFuture, cardDetailViewFuture);
    }

    private List<CardInfoDto> collectAsyncResultsAndMergeThem(
            CompletableFuture<Map<String, Optional<BalanceDto>>> balanceFuture,
            CompletableFuture<List<CardDetailViewDto>> cardDetailViewFuture) {

        List<CardDetailViewDto> views = new ArrayList<>();
        try {
            views = cardDetailViewFuture.get(connectTimeout, TimeUnit.MILLISECONDS);
            cacheCifAndCardNumber(views);
        } catch (ExecutionException | TimeoutException e) {
            handleExceptions(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error(e.getMessage());
        }

        Map<String, Optional<BalanceDto>> balanceMap = new HashMap<>();
        try {
            balanceMap = balanceFuture.get(connectTimeout, TimeUnit.MILLISECONDS);
        } catch (ExecutionException | TimeoutException e) {
            handleExceptions(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error(e.getMessage());
        }

        balanceMap = getBalanceOfNotCachedCardNumbers(views, balanceMap);

        return mergeResults(views, balanceMap);
    }

    private void handleExceptions(Exception e) {
        LOGGER.error(e.getMessage());
        if (e.getCause() instanceof ResourceAccessException) {
            throw new ResourceAccessException("flex-card-reader - Failed to get response");
        }
        if (e.getCause() instanceof FailedToGetSuccessfulResponseException) {
            throw new FailedToGetSuccessfulResponseException(e.getCause().getMessage());
        }
    }

    private Map<String, Optional<BalanceDto>> getBalanceOfNotCachedCardNumbers(
            List<CardDetailViewDto> views, Map<String, Optional<BalanceDto>> balanceMap) {

        List<String> cardNumbers = extractNotCachedCardNumbers(views, balanceMap);
        if (!cardNumbers.isEmpty()) {
            balanceMap = getCardNumberBalanceMap(cardNumbers, balanceMap);
        }
        return balanceMap;
    }

    private List<CardInfoDto> mergeResults(
            List<CardDetailViewDto> views, Map<String, Optional<BalanceDto>> balanceMap) {

        return views.stream()
                .map(v -> mergeCardDetailViewWithBalance(v, balanceMap))
                .collect(Collectors.toList());
    }

    private CardInfoDto mergeCardDetailViewWithBalance(
            CardDetailViewDto view, Map<String, Optional<BalanceDto>> balanceMap) {

        Optional<BalanceDto> optionalBalanceDto = Optional.empty();
        if (balanceMap.containsKey(view.getCardNumber())) {
            optionalBalanceDto = balanceMap.get(view.getCardNumber());
        }

        return cardInfoDtoMapper.map(view, optionalBalanceDto);
    }

    private void cacheCifAndCardNumber(List<CardDetailViewDto> views) {
        Map<String, List<String>> cifCardNumberMap = new HashMap<>();
        views.forEach(
                v -> {
                    List<String> cardNumbers =
                            cifCardNumberMap.getOrDefault(v.getCustomerAccountId(), new ArrayList<>());
                    cardNumbers.add(v.getCardNumber());
                    cifCardNumberMap.put(v.getCustomerAccountId(), cardNumbers);
                });

        cifCardNumberMap.forEach((key, value) -> cacheService.cacheCardNumbers(key, value));
    }

    private CompletableFuture<List<CardDetailViewDto>> getAsyncCardDetailView(String cifs) {
        return CompletableFuture.supplyAsync(() -> getCardDetailView(cifs));
    }

    private CompletableFuture<Map<String, Optional<BalanceDto>>> getAsyncBalanceUsingCache(
            String cifs) {

        List<String> cifList = Arrays.asList(cifs.split(","));
        List<String> cardNumberList = cacheService.getCachedCardNumbersOfCifList(cifList);

        return getAsyncCardNumberBalanceMap(cardNumberList);
    }

    private CompletableFuture<Map<String, Optional<BalanceDto>>> getAsyncCardNumberBalanceMap(
            List<String> cardNumberList) {

        return CompletableFuture.supplyAsync(
                () -> getCardNumberBalanceMap(cardNumberList, new HashMap<>()));
    }

    private Map<String, Optional<BalanceDto>> getCardNumberBalanceMap(
            List<String> cardNumberList, Map<String, Optional<BalanceDto>> cardNumberBalanceMap) {

        String cardNumbers = String.join(",", cardNumberList);
        List<BalanceDto> balanceDtos = getBalance(cardNumbers);

        cardNumberList.forEach(
                c -> {
                    Optional<BalanceDto> balanceDtoOptional =
                            balanceDtos.stream().filter(b -> b.getCardNumber().equals(c)).findFirst();
                    cardNumberBalanceMap.put(c, balanceDtoOptional);
                });

        return cardNumberBalanceMap;
    }

    private List<String> extractNotCachedCardNumbers(
            List<CardDetailViewDto> views, Map<String, Optional<BalanceDto>> balanceMap) {

        return views.stream()
                .filter(v -> !balanceMap.containsKey(v.getCardNumber()))
                .map(CardDetailViewDto::getCardNumber)
                .collect(Collectors.toList());
    }

    private List<CardDetailViewDto> getCardDetailView(String cifs) {

        if (StringUtils.isBlank(cifs)) {
            return new ArrayList<>();
        }

        String url =
                UriComponentsBuilder.fromHttpUrl(flexCardReaderEndpoint + "/v1/cardDetailViews")
                        .queryParam("cif-list", cifs)
                        .toUriString();

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<ResponseDto<List<CardDetailViewDto>>> response =
                flexRestTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        request,
                        new ParameterizedTypeReference<ResponseDto<List<CardDetailViewDto>>>() {
                        });

        return response.getBody().getData();
    }

    private List<BalanceDto> getBalance(String cardNumbers) {

        if (StringUtils.isBlank(cardNumbers)) {
            return new ArrayList<>();
        }

        String url =
                UriComponentsBuilder.fromHttpUrl(ufxInfoMsEndpoint + "/v1/balances")
                        .queryParam("card-list", cardNumbers)
                        .toUriString();

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<ResponseDto<List<BalanceDto>>> response =
                ufxRestTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        request,
                        new ParameterizedTypeReference<ResponseDto<List<BalanceDto>>>() {
                        });

        return response.getBody().getData();
    }
}
