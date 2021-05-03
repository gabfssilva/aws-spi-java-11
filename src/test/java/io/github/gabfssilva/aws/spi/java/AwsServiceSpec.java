package io.github.gabfssilva.aws.spi.java;

import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.awscore.client.builder.AwsAsyncClientBuilder;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.core.client.builder.SdkClientBuilder;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;

import java.net.URI;
import java.util.UUID;

@ExtendWith(LocalStackExtension.class)
public abstract class AwsServiceSpec<C extends SdkClient, B extends AwsAsyncClientBuilder<B, C> & SdkClientBuilder<B, C>> {
    private C client;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    protected LocalStackContainer container = LocalStackInstance.container();

    protected abstract LocalStackContainer.Service enabledService();
    protected abstract AwsAsyncClientBuilder<B, C> builder();

    protected C client() {
        if (client == null) client = builder().httpClient(httpClient()).endpointOverride(endpoint()).build();
        return client;
    }

    protected URI endpoint() {
        return container.getEndpointOverride(enabledService());
    }

    protected SdkAsyncHttpClient httpClient() {
        return AsyncHttpClient.builder().build();
    }

    protected static String randomString() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }
}
