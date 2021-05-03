package io.github.gabfssilva.aws.spi.java;

import io.reactivex.rxjava3.core.Flowable;
import org.reactivestreams.FlowAdapters;
import software.amazon.awssdk.http.SdkHttpFullResponse;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.http.async.AsyncExecuteRequest;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class AsyncHttpClient implements SdkAsyncHttpClient {
    private static final Set<String> headersToSkip = Set.of("Host", "Content-Length", "Expect");

    private final HttpClient httpClient;

    public AsyncHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public CompletableFuture<Void> execute(AsyncExecuteRequest request) {
        final var responseHandler = request.responseHandler();

        try {
            return httpClient
                    .sendAsync(extractRequest(request), HttpResponse.BodyHandlers.ofPublisher())
                    .handle((response, error) -> {
                        if (error != null) {
                            responseHandler.onError(error);
                            return null;
                        }

                        responseHandler.onHeaders(extractHeaders(response));
                        responseHandler.onStream(extractResponse(response));

                        return null;
                    });
        } catch (Throwable e) {
            responseHandler.onError(e);
            return null;
        }
    }

    @Override
    public void close() {
    }

    public static AsyncHttpClientBuilder builder() {
        return new AsyncHttpClientBuilder();
    }

    public static SdkAsyncHttpClient buildDefault() {
        return builder().build();
    }

    private SdkHttpFullResponse extractHeaders(HttpResponse<java.util.concurrent.Flow.Publisher<java.util.List<ByteBuffer>>> response) {
        return SdkHttpResponse
                .builder()
                .headers(response.headers().map())
                .statusCode(response.statusCode())
                .build();
    }

    private Flowable<ByteBuffer> extractResponse(HttpResponse<java.util.concurrent.Flow.Publisher<java.util.List<ByteBuffer>>> response) {
        return Flowable.fromPublisher(FlowAdapters.toPublisher(response.body())).flatMapIterable(x -> x);
    }

    private HttpRequest.BodyPublisher extractBody(AsyncExecuteRequest request) {
        final var publisher = FlowAdapters.toFlowPublisher(request.requestContentPublisher());
        final var contentLength = extractContentLength(request);

        if (contentLength < 1) return HttpRequest.BodyPublishers.fromPublisher(publisher);
        return HttpRequest.BodyPublishers.fromPublisher(publisher, contentLength);
    }

    private long extractContentLength(AsyncExecuteRequest request) {
        final var headers = request.request().headers();
        final var contentLength = headers.get("Content-Length");

        if (nullOrEmpty(contentLength)) return -1;
        return Long.parseLong(contentLength.get(0));
    }

    private boolean nullOrEmpty(List<String> contentLength) {
        return contentLength == null || contentLength.isEmpty();
    }

    private Boolean expectContinue(AsyncExecuteRequest request) {
        final var expectHeader = request.request().headers().get("Expect");
        if (nullOrEmpty(expectHeader)) return false;
        return Objects.equals(expectHeader.get(0), "100-continue");
    }

    private HttpRequest extractRequest(AsyncExecuteRequest request) {
        final var req = request.request();

        HttpRequest.Builder builder = HttpRequest.newBuilder(req.getUri());
        req.headers().forEach((key, values) -> {
            if (!shouldSkipHeader(key)) values.forEach(value -> builder.header(key, value));
        });

        return builder
                .expectContinue(expectContinue(request))
                .method(req.method().name(), extractBody(request)).build();
    }

    private boolean shouldSkipHeader(String key) {
        return headersToSkip.contains(key);
    }
}
