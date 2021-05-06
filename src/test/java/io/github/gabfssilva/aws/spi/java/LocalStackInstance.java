package io.github.gabfssilva.aws.spi.java;

import io.github.gabfssilva.aws.spi.java.utils.ReflectionUtils;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Set;

public class LocalStackInstance {
    private static final Set<LocalStackContainer.Service> services = ReflectionUtils.services();

    private static final LocalStackContainer service =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.12.10"))
                    .withServices(services.toArray(new LocalStackContainer.Service[0]));

    public static LocalStackContainer container() {
        return service;
    }
}
