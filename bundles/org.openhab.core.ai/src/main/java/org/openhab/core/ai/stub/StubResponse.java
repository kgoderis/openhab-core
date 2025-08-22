package org.openhab.core.ai.stub;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.response.Response;

/**
 * Response object for stub services.
 * 
 * This class represents responses from stub services, including
 * success/failure status, data, and metadata.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class StubResponse implements Response<Object> {

    private final boolean success;
    private final @Nullable String message;
    private final @Nullable Object data;
    private final int statusCode;
    private final @Nullable Map<String, String> headers;
    private final Instant timestamp;
    private final long processingTimeMs;

    private StubResponse(Builder builder) {
        this.success = builder.success;
        this.message = builder.message;
        this.data = builder.data;
        this.statusCode = builder.statusCode;
        this.headers = builder.headers != null ? Map.copyOf(builder.headers) : null;
        this.timestamp = Instant.ofEpochMilli(builder.timestamp);
        this.processingTimeMs = builder.processingTimeMs;
    }

    /**
     * Check if the response indicates success.
     * 
     * @return true if successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Get the response message.
     * 
     * @return Response message, or empty if not set
     */
    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
    }

    /**
     * Get the response data.
     * 
     * @return Response data, or empty if not set
     */
    public Optional<Object> getData() {
        return Optional.ofNullable(data);
    }

    /**
     * Get typed response data.
     * 
     * @param <T> Expected data type
     * @param type Expected type class
     * @return Typed response data, or empty if wrong type or not set
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getData(Class<T> type) {
        if (data != null && type.isInstance(data)) {
            return Optional.of((T) data);
        }
        return Optional.empty();
    }

    /**
     * Get the HTTP status code.
     * 
     * @return Status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Get response headers.
     * 
     * @return Headers map, or empty if not set
     */
    public Optional<Map<String, String>> getHeaders() {
        return Optional.ofNullable(headers);
    }

    /**
     * Get a specific header value.
     * 
     * @param name Header name
     * @return Header value, or empty if not found
     */
    public Optional<String> getHeader(String name) {
        if (headers != null && headers.containsKey(name)) {
            return Optional.ofNullable(headers.get(name));
        }
        return Optional.empty();
    }

    /**
     * Get the response timestamp.
     * 
     * @return Response timestamp as Instant
     */
    public Instant getTimestampInstant() {
        return timestamp;
    }

    // Response interface implementation
    @Override
    public String getId() {
        return String.valueOf(timestamp.toEpochMilli());
    }

    @Override
    public String getErrorMessage() {
        return success ? null : message;
    }

    @Override
    public long getTimestamp() {
        return timestamp.toEpochMilli();
    }

    /**
     * Get the processing time in milliseconds.
     * 
     * @return Processing time
     */
    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    /**
     * Create a new builder for StubResponse.
     * 
     * @return Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a builder from this instance for modification.
     * 
     * @return Builder instance with current values
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Create a successful response with data.
     * 
     * @param data Response data
     * @return StubResponse instance
     */
    public static StubResponse success(Object data) {
        return builder().withSuccess(true).withData(data).build();
    }

    /**
     * Create a successful response with message.
     * 
     * @param message Success message
     * @return StubResponse instance
     */
    public static StubResponse success(String message) {
        return builder().withSuccess(true).withMessage(message).build();
    }

    /**
     * Create an error response with message.
     * 
     * @param message Error message
     * @return StubResponse instance
     */
    public static StubResponse error(String message) {
        return builder().withSuccess(false).withMessage(message).withStatusCode(500).build();
    }

    /**
     * Create an error response with message and status code.
     * 
     * @param message Error message
     * @param statusCode HTTP status code
     * @return StubResponse instance
     */
    public static StubResponse error(String message, int statusCode) {
        return builder().withSuccess(false).withMessage(message).withStatusCode(statusCode).build();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StubResponse that = (StubResponse) o;
        return success == that.success && statusCode == that.statusCode && processingTimeMs == that.processingTimeMs
                && Objects.equals(message, that.message) && Objects.equals(data, that.data)
                && Objects.equals(headers, that.headers) && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, message, data, statusCode, headers, timestamp, processingTimeMs);
    }

    @Override
    public String toString() {
        return "StubResponse{" + "success=" + success + ", message='" + message + '\'' + ", data=" + data
                + ", statusCode=" + statusCode + ", headers=" + headers + ", timestamp=" + timestamp
                + ", processingTimeMs=" + processingTimeMs + '}';
    }

    /**
     * Builder for StubResponse.
     * 
     * @author Karel Goderis - Initial Contribution
     * @since 1.0.0
     */
    public static final class Builder {
        private boolean success = true;
        private String message = "";
        private @Nullable Object data;
        private int statusCode = 200;
        private @Nullable Map<String, String> headers;
        private long timestamp = System.currentTimeMillis();
        private long processingTimeMs = 0L;

        public Builder() {
            // Default constructor
        }

        public Builder(StubResponse source) {
            this.success = source.success;
            this.message = source.message != null ? source.message : "";
            this.data = source.data;
            this.statusCode = source.statusCode;
            this.headers = source.headers;
            this.timestamp = source.timestamp.toEpochMilli();
            this.processingTimeMs = source.processingTimeMs;
        }

        public Builder withSuccess(boolean success) {
            this.success = success;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = Objects.requireNonNull(message, "message");
            return this;
        }

        public Builder withData(@Nullable Object data) {
            this.data = data;
            return this;
        }

        public Builder withStatusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder withHeaders(@Nullable Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder withTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withProcessingTimeMs(long processingTimeMs) {
            this.processingTimeMs = processingTimeMs;
            return this;
        }

        public StubResponse build() {
            if (statusCode < 100 || statusCode > 599) {
                throw new IllegalArgumentException("Status code must be between 100 and 599, got: " + statusCode);
            }
            if (timestamp < 0) {
                throw new IllegalArgumentException("Timestamp must be non-negative, got: " + timestamp);
            }
            if (processingTimeMs < 0) {
                throw new IllegalArgumentException("Processing time must be non-negative, got: " + processingTimeMs);
            }
            return new StubResponse(this);
        }
    }
}
