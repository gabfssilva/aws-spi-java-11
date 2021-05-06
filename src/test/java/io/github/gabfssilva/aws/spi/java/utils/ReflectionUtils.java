package io.github.gabfssilva.aws.spi.java.utils;

import io.github.gabfssilva.aws.spi.java.AwsServiceSpec;
import org.reflections.Reflections;
import org.testcontainers.containers.localstack.LocalStackContainer;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Don't look at me like this, you had your ReflectionUtils as well.
 * Just leave me alone. >: (
 */
public class ReflectionUtils {
    private static final Reflections reflections = new Reflections("io.github.gabfssilva.aws.spi.java");

    public static Set<LocalStackContainer.Service> services() {
        final var types = reflections.getSubTypesOf(AwsServiceSpec.class);

        return types.stream()
                .map(t -> t.getDeclaredAnnotation(LocalStackService.class))
                .map(LocalStackService::value)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }
}
