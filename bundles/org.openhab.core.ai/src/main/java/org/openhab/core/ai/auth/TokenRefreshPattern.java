package org.openhab.core.ai.auth;

// import java.util.concurrent.atomic.AtomicLong; // Migrated to MetricsService

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.patterns.SystemPerformanceMetrics;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = TokenRefreshPattern.class)
@NonNullByDefault
public class TokenRefreshPattern {
    private final String principalId;
    // private final AtomicLong refreshCount = new AtomicLong(0); // Migrated to MetricsService
    
    @Reference
    private @Nullable MetricsService metricsService;

    TokenRefreshPattern(String principalId) {
        this.principalId = principalId;
    }

    void recordRefresh() {
        // refreshCount.incrementAndGet(); // Migrated to MetricsService
        try {
            MetricsService metrics = metricsService;
            if (metrics != null) {
                SystemPerformanceMetrics.recordMessageLatency(metrics, "token-refresh-pattern",
                    java.time.Duration.ofNanos(0), "refresh", true);
            }
        } catch (Exception e) {
            // Graceful degradation - don't fail token refresh
        }
    }

    long getRefreshCount() {
        return 0; // Migrated to MetricsService
    }
}
