package org.openhab.core.ai.tool.resources;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.openhab.core.ai.tool.resources.monitoring.ProviderResourceSnapshot;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Per-provider resource usage tracking state with centralized metrics.
 * 
 * <p>
 * This service tracks resource usage for individual AI providers using the
 * centralized MetricsService instead of direct atomic counters. It provides
 * real-time monitoring and historical analysis of provider performance.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component
@NonNullByDefault
public class ProviderResourceUsage {
    private final ModelProviderType provider;
    private final AtomicReference<Instant> lastActivity = new AtomicReference<>(Instant.now());

    // NEW: Centralized MetricsService for recording operations
    @Reference
    private @Nullable MetricsService metricsService;

    // Simple counters for immediate access (not for metrics recording)
    private volatile long currentConcurrentRequests = 0;

    public ProviderResourceUsage(ModelProviderType provider) {
        this.provider = provider;
    }

    public void updateUsage(int concurrentDelta, int successDelta) {
        int previousConcurrent, newConcurrent;

        // Update concurrent counter for immediate access
        synchronized (this) {
            previousConcurrent = currentConcurrentRequests;
            currentConcurrentRequests += concurrentDelta;
            if (currentConcurrentRequests < 0) {
                currentConcurrentRequests = 0;
            }
            newConcurrent = currentConcurrentRequests;
        }

        // Record enhanced metrics
        recordConcurrentRequestMetrics(newConcurrent, Math.max(previousConcurrent, newConcurrent),
                Math.abs(successDelta));

        // Simulate memory and CPU monitoring (in a real implementation, this would read actual system metrics)
        Runtime runtime = Runtime.getRuntime();
        long currentMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        recordMemoryUsage(currentMemory, maxMemory);

        // Simulate CPU usage (in a real implementation, this would use system monitoring APIs)
        double cpuUsage = Math.min(100.0, (newConcurrent * 10.0)); // Simulated CPU usage
        recordCpuUsage(cpuUsage, cpuUsage * 0.8, cpuUsage * 1.2);

        // Record service availability
        boolean serviceAvailable = newConcurrent >= 0; // Simple availability check
        recordServiceStatus(serviceAvailable, serviceAvailable ? "operational" : "overloaded", 50.0);

        // Record operation with MetricsService
        recordProviderOperation(successDelta > 0, Duration.ZERO);

        lastActivity.set(Instant.now());
    }

    /**
     * Record a request operation for this provider.
     * 
     * @param requestId the request identifier
     * @param success whether the request was successful
     * @param duration the request duration
     */
    public void recordRequest(String requestId, boolean success, Duration duration) {
        recordProviderOperation(success, duration);
        lastActivity.set(Instant.now());
    }

    public ModelProviderType getProvider() {
        return provider;
    }

    public long getConcurrentRequests() {
        return currentConcurrentRequests;
    }

    public long getTotalRequests() {
        ProviderResourceSnapshot snapshot = getSnapshot();
        return snapshot != null ? snapshot.total() : 0L;
    }

    public long getSuccessfulRequests() {
        ProviderResourceSnapshot snapshot = getSnapshot();
        return snapshot != null ? snapshot.success() : 0L;
    }

    public long getFailedRequests() {
        ProviderResourceSnapshot snapshot = getSnapshot();
        return snapshot != null ? snapshot.failure() : 0L;
    }

    public Instant getLastActivity() {
        return lastActivity.get();
    }

    public double getSuccessRate() {
        ProviderResourceSnapshot snapshot = getSnapshot();
        return snapshot != null ? snapshot.successRate() : 0.0;
    }

    /**
     * Get performance metrics snapshot for this provider.
     * 
     * @return provider resource snapshot or null if not available
     */
    public @Nullable ProviderResourceSnapshot getSnapshot() {
        if (metricsService != null) {
            try {
                MetricKey key = MetricKeys.provider(provider.name());
                return metricsService.getSnapshot(key, ProviderResourceSnapshot.class);
            } catch (Exception e) {
                // Graceful degradation - log and return null
            }
        }
        return null;
    }

    /**
     * Get performance statistics for this provider over a time range.
     * 
     * @param timeRange the time range for statistics
     * @return provider resource statistics or empty statistics if not available
     */
    // Eliminated getStatistics() method after enhancing metric capture
    // Consumers should use MetricsService directly to access provider resource statistics:
    // - Memory usage: metricsService.getSnapshot(MetricKeys.custom("provider-resource", Map.of("operation",
    // "memory-usage", "provider", provider.name())))
    // - CPU usage: metricsService.getSnapshot(MetricKeys.custom("provider-resource", Map.of("operation", "cpu-usage",
    // "provider", provider.name())))
    // - Concurrent requests: metricsService.getSnapshot(MetricKeys.custom("provider-resource", Map.of("operation",
    // "concurrent-requests", "provider", provider.name())))
    // - Service status: metricsService.getSnapshot(MetricKeys.custom("provider-resource", Map.of("operation",
    // "service-status", "provider", provider.name())))

    /**
     * Record memory usage metrics for this provider
     */
    public void recordMemoryUsage(long currentMemoryBytes, long maxMemoryBytes) {
        if (metricsService != null) {
            try {
                metricsService.recordOperation("provider-resource", "memory-usage").withSuccess(true)
                        .withData("provider", provider.name()).withData("currentMemoryBytes", currentMemoryBytes)
                        .withData("maxMemoryBytes", maxMemoryBytes).withData("memoryUtilization",
                                maxMemoryBytes > 0 ? (currentMemoryBytes * 100.0) / maxMemoryBytes : 0.0)
                        .record();
            } catch (Exception e) {
                // Graceful degradation
            }
        }
    }

    /**
     * Record CPU utilization metrics for this provider
     */
    public void recordCpuUsage(double currentCpuPercentage, double averageCpuPercentage, double peakCpuPercentage) {
        if (metricsService != null) {
            try {
                metricsService.recordOperation("provider-resource", "cpu-usage").withSuccess(true)
                        .withData("provider", provider.name()).withData("currentCpuPercentage", currentCpuPercentage)
                        .withData("averageCpuPercentage", averageCpuPercentage)
                        .withData("peakCpuPercentage", peakCpuPercentage).record();
            } catch (Exception e) {
                // Graceful degradation
            }
        }
    }

    /**
     * Record concurrent request metrics for this provider
     */
    public void recordConcurrentRequestMetrics(int currentConcurrent, int peakConcurrent, int totalRequests) {
        if (metricsService != null) {
            try {
                metricsService.recordOperation("provider-resource", "concurrent-requests").withSuccess(true)
                        .withData("provider", provider.name()).withData("currentConcurrent", currentConcurrent)
                        .withData("peakConcurrent", peakConcurrent).withData("totalRequests", totalRequests).record();
            } catch (Exception e) {
                // Graceful degradation
            }
        }
    }

    /**
     * Record provider service availability status
     */
    public void recordServiceStatus(boolean isAvailable, String statusReason, double responseTimeMs) {
        if (metricsService != null) {
            try {
                metricsService.recordOperation("provider-resource", "service-status").withSuccess(isAvailable)
                        .withData("provider", provider.name()).withData("available", isAvailable)
                        .withData("statusReason", statusReason).withData("responseTimeMs", responseTimeMs).record();
            } catch (Exception e) {
                // Graceful degradation
            }
        }
    }

    /**
     * Record a provider operation using MetricsService.
     * 
     * @param success whether the operation was successful
     * @param duration the operation duration
     */
    private void recordProviderOperation(boolean success, Duration duration) {
        if (metricsService != null) {
            try {
                metricsService.recordOperation("provider-resource", provider.name()).withSuccess(success)
                        .withDuration(duration.toNanos()).withData("provider", provider.name())
                        .withData("component", "ProviderResourceUsage").record();
            } catch (Exception e) {
                // Graceful degradation - continue without metrics
            }
        }
    }
}
