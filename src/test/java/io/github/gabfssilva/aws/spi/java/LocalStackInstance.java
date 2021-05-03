package io.github.gabfssilva.aws.spi.java;

import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

public class LocalStackInstance {
    private static final LocalStackContainer service =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.12.10"))
                    .withServices(LocalStackContainer.Service.S3, LocalStackContainer.Service.SQS);

    public static LocalStackContainer container() {
        return service;
    }
}
