package org.openhab.core.ai.auth;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot;

@NonNullByDefault
public class PermissionCheckPattern {
    private final String principalId;
    private final String permission;
    private final String protocol;
    // Performance monitoring - migrated to MetricsService
    // private final AtomicLong totalChecks = new AtomicLong(0);
    // private final AtomicLong deniedChecks = new AtomicLong(0);

    private MetricsService metricsService;

    public void setMetricsService(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    PermissionCheckPattern(String principalId, String permission, String protocol) {
        this.principalId = principalId;
        this.permission = permission;
        this.protocol = protocol;
    }

    void recordCheck(boolean granted) {
        if (metricsService != null) {
            try {
                metricsService.recordOperation("permission_check", "check").withSuccess(granted).withDuration(0L)
                        .withData("principalId", principalId).withData("permission", permission)
                        .withData("protocol", protocol).record();
            } catch (Exception e) {
                // Fallback to local logging if MetricsService fails
                System.err.println("Failed to record permission check metrics: " + e.getMessage());
            }
        }
    }

    long getTotalChecks() {
        if (metricsService != null) {
            try {
                GenericMetricsSnapshot snapshot = metricsService.getSnapshot("permission_check", "check");
                return snapshot.getMetricAsLong("total_count");
            } catch (Exception e) {
                // Fallback to default value if MetricsService fails
                return 0L;
            }
        }
        return 0L;
    }

    double getDenialRate() {
        if (metricsService != null) {
            try {
                GenericMetricsSnapshot snapshot = metricsService.getSnapshot("permission_check", "check");
                return snapshot.getMetricAsDouble("failure_rate");
            } catch (Exception e) {
                // Fallback to default value if MetricsService fails
                return 0.0;
            }
        }
        return 0.0;
    }
}
