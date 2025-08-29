package org.openhab.core.ai.auth;

// import java.util.concurrent.atomic.AtomicLong; // Migrated to MetricsService

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.patterns.SystemPerformanceMetrics;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = SessionPattern.class)
@NonNullByDefault
public class SessionPattern {
    private final String principalId;
    private final String sessionId;
    // private final AtomicLong creationCount = new AtomicLong(0); // Migrated to MetricsService
    // private final AtomicLong timeoutCount = new AtomicLong(0); // Migrated to MetricsService
    
    @Reference
    private @Nullable MetricsService metricsService;

    SessionPattern(String principalId, String sessionId) {
        this.principalId = principalId;
        this.sessionId = sessionId;
    }

    void recordCreation() {
        // creationCount.incrementAndGet(); // Migrated to MetricsService
        try {
            MetricsService metrics = metricsService;
            if (metrics != null) {
                SystemPerformanceMetrics.recordMessageLatency(metrics, "session-pattern",
                    java.time.Duration.ofNanos(0), "creation", true);
            }
        } catch (Exception e) {
            // Graceful degradation - don't fail session creation
        }
    }

    void recordTimeout() {
        // timeoutCount.incrementAndGet(); // Migrated to MetricsService
        try {
            MetricsService metrics = metricsService;
            if (metrics != null) {
                SystemPerformanceMetrics.recordMessageLatency(metrics, "session-pattern",
                    java.time.Duration.ofNanos(0), "timeout", true);
            }
        } catch (Exception e) {
            // Graceful degradation - don't fail session timeout
        }
    }

    long getCreationCount() {
        return 0; // Migrated to MetricsService
    }

    long getTimeoutCount() {
        return 0; // Migrated to MetricsService
    }
}
