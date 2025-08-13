package org.openhab.core.ai.tool.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Monitoring statistics for system monitoring operations.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class MonitoringStatistics {
    private final long totalMetricsCollected;
    private final long totalAlertsGenerated;
    private final Map<String, Long> metricsByType;
    private final Map<String, Long> alertsBySeverity;
    private final Instant lastCollectionTime;

    public MonitoringStatistics(long totalMetricsCollected, long totalAlertsGenerated, Map<String, Long> metricsByType,
            Map<String, Long> alertsBySeverity, Instant lastCollectionTime) {
        this.totalMetricsCollected = totalMetricsCollected;
        this.totalAlertsGenerated = totalAlertsGenerated;
        this.metricsByType = metricsByType;
        this.alertsBySeverity = alertsBySeverity;
        this.lastCollectionTime = lastCollectionTime;
    }

    public long getTotalMetricsCollected() {
        return totalMetricsCollected;
    }

    public long getTotalAlertsGenerated() {
        return totalAlertsGenerated;
    }

    public Map<String, Long> getMetricsByType() {
        return metricsByType;
    }

    public Map<String, Long> getAlertsBySeverity() {
        return alertsBySeverity;
    }

    public Instant getLastCollectionTime() {
        return lastCollectionTime;
    }
}
