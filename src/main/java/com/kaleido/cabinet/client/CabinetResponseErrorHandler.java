package com.kaleido.cabinet.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

import static org.springframework.http.HttpStatus.Series.*;

public class CabinetResponseErrorHandler implements ResponseErrorHandler {

    private Logger log = LoggerFactory.getLogger(CabinetResponseErrorHandler.class);

    @Override
    public boolean hasError(ClientHttpResponse httpResponse)
            throws IOException {

        return (
                httpResponse.getStatusCode().series() == CLIENT_ERROR
                        || httpResponse.getStatusCode().series() == SERVER_ERROR);
    }

    @Override
    public void handleError(ClientHttpResponse httpResponse)
            throws IOException {
    	
        if (httpResponse.getStatusCode().equals(HttpStatus.BAD_GATEWAY)) {
            throw new CabinetClientHTTPException.CabinetClientBadGatewayException(httpResponse.getStatusCode());
        } else if (httpResponse.getStatusCode().equals(HttpStatus.GATEWAY_TIMEOUT)) {
            throw new CabinetClientHTTPException.CabinetClientGatewayTimeoutException(httpResponse.getStatusCode());
        } else if (httpResponse.getStatusCode().series() == SERVER_ERROR){
            throw new HttpServerErrorException(httpResponse.getStatusCode(), httpResponse.getStatusText());
        } else if (httpResponse.getStatusCode().series() == CLIENT_ERROR){
            throw new HttpClientErrorException(httpResponse.getStatusCode(), httpResponse.getStatusText());
        }
    }
}
