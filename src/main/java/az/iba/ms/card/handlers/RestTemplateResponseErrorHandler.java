package az.iba.ms.card.handlers;

import az.iba.ms.card.exceptions.FailedToGetSuccessfulResponseException;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {

    private final String service;

    public RestTemplateResponseErrorHandler(String service) {
        this.service = service;
    }

    @Override
    public boolean hasError(ClientHttpResponse httpResponse) throws IOException {
        return httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR
                || httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR;
    }

    @Override
    public void handleError(ClientHttpResponse httpResponse) throws IOException {
        String msg = service;
        if (httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR) {
            msg += " - Got client error on rest response";
        } else if (httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR) {
            msg += " - Got server error on rest response";
        }

        throw new FailedToGetSuccessfulResponseException(msg);
    }
}
