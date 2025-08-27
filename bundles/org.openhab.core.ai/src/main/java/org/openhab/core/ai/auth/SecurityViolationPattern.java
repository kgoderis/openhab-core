package org.openhab.core.ai.auth;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot;

@NonNullByDefault
public class SecurityViolationPattern {
    private final String principalId;
    private final String violationType;
    private final String protocol;
    // Performance monitoring - migrated to MetricsService
    // private final AtomicLong violationCount = new AtomicLong(0);

    private MetricsService metricsService;

    public void setMetricsService(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    SecurityViolationPattern(String principalId, String violationType, String protocol) {
        this.principalId = principalId;
        this.violationType = violationType;
        this.protocol = protocol;
    }

    void recordViolation() {
        if (metricsService != null) {
            try {
                metricsService.recordOperation("security_violation", "violation", false, Duration.ZERO);
            } catch (Exception e) {
                // Fallback to local logging if MetricsService fails
                System.err.println("Failed to record security violation metrics: " + e.getMessage());
            }
        }
    }

    long getViolationCount() {
        if (metricsService != null) {
            try {
                GenericMetricsSnapshot snapshot = metricsService.getSnapshot("security_violation", "violation");
                return snapshot.getMetricAsLong("total_count");
            } catch (Exception e) {
                // Fallback to default value if MetricsService fails
                return 0L;
            }
        }
        return 0L;
    }
}
