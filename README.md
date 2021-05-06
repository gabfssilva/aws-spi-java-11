# aws-spi-java-11
Netty-free SPI module for AWS SDK 2 using the HTTP api from Java 11

# How to use it?

This library is at early stage and not meant for production use yet. Use at your own risk. 

But please, use it =P. The more people using it, the faster a stable release will be available.

```groovy
repositories {
    maven {
        url "https://s01.oss.sonatype.org/content/repositories/snapshots/"
    }
}


dependencies {
    implementation ('io.github.gabfssilva:aws-spi-java-11:0.0.1-SNAPSHOT')
    
    implementation ('software.amazon.awssdk:sqs:2.16.52') {
        exclude group: 'software.amazon.awssdk', module: 'netty-nio-client'
    }
}
```

And, the code:

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
- KMS

# How to contribute?

PRs are always welcome. The library is pretty simple itself, but we could use having more tests with other SDK modules.

Also, we need more people using it, once I get enough feedback, I'll start publishing stable releases.

# How is it tested?

todo a better description, but basically with Localstack.

# License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details