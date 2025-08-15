package org.openhab.core.ai.tool.resources;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.model.api.ModelProviderType;

/**
 * Request info for resource-managed executions.
 *
 * Captures request lifecycle data used by `ResourceManager` for accounting
 * and cleanup.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class RequestInfo {
    private final String requestId;
    private final ModelProviderType provider;
    private final int priority;
    private final Instant startTime;
    private final AtomicReference<Instant> endTime = new AtomicReference<>();
    private final AtomicReference<Boolean> success = new AtomicReference<>();
    private final AtomicReference<Exception> error = new AtomicReference<>();

    public RequestInfo(String requestId, ModelProviderType provider, int priority, Instant startTime) {
        this.requestId = requestId;
        this.provider = provider;
        this.priority = priority;
        this.startTime = startTime;
    }

    public String getRequestId() {
        return requestId;
    }

    public ModelProviderType getProvider() {
        return provider;
    }

    public int getPriority() {
        return priority;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime.get();
    }

    public Boolean getSuccess() {
        return success.get();
    }

    public Exception getError() {
        return error.get();
    }

    public void setEndTime(Instant endTime) {
        this.endTime.set(endTime);
    }

    public void setSuccess(boolean success) {
        this.success.set(success);
    }

    public void setError(Exception error) {
        this.error.set(error);
    }

    public Duration getDuration() {
        Instant end = endTime.get();
        return end != null ? Duration.between(startTime, end) : Duration.between(startTime, Instant.now());
    }
}
