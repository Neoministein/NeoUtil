package com.neo.util.common.impl.retry;

import com.neo.util.common.impl.exception.InternalRuntimeException;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
     * @param request the request to send
     * @param retries the amount of retries available
     *
     * @throws InternalRuntimeException if it fails and no more retries are available
     *
     * @return the response message
     */
    public String execute(HttpClient httpClient, HttpRequest request, int retries) {
        Supplier<String> action = () -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    throw new RuntimeException("Http call failed with code " +  response.statusCode());
                }

                return response.body();
            } catch (IOException ex) {
                throw new RuntimeException("Error occurred during lazy http call reason " + ex.getMessage());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("An InterruptedException occurred during lazy http call");
            }
        };

        return lazyExecutor.execute(action, retries);
    }
}
