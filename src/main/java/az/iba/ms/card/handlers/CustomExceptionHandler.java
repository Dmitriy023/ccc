package az.iba.ms.card.handlers;

import az.iba.ms.card.dtos.ErrorResponseDto;
import az.iba.ms.card.exceptions.FailedToGetSuccessfulResponseException;
import java.time.LocalDateTime;
import javax.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    private HttpStatus getStatus(Exception ex) {
        if (ex instanceof ConstraintViolationException) {
            return HttpStatus.BAD_REQUEST;
        } else if (ex instanceof ResourceAccessException) {
            return HttpStatus.GATEWAY_TIMEOUT;
        } else if (ex instanceof FailedToGetSuccessfulResponseException) {
            return HttpStatus.BAD_GATEWAY;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception ex) {

        log.error(ex.getClass().getSimpleName() + ": " + ex.getMessage(), ex);

        HttpStatus status = getStatus(ex);

        ErrorResponseDto errorResponseDto =
                ErrorResponseDto.builder()
                        .status(status.value())
                        .error(status.name())
                        .message(ex.getMessage())
                        .errorDetail(ex.getCause() != null ? ex.getCause().getMessage() : null)
                        .timestamp(LocalDateTime.now())
                        .build();

        return new ResponseEntity<>(errorResponseDto, status);
    }

    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        return getErrorResponse(ex, status, request);
    }

    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            @Nullable Object body,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        return getErrorResponse(ex, status, request);
    }

    private ResponseEntity<Object> getErrorResponse(Exception ex, HttpStatus status, WebRequest request) {
        log.error(ex.getMessage());

        ErrorResponseDto errorResponseDto =
                ErrorResponseDto.builder()
                        .status(status.value())
                        .error(status.name())
                        .message(ex.getMessage())
                        .errorDetail(ex.getCause() != null ? ex.getCause().getMessage() : null)
                        .path(((ServletWebRequest) request).getRequest().getRequestURI())
                        .timestamp(LocalDateTime.now())
                        .build();

        return new ResponseEntity<>(errorResponseDto, status);
    }
}
