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

    // ===== PERFORMANCE METRICS ENHANCEMENTS =====

    /**
     * Add operation timing context including complexity and data size.
     * 
     * @param complexity the operation complexity level (1-10 scale)
     * @param inputDataSize the size of input data in bytes
     * @param outputDataSize the size of output data in bytes
     * @return this recorder for method chaining
     */
    public OperationRecorder withTimingContext(int complexity, long inputDataSize, long outputDataSize) {
        if (complexity < 1 || complexity > 10) {
            throw new IllegalArgumentException("complexity must be between 1 and 10");
        }
        if (inputDataSize < 0 || outputDataSize < 0) {
            throw new IllegalArgumentException("data sizes must be >= 0");
        }
        data.put("complexity", complexity);
        data.put("inputDataSize", inputDataSize);
        data.put("outputDataSize", outputDataSize);
        return this;
    }

    /**
     * Add resource utilization context during the operation.
     * 
     * @param memoryUsedBytes the memory used during operation in bytes
     * @param cpuUsagePercent the CPU usage percentage (0-100)
     * @param diskIOTimeMs the disk I/O time in milliseconds
     * @return this recorder for method chaining
     */
    public OperationRecorder withResourceUtilization(long memoryUsedBytes, double cpuUsagePercent, long diskIOTimeMs) {
        if (memoryUsedBytes < 0 || diskIOTimeMs < 0) {
            throw new IllegalArgumentException("memory and disk I/O time must be >= 0");
        }
        if (cpuUsagePercent < 0 || cpuUsagePercent > 100) {
            throw new IllegalArgumentException("CPU usage must be between 0 and 100");
        }
        data.put("memoryUsedBytes", memoryUsedBytes);
        data.put("cpuUsagePercent", cpuUsagePercent);
        data.put("diskIOTimeMs", diskIOTimeMs);
        return this;
    }

    /**
     * Add concurrency metrics for the operation.
     * 
     * @param concurrentOperations the number of concurrent operations
     * @param queueSize the current queue size
     * @param contentionCount the number of contention events
     * @return this recorder for method chaining
     */
    public OperationRecorder withConcurrencyMetrics(int concurrentOperations, int queueSize, int contentionCount) {
        if (concurrentOperations < 0 || queueSize < 0 || contentionCount < 0) {
            throw new IllegalArgumentException("concurrency metrics must be >= 0");
        }
        data.put("concurrentOperations", concurrentOperations);
        data.put("queueSize", queueSize);
        data.put("contentionCount", contentionCount);
        return this;
    }

    /**
     * Add quality metrics with error categorization.
     * 
     * @param errorCategory the category of error (null if no error)
     * @param errorSeverity the severity level (1-5, null if no error)
     * @param retryCount the number of retries attempted
     * @param fallbackUsed whether a fallback mechanism was used
     * @return this recorder for method chaining
     */
    public OperationRecorder withQualityMetrics(String errorCategory, Integer errorSeverity, int retryCount,
            boolean fallbackUsed) {
        if (retryCount < 0) {
            throw new IllegalArgumentException("retryCount must be >= 0");
        }
        if (errorSeverity != null && (errorSeverity < 1 || errorSeverity > 5)) {
            throw new IllegalArgumentException("errorSeverity must be between 1 and 5");
        }
        if (errorCategory != null) {
            data.put("errorCategory", errorCategory);
        }
        if (errorSeverity != null) {
            data.put("errorSeverity", errorSeverity);
        }
        data.put("retryCount", retryCount);
        data.put("fallbackUsed", fallbackUsed);
        return this;
    }

    /**
     * Add user experience metrics from user perspective.
     * 
     * @param userPerceivedLatencyMs the latency perceived by the user in milliseconds
     * @param userInteractionTimeMs the time user spent interacting in milliseconds
     * @param userSatisfactionScore the user satisfaction score (1-5, null if not available)
     * @return this recorder for method chaining
     */
    public OperationRecorder withUserExperienceMetrics(long userPerceivedLatencyMs, long userInteractionTimeMs,
            Integer userSatisfactionScore) {
        if (userPerceivedLatencyMs < 0 || userInteractionTimeMs < 0) {
            throw new IllegalArgumentException("user experience times must be >= 0");
        }
        if (userSatisfactionScore != null && (userSatisfactionScore < 1 || userSatisfactionScore > 5)) {
            throw new IllegalArgumentException("userSatisfactionScore must be between 1 and 5");
        }
        data.put("userPerceivedLatencyMs", userPerceivedLatencyMs);
        data.put("userInteractionTimeMs", userInteractionTimeMs);
        if (userSatisfactionScore != null) {
            data.put("userSatisfactionScore", userSatisfactionScore);
        }
        return this;
    }

    /**
     * Add system health correlation metrics.
     * 
     * @param systemHealthScore the overall system health score (0-100)
     * @param activeAlerts the number of active system alerts
     * @param resourceAvailability the resource availability percentage (0-100)
     * @param systemLoadAverage the system load average
     * @return this recorder for method chaining
     */
    public OperationRecorder withSystemHealthCorrelation(double systemHealthScore, int activeAlerts,
            double resourceAvailability, double systemLoadAverage) {
        if (systemHealthScore < 0 || systemHealthScore > 100) {
            throw new IllegalArgumentException("systemHealthScore must be between 0 and 100");
        }
        if (activeAlerts < 0) {
            throw new IllegalArgumentException("activeAlerts must be >= 0");
        }
        if (resourceAvailability < 0 || resourceAvailability > 100) {
            throw new IllegalArgumentException("resourceAvailability must be between 0 and 100");
        }
        if (systemLoadAverage < 0) {
            throw new IllegalArgumentException("systemLoadAverage must be >= 0");
        }
        data.put("systemHealthScore", systemHealthScore);
        data.put("activeAlerts", activeAlerts);
        data.put("resourceAvailability", resourceAvailability);
        data.put("systemLoadAverage", systemLoadAverage);
        return this;
    }

    /**
     * Record the operation with all collected data.
     */
    public void record() {
        service.recordOperationWithData(domain, operation, success, Duration.ofNanos(durationNanos), data);
    }
}
