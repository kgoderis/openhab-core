package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.CardBuildingSnapshot;

/**
 * Statistics class for agent card building metrics.
 * 
 * <p>
 * This class provides comprehensive card building statistics including
 * generation performance, template processing, skill discovery,
 * and card validation. It aggregates multiple CardBuildingSnapshot
 * instances to provide historical and statistical analysis of card
 * building behavior.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record CardBuildingStatistics(List<CardBuildingSnapshot> snapshots, Duration timeRange, long timestampMs)
        implements
            StatisticsSnapshot,
            CountsMetrics,
            LatencyMetrics,
            TrendMetrics,
            PercentileMetrics {

    // CountsMetrics implementation
    @Override
    public long total() {
        return snapshots.stream().mapToLong(CardBuildingSnapshot::total).sum();
    }

    @Override
    public long success() {
        return snapshots.stream().mapToLong(CardBuildingSnapshot::success).sum();
    }

    @Override
    public long failure() {
        return snapshots.stream().mapToLong(CardBuildingSnapshot::failure).sum();
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return snapshots.stream().mapToLong(CardBuildingSnapshot::totalDurationNanos).sum();
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        double firstReliability = snapshots.get(0).reliabilityScore();
        double lastReliability = snapshots.get(snapshots.size() - 1).reliabilityScore();
        return firstReliability > 0 ? ((lastReliability - firstReliability) / firstReliability) * 100.0 : 0.0;
    }

    @Override
    public String trendDirection() {
        double percentage = trendPercentage();
        if (percentage > 5.0) {
            return "improving";
        } else if (percentage < -5.0) {
            return "declining";
        } else {
            return "stable";
        }
    }

    @Override
    public double changeRate() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        long totalBuilds = total();
        long timeSpanMs = snapshots.get(snapshots.size() - 1).timestampMs() - snapshots.get(0).timestampMs();
        return timeSpanMs > 0 ? (double) totalBuilds / (timeSpanMs / 1000.0) : 0.0;
    }

    // PercentileMetrics implementation
    @Override
    public double percentile50() {
        return calculatePercentile(0.5);
    }

    @Override
    public double percentile90() {
        return calculatePercentile(0.9);
    }

    @Override
    public double percentile95() {
        return calculatePercentile(0.95);
    }

    @Override
    public double percentile99() {
        return calculatePercentile(0.99);
    }

    private double calculatePercentile(double percentile) {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        List<Double> buildTimes = snapshots.stream().mapToDouble(s -> s.averageBuildTimeMs()).sorted().boxed().toList();

        if (buildTimes.isEmpty()) {
            return 0.0;
        }

        int index = (int) Math.ceil(percentile * buildTimes.size()) - 1;
        index = Math.max(0, Math.min(index, buildTimes.size() - 1));
        return buildTimes.get(index);
    }

    // Additional methods for card building analysis
    public double averageBuildSuccessRate() {
        return snapshots.stream().mapToDouble(CardBuildingSnapshot::buildSuccessRate).average().orElse(0.0);
    }

    public double averageSkillDiscoveryRate() {
        return snapshots.stream().mapToDouble(CardBuildingSnapshot::skillDiscoveryRate).average().orElse(0.0);
    }

    public double averageBuildTime() {
        return snapshots.stream().mapToDouble(CardBuildingSnapshot::averageBuildTimeMs).average().orElse(0.0);
    }

    public Map<String, Long> cardTypeDistribution() {
        return snapshots.stream().collect(Collectors.groupingBy(CardBuildingSnapshot::cardType,
                Collectors.summingLong(CardBuildingSnapshot::total)));
    }

    public long healthyCardBuilderCount() {
        return snapshots.stream().mapToLong(s -> s.isHealthy() ? 1 : 0).sum();
    }

    public boolean isOverallHealthy() {
        double healthyRatio = snapshots.size() > 0 ? (double) healthyCardBuilderCount() / snapshots.size() : 0.0;
        return averageBuildSuccessRate() > 0.95 && healthyRatio > 0.8;
    }
}
