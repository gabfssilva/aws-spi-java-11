package io.github.gabfssilva.aws.spi.java;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.gabfssilva.aws.spi.java.utils.FileUtils;
import io.github.gabfssilva.aws.spi.java.utils.ListUtils;
import io.reactivex.rxjava3.core.Flowable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
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

    @ParameterizedTest(name = "Asserting that I am able to upload artifacts to S3 using multi-part upload")
    @MethodSource("randomBucketNameAndKey")
    public void multipartUpload(final String bucketName, final String key) throws Exception {
        client().createBucket(CreateBucketRequest.builder().bucket(bucketName).build()).get();

        final List<File> chunks = createChunks();

        final var createMultipartUploadRequest =
                CreateMultipartUploadRequest
                        .builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType("application/json")
                        .build();

        final var createMultipartUploadResponse = client().createMultipartUpload(createMultipartUploadRequest).get();
        final var uploadId = createMultipartUploadResponse.uploadId();

        assertThat(createMultipartUploadResponse.sdkHttpResponse().isSuccessful(), equalTo(true));
        assertThat(uploadId, notNullValue());

        final var etags = ListUtils.zipWithIndex(chunks).stream().map(tuple -> {
            final var chunk = tuple.right();
            final var part = tuple.left() + 1;

            final var request =
                    UploadPartRequest
                            .builder()
                            .bucket(bucketName)
                            .key(key)
                            .uploadId(uploadId)
                            .partNumber(part)
                            .build();

            try {
                return client().uploadPart(request, AsyncRequestBody.fromFile(chunk)).get().eTag();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        final var completedMultipartUpload =
                CompletedMultipartUpload
                        .builder()
                        .parts(ListUtils.zipWithIndex(etags).stream().map(tuple -> {
                            final var part = tuple.left() + 1;
                            final var etag = tuple.right();

                            return CompletedPart.builder().partNumber(part).eTag(etag).build();
                        }).toArray(CompletedPart[]::new))
                        .build();

        final var request =
                CompleteMultipartUploadRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .uploadId(uploadId)
                        .multipartUpload(completedMultipartUpload)
                        .build();

        final var response = client().completeMultipartUpload(request).get();

        assertThat(response.sdkHttpResponse().isSuccessful(), equalTo(true));

        final var getObjectRequest = GetObjectRequest.builder().key(key).bucket(bucketName).build();
        final var stream = client().getObject(getObjectRequest, AsyncResponseTransformer.toBytes());
        final var integers = Arrays.stream(new ObjectMapper().readValue(stream.get().asByteArray(), Integer[].class)).collect(Collectors.toList());
        final var sum = integers.stream().mapToInt(i -> i).sum();

        assertThat(sum, equalTo(IntStream.rangeClosed(0, 10000000).sum()));
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

    private List<File> createChunks() throws Exception {
        final var jsonStart = Flowable.just("[");

        final var content =
                Flowable.rangeLong(1, 10000000)
                        .flatMapIterable(e -> Arrays.asList(e.toString(), ","))
                        .skipLast(1);

        final var jsonEnd = Flowable.just("]");

        final var fileStream =
                Flowable.concat(jsonStart, content, jsonEnd);

        final var path = Files.createTempFile("numbers", ".json");

        fileStream
                .buffer(10000)
                .forEach(chunk -> Files.write(path, chunk, StandardOpenOption.APPEND));

        return FileUtils.splitFile(path.toFile(), 6);
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