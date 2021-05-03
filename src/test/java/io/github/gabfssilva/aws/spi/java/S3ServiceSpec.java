package io.github.gabfssilva.aws.spi.java;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@DisplayName("A test specification to assure that S3 client is working fine with this library")
class S3ServiceSpec extends AwsServiceSpec<S3AsyncClient, S3AsyncClientBuilder> {

    @ParameterizedTest(name = "Asserting that I am able to create buckets")
    @MethodSource("randomBucketName")
    public void createBucketTest(final String bucketName) throws ExecutionException, InterruptedException {
        final var response = client().createBucket(CreateBucketRequest.builder().bucket(bucketName).build()).get();
        assertThat(response.sdkHttpResponse().isSuccessful(), equalTo(true));
    }

    @ParameterizedTest(name = "Asserting that I am able to upload artifacts to S3")
    @MethodSource("randomBucketNameAndKey")
    public void uploadArtifactTest(final String bucketName, final String key) throws ExecutionException, InterruptedException {
        client().createBucket(CreateBucketRequest.builder().bucket(bucketName).build()).get();
        final var request = PutObjectRequest.builder().bucket(bucketName).key(key).build();
        final var response = client().putObject(request, AsyncRequestBody.fromBytes("Hello, S3!".getBytes(StandardCharsets.UTF_8))).get();

        assertThat(response.sdkHttpResponse().isSuccessful(), equalTo(true));
    }

    @ParameterizedTest(name = "Asserting that I am able to download artifacts from S3")
    @MethodSource("randomBucketNameAndKey")
    public void downloadArtifactTest(final String bucketName, final String key) throws ExecutionException, InterruptedException {
        client().createBucket(CreateBucketRequest.builder().bucket(bucketName).build()).get();
        final var fileContent =
                "Hello, S3! This file is stored on " +
                        bucketName + "/" +
                        key + ". Since this file is pretty simple, it's just a simple string";

        client().putObject(PutObjectRequest.builder().bucket(bucketName).key(key).build(),
                AsyncRequestBody.fromBytes(fileContent.getBytes(StandardCharsets.UTF_8))).get();

        final var response = client().getObject(GetObjectRequest.builder().bucket(bucketName).key(key).build(), AsyncResponseTransformer.toBytes()).get();

        assertThat(response.asUtf8String(), equalTo(fileContent));
    }

    private static Stream<String> randomBucketName() {
        final var randomQueueName = "bucket-" + randomString();
        return Stream.of(randomQueueName);
    }

    private static Stream<Arguments> randomBucketNameAndKey() {
        final var randomQueueName = "bucket-" + randomString();
        final var key = "key-" + randomString();
        return Stream.of(Arguments.of(randomQueueName, key));
    }

    @Override
    public LocalStackContainer.Service enabledService() {
        return LocalStackContainer.Service.S3;
    }

    @Override
    public S3AsyncClientBuilder builder() {
        return S3AsyncClient.builder();
    }
}