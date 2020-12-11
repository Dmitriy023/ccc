package az.iba.ms.card.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import az.iba.ms.card.exceptions.FailedToGetSuccessfulResponseException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

@SpringBootTest
class RestTemplateResponseErrorHandlerTest {

    @Mock
    ClientHttpResponse clientHttpResponse;
    private RestTemplateResponseErrorHandler restTemplateResponseErrorHandler =
            new RestTemplateResponseErrorHandler("service-name");

    @Test
    public void shouldReturnTrue_WhenHttpClientErrors() throws IOException {

        when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);

        boolean val = restTemplateResponseErrorHandler.hasError(clientHttpResponse);
        assertThat(val).isTrue();
    }

    @Test
    public void shouldReturnTrue_WhenHttpServerErrors() throws IOException {

        when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);

        boolean val = restTemplateResponseErrorHandler.hasError(clientHttpResponse);
        assertThat(val).isTrue();
    }

    @Test
    public void shouldThrowException_WhenHttpClientErrorsCactched() throws IOException {

        when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);

        Throwable thrown =
                assertThrows(
                        FailedToGetSuccessfulResponseException.class,
                        () -> restTemplateResponseErrorHandler.handleError(clientHttpResponse));

        assertThat(thrown.getMessage()).isEqualTo("service-name - Got client error on rest response");
    }

    @Test
    public void shouldThrowException_WhenHttpServerErrorsCactched() throws IOException {

        when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.BAD_GATEWAY);

        Throwable thrown =
                assertThrows(
                        FailedToGetSuccessfulResponseException.class,
                        () -> restTemplateResponseErrorHandler.handleError(clientHttpResponse));

        assertThat(thrown.getMessage()).isEqualTo("service-name - Got server error on rest response");
    }
}
