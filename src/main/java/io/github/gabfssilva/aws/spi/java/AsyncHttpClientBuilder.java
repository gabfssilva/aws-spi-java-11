package io.github.gabfssilva.aws.spi.java;

import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.utils.AttributeMap;

import java.net.http.HttpClient;

public class AsyncHttpClientBuilder implements SdkAsyncHttpClient.Builder<AsyncHttpClientBuilder> {
    private HttpClient httpClient;

    @Override
    public SdkAsyncHttpClient buildWithDefaults(AttributeMap serviceDefaults) {
        return new AsyncHttpClient(httpClient());
    }

    public AsyncHttpClientBuilder withHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    private HttpClient httpClient(){
        if (httpClient == null) httpClient = HttpClient.newHttpClient();
        return httpClient;
    }
}
