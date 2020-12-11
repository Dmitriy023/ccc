package az.iba.ms.card.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
class AdapterRequestTest {

    @Autowired
    @Qualifier("flexRestTemplate")
    RestTemplate flexRestTemplate;

    @Autowired
    @Qualifier("ufxRestTemplate")
    RestTemplate ufxRestTemplate;

    @Autowired
    @Qualifier("headers")
    HttpHeaders headers;

    @Test
    public void shouldErrorHandler_MustBeSet() {

        assertThat(flexRestTemplate.getErrorHandler()).isNotNull();
        assertThat(ufxRestTemplate.getErrorHandler()).isNotNull();
    }

    @Test
    public void shouldContentHeaders_MustBeSet() {

        assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(headers.getAccept())
                .isEqualTo(Collections.singletonList(MediaType.APPLICATION_JSON));
    }
}
