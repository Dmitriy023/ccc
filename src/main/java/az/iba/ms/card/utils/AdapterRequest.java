package az.iba.ms.card.utils;

import az.iba.ms.card.handlers.RestTemplateResponseErrorHandler;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AdapterRequest {

    private RestTemplate buildRestTemplate(String service, int connectTimeout, int readTimeout) {

        RestTemplate template =
                new RestTemplateBuilder()
                        .errorHandler(new RestTemplateResponseErrorHandler(service))
                        .build();

        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setReadTimeout(connectTimeout);
        rf.setConnectTimeout(readTimeout);

        template.setRequestFactory(rf);

        return template;
    }

    @Bean(name = "flexRestTemplate")
    public RestTemplate flexRestTemplate(
            @Value("${restTemplate.connectTimeout}") int connectTimeout,
            @Value("${restTemplate.readTimeout}") int readTimeout) {
        return buildRestTemplate("flex-card-reader", connectTimeout, readTimeout);
    }

    @Bean(name = "ufxRestTemplate")
    public RestTemplate ufxRestTemplate(
            @Value("${restTemplate.connectTimeout}") int connectTimeout,
            @Value("${restTemplate.readTimeout}") int readTimeout) {
        return buildRestTemplate("ufx-msg-info", connectTimeout, readTimeout);
    }

    @Bean(name = "headers")
    public HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        return headers;
    }
}
