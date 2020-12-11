package az.iba.ms.card.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import az.iba.ms.card.dtos.ErrorResponseDto;
import az.iba.ms.card.exceptions.FailedToGetSuccessfulResponseException;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;

@SpringBootTest
public class CustomExceptionHandlerTest {

    @Mock
    ConstraintViolationException constraintViolationException;
    @Autowired
    private CustomExceptionHandler customExceptionHandler;

    @Test
    public void shouldReturn500_WhenGenericExceptionIsThrown() {

        ResponseEntity<ErrorResponseDto> response =
                customExceptionHandler.handleException(new Exception("ex"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getError()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.name());
        assertThat(response.getBody().getMessage()).isEqualTo("ex");
        assertThat(response.getBody().getErrorDetail()).isNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    public void shouldReturn400_WhenConstraintViolationExceptionIsThrown() {

        when(constraintViolationException.getMessage()).thenReturn("msg");
        when(constraintViolationException.getCause()).thenReturn(new Exception("deneme"));

        ResponseEntity<ErrorResponseDto> response =
                customExceptionHandler.handleException(constraintViolationException);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError()).isEqualTo(HttpStatus.BAD_REQUEST.name());
        assertThat(response.getBody().getMessage()).isEqualTo("msg");
        assertThat(response.getBody().getErrorDetail()).isEqualTo("deneme");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    public void shouldReturn502_WhenFailedToGetSuccessfulResponseExceptionIsThrown() {

        ResponseEntity<ErrorResponseDto> response =
                customExceptionHandler.handleException(
                        new FailedToGetSuccessfulResponseException("some error msg"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody().getError()).isEqualTo(HttpStatus.BAD_GATEWAY.name());
        assertThat(response.getBody().getMessage()).isEqualTo("some error msg");
        assertThat(response.getBody().getErrorDetail()).isNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY.value());
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    public void shouldReturn504_WhenResourceAccessExceptionIsThrown() {

        ResponseEntity<ErrorResponseDto> response =
                customExceptionHandler.handleException(new ResourceAccessException("some error msg"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
        assertThat(response.getBody().getError()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT.name());
        assertThat(response.getBody().getMessage()).isEqualTo("some error msg");
        assertThat(response.getBody().getErrorDetail()).isNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value());
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }
}
