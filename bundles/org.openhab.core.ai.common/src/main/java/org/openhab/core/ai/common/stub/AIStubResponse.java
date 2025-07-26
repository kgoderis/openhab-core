package org.openhab.core.ai.common.stub;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Response object for stub services.
 * 
 * This class represents responses from stub services, including
 * success/failure status, data, and metadata.
 * 
 * 
 */
public class AIStubResponse {

    private final boolean success;
    private final String message;
    private final Object data;
    private final int statusCode;
    private final Map<String, String> headers;
    private final Instant timestamp;
    private final long processingTimeMs;

    private AIStubResponse(Builder builder) {
        this.success = builder.success;
        this.message = builder.message;
        this.data = builder.data;
        this.statusCode = builder.statusCode;
        this.headers = builder.headers;
        this.timestamp = builder.timestamp != null ? builder.timestamp : Instant.now();
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
        return Optional.ofNullable(headers != null ? headers.get(name) : null);
    }

    /**
     * Get the response timestamp.
     * 
     * @return When the response was created
     */
    public Instant getTimestamp() {
        return timestamp;
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
     * Create a new builder instance.
     * 
     * @return Builder for creating stub responses
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a successful response with data.
     * 
     * @param data Response data
     * @return Successful stub response
     */
    public static AIStubResponse success(Object data) {
        return builder().success(true).data(data).statusCode(200).build();
    }

    /**
     * Create a successful response with message.
     * 
     * @param message Success message
     * @return Successful stub response
     */
    public static AIStubResponse success(String message) {
        return builder().success(true).message(message).statusCode(200).build();
    }

    /**
     * Create an error response.
     * 
     * @param message Error message
     * @return Error stub response
     */
    public static AIStubResponse error(String message) {
        return builder().success(false).message(message).statusCode(500).build();
    }

    /**
     * Create an error response with status code.
     * 
     * @param message Error message
     * @param statusCode HTTP status code
     * @return Error stub response
     */
    public static AIStubResponse error(String message, int statusCode) {
        return builder().success(false).message(message).statusCode(statusCode).build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AIStubResponse that = (AIStubResponse) o;
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
        return "AIStubResponse{" + "success=" + success + ", message='" + message + '\'' + ", statusCode=" + statusCode
                + ", timestamp=" + timestamp + ", processingTimeMs=" + processingTimeMs + ", hasData=" + (data != null)
                + ", headerCount=" + (headers != null ? headers.size() : 0) + '}';
    }

    /**
     * Builder for creating AIStubResponse instances.
     */
    public static class Builder {
        private boolean success = true;
        private String message;
        private Object data;
        private int statusCode = 200;
        private Map<String, String> headers;
        private Instant timestamp;
        private long processingTimeMs = 0;

        private Builder() {
        }

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder data(Object data) {
            this.data = data;
            return this;
        }

        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder processingTimeMs(long processingTimeMs) {
            this.processingTimeMs = processingTimeMs;
            return this;
        }

        public AIStubResponse build() {
            return new AIStubResponse(this);
        }
    }
}
