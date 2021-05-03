package io.github.gabfssilva.aws.spi.java;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClientBuilder;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@DisplayName("A test specification to assure that DynamoDB client is working fine with this library")
class DynamoDBServiceSpec extends AwsServiceSpec<DynamoDbAsyncClient, DynamoDbAsyncClientBuilder> {

    @ParameterizedTest(name = "Asserting that I am able to create tables")
    @MethodSource("randomTableName")
    public void createTableTest(final String tableName) throws ExecutionException, InterruptedException {
        final CreateTableRequest request = createTableRequest(tableName);

        final var response = client().createTable(request).get();
        assertThat(response.sdkHttpResponse().isSuccessful(), equalTo(true));
    }

    @ParameterizedTest(name = "Asserting that I am able to put items in a table")
    @MethodSource("randomTableName")
    public void putItemRequest(final String tableName) throws ExecutionException, InterruptedException {
        final CreateTableRequest request = createTableRequest(tableName);
        client().createTable(request).get();

        final var item =
                Map.of("id", AttributeValue.builder().s(randomString()).build());

        final PutItemRequest putItemRequest = putItemRequest(tableName, item);

        final var response = client().putItem(putItemRequest).get();
        assertThat(response.sdkHttpResponse().isSuccessful(), equalTo(true));
    }

    @ParameterizedTest(name = "Asserting that I am able to get items from a table")
    @MethodSource("randomTableName")
    public void getItemRequest(final String tableName) throws ExecutionException, InterruptedException {
        final CreateTableRequest request = createTableRequest(tableName);
        client().createTable(request).get();

        final var randomValue = randomString();

        final var item =
                Map.of("id", AttributeValue.builder().s(randomValue).build());

        final PutItemRequest putItemRequest = putItemRequest(tableName, item);

        client().putItem(putItemRequest).get();

        final var getItemRequest =
                GetItemRequest
                        .builder()
                        .tableName(tableName)
                        .key(item)
                        .attributesToGet("id")
                        .build();

        final var response = client().getItem(getItemRequest).get();

        assertThat(response.item().get("id").s(), equalTo(randomValue));
    }

    private PutItemRequest putItemRequest(String tableName, Map<String, AttributeValue> item) {
        return PutItemRequest
                .builder()
                .tableName(tableName)
                .item(item)
                .build();
    }

    private CreateTableRequest createTableRequest(String tableName) {
        final var schemaElement =
                KeySchemaElement
                        .builder()
                        .keyType(KeyType.HASH)
                        .attributeName("id")
                        .build();

        final var attributeDefinition =
                AttributeDefinition
                        .builder()
                        .attributeName("id")
                        .attributeType(ScalarAttributeType.S)
                        .build();

        final var provisionedThroughput =
                ProvisionedThroughput
                        .builder()
                        .readCapacityUnits(1000L)
                        .writeCapacityUnits(1000L)
                        .build();

        return CreateTableRequest
                .builder()
                .provisionedThroughput(provisionedThroughput)
                .keySchema(schemaElement)
                .attributeDefinitions(attributeDefinition)
                .tableName(tableName)
                .build();
    }

    private static Stream<String> randomTableName() {
        final var randomTopicName = "table-" + randomString();
        return Stream.of(randomTopicName);
    }

    @Override
    public LocalStackContainer.Service enabledService() {
        return LocalStackContainer.Service.DYNAMODB;
    }

    @Override
    public DynamoDbAsyncClientBuilder builder() {
        return DynamoDbAsyncClient.builder();
    }
}