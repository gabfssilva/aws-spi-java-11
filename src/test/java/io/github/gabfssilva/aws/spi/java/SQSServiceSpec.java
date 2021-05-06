package io.github.gabfssilva.aws.spi.java;

import io.github.gabfssilva.aws.spi.java.utils.LocalStackService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClientBuilder;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@DisplayName("A test specification to assure that SQS client is working fine with this library")
@LocalStackService(LocalStackContainer.Service.SQS)
class SQSServiceSpec extends AwsServiceSpec<SqsAsyncClient, SqsAsyncClientBuilder> {

    @ParameterizedTest(name = "Asserting that I am able to create queues")
    @MethodSource("randomQueueName")
    public void createQueueTest(final String queueName) throws ExecutionException, InterruptedException {
        final var response = client().createQueue(CreateQueueRequest.builder().queueName(queueName).build()).get();
        assertThat(response.queueUrl(), endsWith("/" + queueName));
    }

    @ParameterizedTest(name = "Asserting that I am able to get queue urls")
    @MethodSource("randomQueueName")
    public void getQueueUrlTest(final String queueName) throws ExecutionException, InterruptedException {
        client().createQueue(CreateQueueRequest.builder().queueName(queueName).build()).get();
        final var response = client().getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build()).get();
        assertThat(response.queueUrl(), endsWith("/" + queueName));
    }

    @ParameterizedTest(name = "Asserting that I am able to send messages to a queue")
    @MethodSource("randomQueueName")
    public void sendMessageTest(final String queueName) throws ExecutionException, InterruptedException {
        final var queueUrl = client().createQueue(CreateQueueRequest.builder().queueName(queueName).build()).get().queueUrl();
        final var request = SendMessageRequest.builder().messageBody("Hello, Java HTTP api!").queueUrl(queueUrl).build();
        final var response = client().sendMessage(request).get();
        assertThat(response.messageId(), notNullValue());
    }

    @ParameterizedTest(name = "Asserting that I am able to receive messages from a queue")
    @MethodSource("randomQueueName")
    public void receiveMessageTest(final String queueName) throws ExecutionException, InterruptedException {
        final var queueUrl = client().createQueue(CreateQueueRequest.builder().queueName(queueName).build()).get().queueUrl();
        final var messageBody = "Hello, Java HTTP api!";
        final var request = SendMessageRequest.builder().messageBody(messageBody).queueUrl(queueUrl).build();
        client().sendMessage(request).get();

        final var response = client().receiveMessage(ReceiveMessageRequest.builder().queueUrl(queueUrl).build()).get();

        assertThat(response.messages().size(), equalTo(1));
        assertThat(response.messages().stream().map(m -> m.body()).collect(Collectors.toList()), hasItem(messageBody));
    }

    private static Stream<String> randomQueueName() {
        final var randomQueueName = "queue-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        return Stream.of(randomQueueName);
    }

    @Override
    public SqsAsyncClientBuilder builder() {
        return SqsAsyncClient.builder();
    }
}