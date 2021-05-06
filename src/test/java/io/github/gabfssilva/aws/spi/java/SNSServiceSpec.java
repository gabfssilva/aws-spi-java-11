package io.github.gabfssilva.aws.spi.java;

import io.github.gabfssilva.aws.spi.java.utils.LocalStackService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.SnsAsyncClientBuilder;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@DisplayName("A test specification to assure that SNS client is working fine with this library")
@LocalStackService(LocalStackContainer.Service.SNS)
class SNSServiceSpec extends AwsServiceSpec<SnsAsyncClient, SnsAsyncClientBuilder> {

    @ParameterizedTest(name = "Asserting that I am able to create topics")
    @MethodSource("randomTopicName")
    public void createTopicTest(final String topicName) throws ExecutionException, InterruptedException {
        final var response = client().createTopic(CreateTopicRequest.builder().name(topicName).build()).get();
        assertThat(response.topicArn(), endsWith(":" + topicName));
    }

    @ParameterizedTest(name = "Asserting that I am able to publish messages to a topic")
    @MethodSource("randomTopicName")
    public void publishMessageTest(final String topicName) throws ExecutionException, InterruptedException {
        final var topicArn = client().createTopic(CreateTopicRequest.builder().name(topicName).build()).get().topicArn();
        final var response = client().publish(PublishRequest.builder().topicArn(topicArn).message("My pretty and nice message").build()).get();
        assertThat(response.messageId(), notNullValue());
    }

    private static Stream<String> randomTopicName() {
        final var randomTopicName = "topic-" + randomString();
        return Stream.of(randomTopicName);
    }

    @Override
    public SnsAsyncClientBuilder builder() {
        return SnsAsyncClient.builder();
    }
}