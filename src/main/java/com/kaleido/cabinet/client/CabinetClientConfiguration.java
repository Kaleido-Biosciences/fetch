/*
 * Copyright (c) 2019. Kaleido Biosciences. All Rights Reserved
 */

package com.kaleido.cabinet.client;


import com.kaleido.cabinet.client.CabinetClient;
import com.kaleido.cabinet.client.CabinetClientHTTPException;
import com.kaleido.cabinet.client.CabinetResponseErrorHandler;
import com.kaleido.fetch.domain.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides the {@code @Beans} needed for the Kapture client. Applications using this library should include this class
 * in the list of scanned classes or packages. {@code @Beans} in this class are configured through externalized configuration
 * such as an {@code application.properties} file via the {@code KaptureClientProperties} class
 * {@see KaptureClientProperties}
 */
@SpringBootApplication
@EnableConfigurationProperties(CabinetClientProperties.class)
public class CabinetClientConfiguration {

    private CabinetClientProperties cabinetClientProperties;


    public CabinetClientConfiguration(CabinetClientProperties cabinetClientProperties) {
        this.cabinetClientProperties = cabinetClientProperties;
    }

   

    /**
     * Configuration for Kapture Client Retry template.  When a request returns an exception related to 502 or 503
     * the service can automatically retry up to a predefined amount of times (Default: 3 including original call)
     * after an incremental amount of wait time (Default 5 seconds, doubling each attempt with default max of 15 seconds)
     * The following KaptureClientProperties can be changed to change the behavior of the retryTemplate
     * <p>
     * retryInterval: The initial delay, in milliseconds, before the request is retried (default: 5000L)
     * retryMultiplier: The value that the delay interval will be multiplied by before each attempt (default: 2.0D)
     * For example an initial delay of 5000ms at multiplier 2.0, the second attempt would wait 10000ms
     * and the third attempt would wait 20000ms.
     * maxRetryInterval: The max delay, in milliseconds, that the retry would wait before retrying. (default: 15000L)
     * maxRequestAttempts: The max amount of times a request can be called.  This value is inclusive of the original attempt.
     * Setting this value to 1 would effectively disable retry.
     **/
    @Bean
    public RetryTemplate cabinetRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        ExponentialBackOffPolicy exponentialBackOffPolicy = new ExponentialBackOffPolicy();
        exponentialBackOffPolicy.setInitialInterval(cabinetClientProperties.getRetryInterval());
        exponentialBackOffPolicy.setMultiplier(cabinetClientProperties.getRetryMultiplier());
        exponentialBackOffPolicy.setMaxInterval(cabinetClientProperties.getMaxRetryInterval());
        retryTemplate.setBackOffPolicy(exponentialBackOffPolicy);
        Map<Class<? extends Throwable>, Boolean> includeExceptions = new HashMap<>();
        includeExceptions.put(CabinetClientHTTPException.CabinetClientGatewayTimeoutException.class, true);
        includeExceptions.put(CabinetClientHTTPException.CabinetClientBadGatewayException.class, true);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(cabinetClientProperties.getMaxRequestAttempts(), includeExceptions);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }

 

    @Bean
    CabinetClient<PlateMap> cabinetClient(RestTemplate restTemplate, RetryTemplate retryTemplate) {
        return new CabinetClient<>(cabinetClientProperties.getBase() + cabinetClientProperties.getCabinetPlatemapEndpoint(),
                cabinetClientProperties.getBase() +
                        cabinetClientProperties.getSearchPathComponent() + "/"
                        + cabinetClientProperties.getCabinetPlatemapEndpoint(),
                restTemplate, retryTemplate, PlateMap.class);
    }

 

   

}
