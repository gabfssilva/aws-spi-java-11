# aws-spi-java-11
Netty-free SPI module for AWS SDK 2 using the HTTP api from Java 11

# How to use?

```java
import io.github.gabfssilva.aws.spi.java.*;

...

final var client = AsyncHttpClient.buildDefault();

final var sqsClient = SqsAsyncClient.builder().httpClient(client).build();
```

# Tested against

- SQS
- SNS
- S3
- DynamoDB

# How to contribute?

PRs are always welcome. The library is pretty simple itself, but we could use having more tests with other SDK modules.

Also, we need more people using it, once I get enough feedback, I'll start publishing stable releases.

# How is it tested?

todo a better description, but basically with Localstack.

# License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details