package org.openhab.core.ai.auth;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot;

@NonNullByDefault
public class JWTFailurePattern {
    private final String tokenPrefix;
    private final String reason;
    // Performance monitoring - migrated to MetricsService
    // private final AtomicLong failureCount = new AtomicLong(0);

    private MetricsService metricsService;

    public void setMetricsService(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    JWTFailurePattern(String tokenPrefix, String reason) {
        this.tokenPrefix = tokenPrefix;
        this.reason = reason;
    }

    void recordFailure() {
        if (metricsService != null) {
            try {
                metricsService.recordOperation("jwt_failure", "failure", false, Duration.ZERO);
            } catch (Exception e) {
                // Fallback to local logging if MetricsService fails
                System.err.println("Failed to record JWT failure metrics: " + e.getMessage());
            }
        }
    }

    long getFailureCount() {
        if (metricsService != null) {
            try {
                GenericMetricsSnapshot snapshot = metricsService.getSnapshot("jwt_failure", "failure");
                return snapshot.getMetricAsLong("total_count");
            } catch (Exception e) {
                // Fallback to default value if MetricsService fails
                return 0L;
            }
        }
        return 0L;
    }
}
