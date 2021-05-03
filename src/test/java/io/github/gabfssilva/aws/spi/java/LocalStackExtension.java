package io.github.gabfssilva.aws.spi.java;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

public class LocalStackExtension implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {
    private static boolean started = false;

    @Override
    public void beforeAll(ExtensionContext context) {
        if (!started) {
            started = true;
            LocalStackInstance.container().start();
            context.getRoot().getStore(GLOBAL).put("localstack", this);
        }
    }

    @Override
    public void close() {
        LocalStackInstance.container().stop();
    }
}