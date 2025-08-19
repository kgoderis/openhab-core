package org.openhab.core.ai.common.response;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.builder.AbstractBuilder;
import org.openhab.core.ai.stub.StubResponse;

/**
 * Builder for {@link StubResponse} in the unified response hierarchy.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class StubResponseBuilder extends AbstractBuilder<StubResponse> {

    private @Nullable String id;
    private boolean success = true;
    private String message = "";
    private @Nullable Object data;
    private int statusCode = 200;
    private Map<String, String> headers = Map.of();
    private long timestamp = System.currentTimeMillis();
    private long processingTimeMs = 0L;

    public StubResponseBuilder withId(String id) {
        this.id = Objects.requireNonNull(id, "id");
        return this;
    }

    public StubResponseBuilder success(boolean success) {
        this.success = success;
        return this;
    }

    public StubResponseBuilder message(String message) {
        this.message = Objects.requireNonNull(message, "message");
        return this;
    }

    public StubResponseBuilder data(Object data) {
        this.data = data;
        return this;
    }

    public StubResponseBuilder statusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public StubResponseBuilder headers(Map<String, String> headers) {
        this.headers = Objects.requireNonNull(headers, "headers");
        return this;
    }

    public StubResponseBuilder timestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public StubResponseBuilder processingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
        return this;
    }

    @Override
    protected void validate() {
        // No specific validation required for StubResponse
    }

    @Override
    protected void doReset() {
        id = null;
        success = true;
        message = "";
        data = null;
        statusCode = 200;
        headers = Map.of();
        timestamp = System.currentTimeMillis();
        processingTimeMs = 0L;
    }

    @Override
    public StubResponse build() {
        if (!isValid()) {
            throw new IllegalArgumentException("Invalid StubResponseBuilder state: " + getValidationErrors());
        }
        return new StubResponse(this);
    }

    // Getters for the StubResponse constructor
    public boolean getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }
}
