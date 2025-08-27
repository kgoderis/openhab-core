package org.openhab.core.ai.auth;

import java.time.Duration;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot;

@NonNullByDefault
public class AuthenticationPattern {
    private final String principalId;
    private final String protocol;
    // Performance monitoring - migrated to MetricsService
    // private final AtomicLong totalAttempts = new AtomicLong(0);
    // private final AtomicLong failedAttempts = new AtomicLong(0);

    private MetricsService metricsService;

    public void setMetricsService(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    AuthenticationPattern(String principalId, String protocol) {
        this.principalId = principalId;
        this.protocol = protocol;
    }

    void recordAttempt(boolean success) {
        if (metricsService != null) {
            try {
                metricsService.recordOperation("authentication", "attempt", success, Duration.ZERO);
            } catch (Exception e) {
                // Fallback to local logging if MetricsService fails
                System.err.println("Failed to record authentication attempt metrics: " + e.getMessage());
            }
        }
    }

    long getTotalAttempts() {
        if (metricsService != null) {
            try {
                GenericMetricsSnapshot snapshot = metricsService.getSnapshot("authentication", "attempt");
                return snapshot.getMetricAsLong("total_count");
            } catch (Exception e) {
                // Fallback to default value if MetricsService fails
                return 0L;
            }
        }
        return 0L;
    }

    double getFailureRate() {
        if (metricsService != null) {
            try {
                GenericMetricsSnapshot snapshot = metricsService.getSnapshot("authentication", "attempt");
                return snapshot.getMetricAsDouble("failure_rate");
            } catch (Exception e) {
                // Fallback to default value if MetricsService fails
                return 0.0;
            }
        }
        return 0.0;
    }
}
