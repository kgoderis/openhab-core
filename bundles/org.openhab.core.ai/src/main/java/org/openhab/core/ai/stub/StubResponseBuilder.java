package org.openhab.core.ai.stub;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class StubResponseBuilder {
    boolean success = true;
    @Nullable String message;
    @Nullable Object data;
    int statusCode = 200;
    @Nullable Map<String, String> headers;
    @Nullable Instant timestamp;
    long processingTimeMs = 0;

    public StubResponseBuilder success(boolean success) { this.success = success; return this; }
    public StubResponseBuilder message(@Nullable String message) { this.message = message; return this; }
    public StubResponseBuilder data(@Nullable Object data) { this.data = data; return this; }
    public StubResponseBuilder statusCode(int statusCode) { this.statusCode = statusCode; return this; }
    public StubResponseBuilder headers(@Nullable Map<String, String> headers) { this.headers = headers; return this; }
    public StubResponseBuilder timestamp(@Nullable Instant timestamp) { this.timestamp = timestamp; return this; }
    public StubResponseBuilder processingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; return this; }

    public StubResponse build() { return new StubResponse(this); }
}


