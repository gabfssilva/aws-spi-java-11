package io.github.gabfssilva.aws.spi.java;

import io.github.gabfssilva.aws.spi.java.utils.LocalStackService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsAsyncClient;
import software.amazon.awssdk.services.kms.KmsAsyncClientBuilder;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.EncryptRequest;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@DisplayName("A test specification to assure that KMS client is working fine with this library")
@LocalStackService(LocalStackContainer.Service.KMS)
class KMSServiceSpec extends AwsServiceSpec<KmsAsyncClient, KmsAsyncClientBuilder> {

    @Test
    @DisplayName("Asserting that I am able to create keys")
    public void createKeyTest() throws ExecutionException, InterruptedException {
        final var keyResponse = client().createKey().get();
        assertThat(keyResponse.keyMetadata().keyId(), notNullValue());
    }

    @Test
    @DisplayName("Asserting that I am able to encrypt data")
    public void encryptTest() throws ExecutionException, InterruptedException {
        final var createdKey = client().createKey().get();

        final var request =
                EncryptRequest
                        .builder()
                        .keyId(createdKey.keyMetadata().keyId())
                        .plaintext(SdkBytes.fromString("This is a pretty secure password", StandardCharsets.UTF_8))
                        .build();

        final var response = client().encrypt(request).get();

        assertThat(response.keyId(), endsWith(createdKey.keyMetadata().keyId()));
        assertThat(response.ciphertextBlob(), notNullValue(SdkBytes.class));
    }

    @Test
    @DisplayName("Asserting that I am able to decrypt data")
    public void decryptTest() throws ExecutionException, InterruptedException {
        final var createdKey = client().createKey().get();

        final var prettySecurePassword = "This is a pretty secure password";

        final var encryptResponse = client().encrypt(
                EncryptRequest
                        .builder()
                        .keyId(createdKey.keyMetadata().keyId())
                        .plaintext(SdkBytes.fromString(prettySecurePassword, StandardCharsets.UTF_8))
                        .build()
        ).get();

        final var encryptedData = encryptResponse.ciphertextBlob();
        final var response = client().decrypt(DecryptRequest.builder().ciphertextBlob(encryptedData).build()).get();

        assertThat(response.plaintext().asUtf8String(), equalTo(prettySecurePassword));
    }

    @Override
    public KmsAsyncClientBuilder builder() {
        return KmsAsyncClient.builder();
    }
}