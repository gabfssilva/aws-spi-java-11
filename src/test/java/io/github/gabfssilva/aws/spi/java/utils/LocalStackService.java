package io.github.gabfssilva.aws.spi.java.utils;

import org.testcontainers.containers.localstack.LocalStackContainer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LocalStackService {
    LocalStackContainer.Service value();
}
