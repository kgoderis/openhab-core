package org.openhab.core.ai.auth;

// import java.util.concurrent.atomic.AtomicLong; // Migrated to MetricsService

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.patterns.SystemPerformanceMetrics;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@NonNullByDefault
@Component(service = LogoutPattern.class)
public class LogoutPattern {
    private final String principalId;
    // private final AtomicLong logoutCount = new AtomicLong(0); // Migrated to MetricsService

    @Reference
    private @Nullable MetricsService metricsService;

    public LogoutPattern(String principalId) {
        this.principalId = principalId;
    }

    void recordLogout() {
        // logoutCount.incrementAndGet(); // Migrated to MetricsService
        try {
            MetricsService metrics = metricsService;
            if (metrics != null) {
                SystemPerformanceMetrics.recordMessageLatency(metrics, "logout-pattern",
                    java.time.Duration.ofNanos(0), "logout", true);
            }
        } catch (Exception e) {
            // Graceful degradation - don't fail logout
        }
    }

    long getLogoutCount() {
        return 0; // Migrated to MetricsService
    }
}
