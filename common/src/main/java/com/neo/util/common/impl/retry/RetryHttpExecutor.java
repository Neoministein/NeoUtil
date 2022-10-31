package com.neo.util.common.impl.retry;

import com.neo.util.common.impl.exception.CommonRuntimeException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * This utility class handles sending and retrying http calls through a apache http client.
 */
public class RetryHttpExecutor {

    protected final RetryExecutor lazyExecutor;

    public RetryHttpExecutor() {
        this(new RetryExecutor());
    }

    protected RetryHttpExecutor(RetryExecutor lazyAction) {
        this.lazyExecutor = lazyAction;
    }

    /**
     * Executes the httpUriRequest via the httpClient and verifies if the response format is correct and reties if it fails
     *
     * @param httpClient the http client which send the request
     * @param httpUriRequest the request to send
     * @param retries the amount of retries available
     *
     * @throws CommonRuntimeException if it fails and no more retries are available
     *
     * @return the response message
     */
    public String execute(HttpClient httpClient, HttpUriRequest httpUriRequest, int retries) {
        Supplier<String> action = () -> {
            try {
                HttpResponse httpResponse = httpClient.execute(httpUriRequest);
                if (httpResponse.getStatusLine().getStatusCode() != 200) {
                    throw new RuntimeException("Http call failed "
                            + "code " + httpResponse.getStatusLine().getStatusCode()
                            + "message " + httpResponse.getStatusLine().getReasonPhrase());
                }
                HttpEntity responseEntity = httpResponse.getEntity();

                return EntityUtils.toString(responseEntity);
            } catch (IOException | ParseException ex) {
                throw new RuntimeException("Error while during lazy http call reason " + ex.getMessage());
            }
        };

        return lazyExecutor.execute(action, retries);
    }
}
