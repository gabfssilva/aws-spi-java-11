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

todo

# How is it tested?

todo a better description, but basically with Localstack.

# License

todo