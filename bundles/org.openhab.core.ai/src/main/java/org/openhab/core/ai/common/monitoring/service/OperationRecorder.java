package org.openhab.core.ai.common.monitoring.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricsService;

/**
 * Builder pattern for flexible operation data recording.
 * 
 * <p>
 * This class provides a fluent API for recording operation metrics with additional
 * domain-specific data. It supports arbitrary key-value data storage and validation.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class OperationRecorder {

    private final MetricsService service;
    private final String domain;
    private final String operation;
    private final Map<String, Object> data = new HashMap<>();
    private boolean success;
    private long durationNanos;

    /**
     * Create a new operation recorder.
     * 
     * @param service the metrics service
     * @param domain the operation domain
     * @param operation the operation name
     */
    public OperationRecorder(MetricsService service, String domain, String operation) {
        this.service = Objects.requireNonNull(service, "service");
        this.domain = Objects.requireNonNull(domain, "domain");
        this.operation = Objects.requireNonNull(operation, "operation");
    }

    /**
     * Set the operation success status.
     * 
     * @param success whether the operation was successful
     * @return this recorder for method chaining
     */
    public OperationRecorder withSuccess(boolean success) {
        this.success = success;
        return this;
    }

    /**
     * Set the operation duration.
     * 
     * @param durationNanos the operation duration in nanoseconds
     * @return this recorder for method chaining
     */
    public OperationRecorder withDuration(long durationNanos) {
        if (durationNanos < 0) {
            throw new IllegalArgumentException("durationNanos must be >= 0");
        }
        this.durationNanos = durationNanos;
        return this;
    }

    /**
     * Add arbitrary data to the operation record.
     * 
     * @param key the data key
     * @param value the data value
     * @return this recorder for method chaining
     */
    public OperationRecorder withData(String key, Object value) {
        Objects.requireNonNull(key, "key");
        if (key.isBlank()) {
            throw new IllegalArgumentException("key must not be blank");
        }
        data.put(key, value);
        return this;
    }

    /**
     * Add multiple data entries to the operation record.
     * 
     * @param dataMap the data map to add
     * @return this recorder for method chaining
     */
    public OperationRecorder withData(Map<String, Object> dataMap) {
        Objects.requireNonNull(dataMap, "dataMap");
        data.putAll(dataMap);
        return this;
    }

    /**
     * Record the operation with all collected data.
     */
    public void record() {
        service.recordOperationWithData(domain, operation, success, Duration.ofNanos(durationNanos), data);
    }
}
