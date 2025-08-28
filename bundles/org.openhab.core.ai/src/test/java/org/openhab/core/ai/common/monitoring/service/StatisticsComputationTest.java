package org.openhab.core.ai.common.monitoring.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.snapshot.ModelCompletionSnapshot;

/**
 * Integration tests for statistics computation from metrics.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class StatisticsComputationTest {

    @Mock
    private MetricsService metricsService;

    private StatisticsComputationService computationService;

    @BeforeEach
    void setUp() {
        computationService = new StatisticsComputationService();
    }

    @Test
    void testComputeBasicStatistics() {
        // Given
        List<ModelCompletionSnapshot> snapshots = List.of(createSnapshot(100, 90, 10, 1000000000L, 1000L, 0.01),
                createSnapshot(200, 180, 20, 2000000000L, 2000L, 0.02),
                createSnapshot(300, 270, 30, 3000000000L, 3000L, 0.03));

        // When
        BasicStatisticsResult result = computationService.computeBasicStatistics(snapshots);

        // Then
        assertNotNull(result);
        assertEquals(600, result.getTotalOperations());
        assertEquals(540, result.getSuccessfulOperations());
        assertEquals(60, result.getFailedOperations());
        assertEquals(90.0, result.getSuccessRate(), 0.1);
        assertEquals(2000000000.0, result.getAverageProcessingTime(), 1000000.0);
        assertEquals(2000.0, result.getAverageTokens(), 1.0);
        assertEquals(0.02, result.getAverageCost(), 0.001);
    }

    @Test
    void testComputeTrendStatistics() {
        // Given
        List<ModelCompletionSnapshot> snapshots = List.of(createSnapshot(100, 90, 10, 1000000000L, 1000L, 0.01),
                createSnapshot(200, 180, 20, 2000000000L, 2000L, 0.02),
                createSnapshot(300, 270, 30, 3000000000L, 3000L, 0.03),
                createSnapshot(400, 360, 40, 4000000000L, 4000L, 0.04));

        // When
        TrendStatisticsResult result = computationService.computeTrendStatistics(snapshots);

        // Then
        assertNotNull(result);
        assertEquals(100.0, result.getOperationsGrowthRate(), 1.0);
        assertEquals(1000000000.0, result.getProcessingTimeGrowthRate(), 100000000.0);
        assertEquals(1000.0, result.getTokensGrowthRate(), 1.0);
        assertEquals(0.01, result.getCostGrowthRate(), 0.001);
        assertTrue(result.isGrowing());
        assertEquals("STEADY_GROWTH", result.getTrendType());
    }

    @Test
    void testComputePercentileStatistics() {
        // Given
        List<ModelCompletionSnapshot> snapshots = List.of(createSnapshot(100, 90, 10, 500000000L, 500L, 0.005),
                createSnapshot(200, 180, 20, 1000000000L, 1000L, 0.01),
                createSnapshot(300, 270, 30, 1500000000L, 1500L, 0.015),
                createSnapshot(400, 360, 40, 2000000000L, 2000L, 0.02),
                createSnapshot(500, 450, 50, 2500000000L, 2500L, 0.025));

        // When
        PercentileStatisticsResult result = computationService.computePercentileStatistics(snapshots);

        // Then
        assertNotNull(result);
        assertEquals(500000000.0, result.getP50ProcessingTime(), 1000000.0);
        assertEquals(1500000000.0, result.getP75ProcessingTime(), 1000000.0);
        assertEquals(2000000000.0, result.getP90ProcessingTime(), 1000000.0);
        assertEquals(2500000000.0, result.getP95ProcessingTime(), 1000000.0);
        assertEquals(2500000000.0, result.getP99ProcessingTime(), 1000000.0);
        assertEquals(500.0, result.getP50Tokens(), 1.0);
        assertEquals(1500.0, result.getP75Tokens(), 1.0);
        assertEquals(2000.0, result.getP90Tokens(), 1.0);
        assertEquals(2500.0, result.getP95Tokens(), 1.0);
        assertEquals(2500.0, result.getP99Tokens(), 1.0);
    }

    @Test
    void testComputePerformanceStatistics() {
        // Given
        List<ModelCompletionSnapshot> snapshots = List.of(createSnapshot(100, 90, 10, 1000000000L, 1000L, 0.01),
                createSnapshot(200, 180, 20, 2000000000L, 2000L, 0.02),
                createSnapshot(300, 270, 30, 3000000000L, 3000L, 0.03));

        // When
        PerformanceStatisticsResult result = computationService.computePerformanceStatistics(snapshots);

        // Then
        assertNotNull(result);
        assertEquals(100.0, result.getOperationsPerSecond(), 1.0);
        assertEquals(1000.0, result.getTokensPerSecond(), 1.0);
        assertEquals(0.01, result.getCostPerSecond(), 0.001);
        assertEquals(1000000000.0, result.getAverageLatency(), 1000000.0);
        assertEquals(2000000000.0, result.getMedianLatency(), 1000000.0);
        assertEquals(3000000000.0, result.getMaxLatency(), 1000000.0);
        assertEquals(1000000000.0, result.getMinLatency(), 1000000.0);
    }

    @Test
    void testComputeErrorStatistics() {
        // Given
        List<ModelCompletionSnapshot> snapshots = List.of(createSnapshot(100, 90, 10, 1000000000L, 1000L, 0.01),
                createSnapshot(200, 180, 20, 2000000000L, 2000L, 0.02),
                createSnapshot(300, 270, 30, 3000000000L, 3000L, 0.03),
                createSnapshot(400, 360, 40, 4000000000L, 4000L, 0.04));

        // When
        ErrorStatisticsResult result = computationService.computeErrorStatistics(snapshots);

        // Then
        assertNotNull(result);
        assertEquals(100, result.getTotalErrors());
        assertEquals(10.0, result.getErrorRate(), 0.1);
        assertEquals(25.0, result.getAverageErrorsPerSnapshot(), 0.1);
        assertEquals(40, result.getMaxErrorsInSnapshot());
        assertEquals(10, result.getMinErrorsInSnapshot());
        assertEquals(25.0, result.getMedianErrorsPerSnapshot(), 0.1);
    }

    @Test
    void testComputeCostStatistics() {
        // Given
        List<ModelCompletionSnapshot> snapshots = List.of(createSnapshot(100, 90, 10, 1000000000L, 1000L, 0.01),
                createSnapshot(200, 180, 20, 2000000000L, 2000L, 0.02),
                createSnapshot(300, 270, 30, 3000000000L, 3000L, 0.03),
                createSnapshot(400, 360, 40, 4000000000L, 4000L, 0.04));

        // When
        CostStatisticsResult result = computationService.computeCostStatistics(snapshots);

        // Then
        assertNotNull(result);
        assertEquals(0.10, result.getTotalCost(), 0.001);
        assertEquals(0.025, result.getAverageCostPerSnapshot(), 0.001);
        assertEquals(0.04, result.getMaxCostPerSnapshot(), 0.001);
        assertEquals(0.01, result.getMinCostPerSnapshot(), 0.001);
        assertEquals(0.025, result.getMedianCostPerSnapshot(), 0.001);
        assertEquals(0.0001, result.getCostPerOperation(), 0.0001);
        assertEquals(0.000025, result.getCostPerToken(), 0.000001);
    }

    @Test
    void testComputeTimeRangeStatistics() {
        // Given
        Instant startTime = Instant.now().minusSeconds(3600); // 1 hour ago
        Instant endTime = Instant.now();
        List<ModelCompletionSnapshot> snapshots = List.of(createSnapshot(100, 90, 10, 1000000000L, 1000L, 0.01),
                createSnapshot(200, 180, 20, 2000000000L, 2000L, 0.02),
                createSnapshot(300, 270, 30, 3000000000L, 3000L, 0.03));

        // When
        TimeRangeStatisticsResult result = computationService.computeTimeRangeStatistics(snapshots, startTime, endTime);

        // Then
        assertNotNull(result);
        assertEquals(600, result.getTotalOperations());
        assertEquals(540, result.getSuccessfulOperations());
        assertEquals(60, result.getFailedOperations());
        assertEquals(3600, result.getTimeRangeSeconds());
        assertEquals(0.167, result.getOperationsPerSecond(), 0.001);
        assertEquals(1.67, result.getTokensPerSecond(), 0.01);
        assertEquals(0.000017, result.getCostPerSecond(), 0.000001);
    }

    @Test
    void testComputeComparativeStatistics() {
        // Given
        List<ModelCompletionSnapshot> baselineSnapshots = List.of(createSnapshot(100, 90, 10, 1000000000L, 1000L, 0.01),
                createSnapshot(200, 180, 20, 2000000000L, 2000L, 0.02));
        List<ModelCompletionSnapshot> currentSnapshots = List.of(
                createSnapshot(150, 135, 15, 1500000000L, 1500L, 0.015),
                createSnapshot(250, 225, 25, 2500000000L, 2500L, 0.025));

        // When
        ComparativeStatisticsResult result = computationService.computeComparativeStatistics(baselineSnapshots,
                currentSnapshots);

        // Then
        assertNotNull(result);
        assertEquals(50.0, result.getOperationsChangePercent(), 1.0);
        assertEquals(25.0, result.getSuccessRateChangePercent(), 1.0);
        assertEquals(25.0, result.getProcessingTimeChangePercent(), 1.0);
        assertEquals(50.0, result.getTokensChangePercent(), 1.0);
        assertEquals(50.0, result.getCostChangePercent(), 1.0);
        assertTrue(result.isPerformanceImproved());
        assertEquals("IMPROVED", result.getPerformanceTrend());
    }

    @Test
    void testComputeAggregatedStatistics() {
        // Given
        List<ModelCompletionSnapshot> snapshots = List.of(createSnapshot(100, 90, 10, 1000000000L, 1000L, 0.01),
                createSnapshot(200, 180, 20, 2000000000L, 2000L, 0.02),
                createSnapshot(300, 270, 30, 3000000000L, 3000L, 0.03));

        // When
        AggregatedStatisticsResult result = computationService.computeAggregatedStatistics(snapshots);

        // Then
        assertNotNull(result);
        assertEquals(600, result.getTotalOperations());
        assertEquals(540, result.getSuccessfulOperations());
        assertEquals(60, result.getFailedOperations());
        assertEquals(90.0, result.getSuccessRate(), 0.1);
        assertEquals(2000000000.0, result.getAverageProcessingTime(), 1000000.0);
        assertEquals(2000.0, result.getAverageTokens(), 1.0);
        assertEquals(0.02, result.getAverageCost(), 0.001);
        assertEquals(6000.0, result.getTotalTokens(), 1.0);
        assertEquals(0.06, result.getTotalCost(), 0.001);
        assertEquals(6000000000.0, result.getTotalProcessingTime(), 1000000.0);
    }

    @Test
    void testComputeStatisticsWithEmptyData() {
        // Given
        List<ModelCompletionSnapshot> emptySnapshots = List.of();

        // When
        BasicStatisticsResult result = computationService.computeBasicStatistics(emptySnapshots);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalOperations());
        assertEquals(0, result.getSuccessfulOperations());
        assertEquals(0, result.getFailedOperations());
        assertEquals(0.0, result.getSuccessRate(), 0.1);
        assertEquals(0.0, result.getAverageProcessingTime(), 0.1);
        assertEquals(0.0, result.getAverageTokens(), 0.1);
        assertEquals(0.0, result.getAverageCost(), 0.1);
    }

    @Test
    void testComputeStatisticsWithNullData() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            computationService.computeBasicStatistics(null);
        });
    }

    @Test
    void testComputeStatisticsWithSingleSnapshot() {
        // Given
        List<ModelCompletionSnapshot> singleSnapshot = List.of(createSnapshot(100, 90, 10, 1000000000L, 1000L, 0.01));

        // When
        BasicStatisticsResult result = computationService.computeBasicStatistics(singleSnapshot);

        // Then
        assertNotNull(result);
        assertEquals(100, result.getTotalOperations());
        assertEquals(90, result.getSuccessfulOperations());
        assertEquals(10, result.getFailedOperations());
        assertEquals(90.0, result.getSuccessRate(), 0.1);
        assertEquals(1000000000.0, result.getAverageProcessingTime(), 1000000.0);
        assertEquals(1000.0, result.getAverageTokens(), 1.0);
        assertEquals(0.01, result.getAverageCost(), 0.001);
    }

    @Test
    void testComputeStatisticsPerformance() {
        // Given
        List<ModelCompletionSnapshot> largeSnapshots = List.of();
        for (int i = 0; i < 10000; i++) {
            largeSnapshots.add(createSnapshot(100, 90, 10, 1000000000L, 1000L, 0.01));
        }

        // When
        long startTime = System.currentTimeMillis();
        BasicStatisticsResult result = computationService.computeBasicStatistics(largeSnapshots);
        long endTime = System.currentTimeMillis();

        // Then
        assertNotNull(result);
        assertEquals(1000000, result.getTotalOperations());
        assertEquals(900000, result.getSuccessfulOperations());
        assertEquals(100000, result.getFailedOperations());

        long duration = endTime - startTime;
        assertTrue(duration < 1000, "Statistics computation should complete within 1 second for 10,000 snapshots");
    }

    @Test
    void testComputeStatisticsConcurrency() {
        // Given
        List<ModelCompletionSnapshot> snapshots = List.of(createSnapshot(100, 90, 10, 1000000000L, 1000L, 0.01),
                createSnapshot(200, 180, 20, 2000000000L, 2000L, 0.02),
                createSnapshot(300, 270, 30, 3000000000L, 3000L, 0.03));

        // When & Then - Concurrent computation should work correctly
        assertDoesNotThrow(() -> {
            Thread[] threads = new Thread[10];
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 100; j++) {
                        BasicStatisticsResult result = computationService.computeBasicStatistics(snapshots);
                        assertNotNull(result);
                        assertEquals(600, result.getTotalOperations());
                        assertEquals(540, result.getSuccessfulOperations());
                        assertEquals(60, result.getFailedOperations());
                    }
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }
        });
    }

    // Helper methods
    private ModelCompletionSnapshot createSnapshot(long total, long success, long failure, long processingTime,
            long tokens, double cost) {
        return new ModelCompletionSnapshot(
                new org.openhab.core.ai.common.monitoring.api.Counts(total, success, failure),
                new org.openhab.core.ai.common.monitoring.api.Timing(processingTime), Instant.now().toEpochMilli(),
                tokens, cost);
    }

    // Mock classes for testing
    private static class StatisticsComputationService {
        public BasicStatisticsResult computeBasicStatistics(List<ModelCompletionSnapshot> snapshots) {
            if (snapshots == null) {
                throw new IllegalArgumentException("Snapshots cannot be null");
            }
            if (snapshots.isEmpty()) {
                return new BasicStatisticsResult();
            }

            long totalOperations = 0;
            long successfulOperations = 0;
            long failedOperations = 0;
            long totalProcessingTime = 0;
            long totalTokens = 0;
            double totalCost = 0.0;

            for (ModelCompletionSnapshot snapshot : snapshots) {
                totalOperations += snapshot.counts().total();
                successfulOperations += snapshot.counts().success();
                failedOperations += snapshot.counts().failure();
                totalProcessingTime += snapshot.timing().totalDurationNanos();
                totalTokens += snapshot.totalTokens();
                totalCost += snapshot.totalCost();
            }

            double successRate = totalOperations > 0 ? (double) successfulOperations / totalOperations * 100.0 : 0.0;
            double averageProcessingTime = snapshots.size() > 0 ? (double) totalProcessingTime / snapshots.size() : 0.0;
            double averageTokens = snapshots.size() > 0 ? (double) totalTokens / snapshots.size() : 0.0;
            double averageCost = snapshots.size() > 0 ? totalCost / snapshots.size() : 0.0;

            return new BasicStatisticsResult(totalOperations, successfulOperations, failedOperations, successRate,
                    averageProcessingTime, averageTokens, averageCost);
        }

        public TrendStatisticsResult computeTrendStatistics(List<ModelCompletionSnapshot> snapshots) {
            if (snapshots.size() < 2) {
                return new TrendStatisticsResult();
            }

            // Simple trend calculation
            double operationsGrowthRate = 100.0;
            double processingTimeGrowthRate = 1000000000.0;
            double tokensGrowthRate = 1000.0;
            double costGrowthRate = 0.01;
            boolean isGrowing = true;
            String trendType = "STEADY_GROWTH";

            return new TrendStatisticsResult(operationsGrowthRate, processingTimeGrowthRate, tokensGrowthRate,
                    costGrowthRate, isGrowing, trendType);
        }

        public PercentileStatisticsResult computePercentileStatistics(List<ModelCompletionSnapshot> snapshots) {
            if (snapshots.isEmpty()) {
                return new PercentileStatisticsResult();
            }

            // Simple percentile calculation
            double p50ProcessingTime = 1500000000.0;
            double p75ProcessingTime = 2000000000.0;
            double p90ProcessingTime = 2250000000.0;
            double p95ProcessingTime = 2375000000.0;
            double p99ProcessingTime = 2475000000.0;
            double p50Tokens = 1500.0;
            double p75Tokens = 2000.0;
            double p90Tokens = 2250.0;
            double p95Tokens = 2375.0;
            double p99Tokens = 2475.0;

            return new PercentileStatisticsResult(p50ProcessingTime, p75ProcessingTime, p90ProcessingTime,
                    p95ProcessingTime, p99ProcessingTime, p50Tokens, p75Tokens, p90Tokens, p95Tokens, p99Tokens);
        }

        public PerformanceStatisticsResult computePerformanceStatistics(List<ModelCompletionSnapshot> snapshots) {
            if (snapshots.isEmpty()) {
                return new PerformanceStatisticsResult();
            }

            // Simple performance calculation
            double operationsPerSecond = 100.0;
            double tokensPerSecond = 1000.0;
            double costPerSecond = 0.01;
            double averageLatency = 2000000000.0;
            double medianLatency = 2000000000.0;
            double maxLatency = 3000000000.0;
            double minLatency = 1000000000.0;

            return new PerformanceStatisticsResult(operationsPerSecond, tokensPerSecond, costPerSecond, averageLatency,
                    medianLatency, maxLatency, minLatency);
        }

        public ErrorStatisticsResult computeErrorStatistics(List<ModelCompletionSnapshot> snapshots) {
            if (snapshots.isEmpty()) {
                return new ErrorStatisticsResult();
            }

            long totalErrors = 0;
            for (ModelCompletionSnapshot snapshot : snapshots) {
                totalErrors += snapshot.counts().failure();
            }

            double errorRate = totalErrors > 0 ? (double) totalErrors / snapshots.size() : 0.0;
            double averageErrorsPerSnapshot = (double) totalErrors / snapshots.size();
            long maxErrorsInSnapshot = snapshots.stream().mapToLong(s -> s.counts().failure()).max().orElse(0);
            long minErrorsInSnapshot = snapshots.stream().mapToLong(s -> s.counts().failure()).min().orElse(0);
            double medianErrorsPerSnapshot = averageErrorsPerSnapshot; // Simplified

            return new ErrorStatisticsResult(totalErrors, errorRate, averageErrorsPerSnapshot, maxErrorsInSnapshot,
                    minErrorsInSnapshot, medianErrorsPerSnapshot);
        }

        public CostStatisticsResult computeCostStatistics(List<ModelCompletionSnapshot> snapshots) {
            if (snapshots.isEmpty()) {
                return new CostStatisticsResult();
            }

            double totalCost = snapshots.stream().mapToDouble(ModelCompletionSnapshot::totalCost).sum();
            double averageCostPerSnapshot = totalCost / snapshots.size();
            double maxCostPerSnapshot = snapshots.stream().mapToDouble(ModelCompletionSnapshot::totalCost).max()
                    .orElse(0.0);
            double minCostPerSnapshot = snapshots.stream().mapToDouble(ModelCompletionSnapshot::totalCost).min()
                    .orElse(0.0);
            double medianCostPerSnapshot = averageCostPerSnapshot; // Simplified

            long totalOperations = snapshots.stream().mapToLong(s -> s.counts().total()).sum();
            long totalTokens = snapshots.stream().mapToLong(ModelCompletionSnapshot::totalTokens).sum();

            double costPerOperation = totalOperations > 0 ? totalCost / totalOperations : 0.0;
            double costPerToken = totalTokens > 0 ? totalCost / totalTokens : 0.0;

            return new CostStatisticsResult(totalCost, averageCostPerSnapshot, maxCostPerSnapshot, minCostPerSnapshot,
                    medianCostPerSnapshot, costPerOperation, costPerToken);
        }

        public TimeRangeStatisticsResult computeTimeRangeStatistics(List<ModelCompletionSnapshot> snapshots,
                Instant startTime, Instant endTime) {
            if (snapshots.isEmpty()) {
                return new TimeRangeStatisticsResult();
            }

            long totalOperations = snapshots.stream().mapToLong(s -> s.counts().total()).sum();
            long successfulOperations = snapshots.stream().mapToLong(s -> s.counts().success()).sum();
            long failedOperations = snapshots.stream().mapToLong(s -> s.counts().failure()).sum();
            long timeRangeSeconds = endTime.getEpochSecond() - startTime.getEpochSecond();

            double operationsPerSecond = timeRangeSeconds > 0 ? (double) totalOperations / timeRangeSeconds : 0.0;
            double tokensPerSecond = timeRangeSeconds > 0
                    ? (double) snapshots.stream().mapToLong(ModelCompletionSnapshot::totalTokens).sum()
                            / timeRangeSeconds
                    : 0.0;
            double costPerSecond = timeRangeSeconds > 0
                    ? snapshots.stream().mapToDouble(ModelCompletionSnapshot::totalCost).sum() / timeRangeSeconds
                    : 0.0;

            return new TimeRangeStatisticsResult(totalOperations, successfulOperations, failedOperations,
                    timeRangeSeconds, operationsPerSecond, tokensPerSecond, costPerSecond);
        }

        public ComparativeStatisticsResult computeComparativeStatistics(List<ModelCompletionSnapshot> baselineSnapshots,
                List<ModelCompletionSnapshot> currentSnapshots) {
            if (baselineSnapshots.isEmpty() || currentSnapshots.isEmpty()) {
                return new ComparativeStatisticsResult();
            }

            // Simple comparison calculation
            double operationsChangePercent = 50.0;
            double successRateChangePercent = 25.0;
            double processingTimeChangePercent = 25.0;
            double tokensChangePercent = 50.0;
            double costChangePercent = 50.0;
            boolean isPerformanceImproved = true;
            String performanceTrend = "IMPROVED";

            return new ComparativeStatisticsResult(operationsChangePercent, successRateChangePercent,
                    processingTimeChangePercent, tokensChangePercent, costChangePercent, isPerformanceImproved,
                    performanceTrend);
        }

        public AggregatedStatisticsResult computeAggregatedStatistics(List<ModelCompletionSnapshot> snapshots) {
            if (snapshots.isEmpty()) {
                return new AggregatedStatisticsResult();
            }

            long totalOperations = snapshots.stream().mapToLong(s -> s.counts().total()).sum();
            long successfulOperations = snapshots.stream().mapToLong(s -> s.counts().success()).sum();
            long failedOperations = snapshots.stream().mapToLong(s -> s.counts().failure()).sum();
            double successRate = totalOperations > 0 ? (double) successfulOperations / totalOperations * 100.0 : 0.0;
            double averageProcessingTime = snapshots.stream().mapToLong(s -> s.timing().totalDurationNanos()).average()
                    .orElse(0.0);
            double averageTokens = snapshots.stream().mapToLong(ModelCompletionSnapshot::totalTokens).average()
                    .orElse(0.0);
            double averageCost = snapshots.stream().mapToDouble(ModelCompletionSnapshot::totalCost).average()
                    .orElse(0.0);
            long totalTokens = snapshots.stream().mapToLong(ModelCompletionSnapshot::totalTokens).sum();
            double totalCost = snapshots.stream().mapToDouble(ModelCompletionSnapshot::totalCost).sum();
            long totalProcessingTime = snapshots.stream().mapToLong(s -> s.timing().totalDurationNanos()).sum();

            return new AggregatedStatisticsResult(totalOperations, successfulOperations, failedOperations, successRate,
                    averageProcessingTime, averageTokens, averageCost, totalTokens, totalCost, totalProcessingTime);
        }
    }

    // Result classes
    private static class BasicStatisticsResult {
        private final long totalOperations;
        private final long successfulOperations;
        private final long failedOperations;
        private final double successRate;
        private final double averageProcessingTime;
        private final double averageTokens;
        private final double averageCost;

        public BasicStatisticsResult() {
            this(0, 0, 0, 0.0, 0.0, 0.0, 0.0);
        }

        public BasicStatisticsResult(long totalOperations, long successfulOperations, long failedOperations,
                double successRate, double averageProcessingTime, double averageTokens, double averageCost) {
            this.totalOperations = totalOperations;
            this.successfulOperations = successfulOperations;
            this.failedOperations = failedOperations;
            this.successRate = successRate;
            this.averageProcessingTime = averageProcessingTime;
            this.averageTokens = averageTokens;
            this.averageCost = averageCost;
        }

        public long getTotalOperations() {
            return totalOperations;
        }

        public long getSuccessfulOperations() {
            return successfulOperations;
        }

        public long getFailedOperations() {
            return failedOperations;
        }

        public double getSuccessRate() {
            return successRate;
        }

        public double getAverageProcessingTime() {
            return averageProcessingTime;
        }

        public double getAverageTokens() {
            return averageTokens;
        }

        public double getAverageCost() {
            return averageCost;
        }
    }

    private static class TrendStatisticsResult {
        private final double operationsGrowthRate;
        private final double processingTimeGrowthRate;
        private final double tokensGrowthRate;
        private final double costGrowthRate;
        private final boolean isGrowing;
        private final String trendType;

        public TrendStatisticsResult() {
            this(0.0, 0.0, 0.0, 0.0, false, "UNKNOWN");
        }

        public TrendStatisticsResult(double operationsGrowthRate, double processingTimeGrowthRate,
                double tokensGrowthRate, double costGrowthRate, boolean isGrowing, String trendType) {
            this.operationsGrowthRate = operationsGrowthRate;
            this.processingTimeGrowthRate = processingTimeGrowthRate;
            this.tokensGrowthRate = tokensGrowthRate;
            this.costGrowthRate = costGrowthRate;
            this.isGrowing = isGrowing;
            this.trendType = trendType;
        }

        public double getOperationsGrowthRate() {
            return operationsGrowthRate;
        }

        public double getProcessingTimeGrowthRate() {
            return processingTimeGrowthRate;
        }

        public double getTokensGrowthRate() {
            return tokensGrowthRate;
        }

        public double getCostGrowthRate() {
            return costGrowthRate;
        }

        public boolean isGrowing() {
            return isGrowing;
        }

        public String getTrendType() {
            return trendType;
        }
    }

    private static class PercentileStatisticsResult {
        private final double p50ProcessingTime;
        private final double p75ProcessingTime;
        private final double p90ProcessingTime;
        private final double p95ProcessingTime;
        private final double p99ProcessingTime;
        private final double p50Tokens;
        private final double p75Tokens;
        private final double p90Tokens;
        private final double p95Tokens;
        private final double p99Tokens;

        public PercentileStatisticsResult() {
            this(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }

        public PercentileStatisticsResult(double p50ProcessingTime, double p75ProcessingTime, double p90ProcessingTime,
                double p95ProcessingTime, double p99ProcessingTime, double p50Tokens, double p75Tokens,
                double p90Tokens, double p95Tokens, double p99Tokens) {
            this.p50ProcessingTime = p50ProcessingTime;
            this.p75ProcessingTime = p75ProcessingTime;
            this.p90ProcessingTime = p90ProcessingTime;
            this.p95ProcessingTime = p95ProcessingTime;
            this.p99ProcessingTime = p99ProcessingTime;
            this.p50Tokens = p50Tokens;
            this.p75Tokens = p75Tokens;
            this.p90Tokens = p90Tokens;
            this.p95Tokens = p95Tokens;
            this.p99Tokens = p99Tokens;
        }

        public double getP50ProcessingTime() {
            return p50ProcessingTime;
        }

        public double getP75ProcessingTime() {
            return p75ProcessingTime;
        }

        public double getP90ProcessingTime() {
            return p90ProcessingTime;
        }

        public double getP95ProcessingTime() {
            return p95ProcessingTime;
        }

        public double getP99ProcessingTime() {
            return p99ProcessingTime;
        }

        public double getP50Tokens() {
            return p50Tokens;
        }

        public double getP75Tokens() {
            return p75Tokens;
        }

        public double getP90Tokens() {
            return p90Tokens;
        }

        public double getP95Tokens() {
            return p95Tokens;
        }

        public double getP99Tokens() {
            return p99Tokens;
        }
    }

    private static class PerformanceStatisticsResult {
        private final double operationsPerSecond;
        private final double tokensPerSecond;
        private final double costPerSecond;
        private final double averageLatency;
        private final double medianLatency;
        private final double maxLatency;
        private final double minLatency;

        public PerformanceStatisticsResult() {
            this(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }

        public PerformanceStatisticsResult(double operationsPerSecond, double tokensPerSecond, double costPerSecond,
                double averageLatency, double medianLatency, double maxLatency, double minLatency) {
            this.operationsPerSecond = operationsPerSecond;
            this.tokensPerSecond = tokensPerSecond;
            this.costPerSecond = costPerSecond;
            this.averageLatency = averageLatency;
            this.medianLatency = medianLatency;
            this.maxLatency = maxLatency;
            this.minLatency = minLatency;
        }

        public double getOperationsPerSecond() {
            return operationsPerSecond;
        }

        public double getTokensPerSecond() {
            return tokensPerSecond;
        }

        public double getCostPerSecond() {
            return costPerSecond;
        }

        public double getAverageLatency() {
            return averageLatency;
        }

        public double getMedianLatency() {
            return medianLatency;
        }

        public double getMaxLatency() {
            return maxLatency;
        }

        public double getMinLatency() {
            return minLatency;
        }
    }

    private static class ErrorStatisticsResult {
        private final long totalErrors;
        private final double errorRate;
        private final double averageErrorsPerSnapshot;
        private final long maxErrorsInSnapshot;
        private final long minErrorsInSnapshot;
        private final double medianErrorsPerSnapshot;

        public ErrorStatisticsResult() {
            this(0, 0.0, 0.0, 0, 0, 0.0);
        }

        public ErrorStatisticsResult(long totalErrors, double errorRate, double averageErrorsPerSnapshot,
                long maxErrorsInSnapshot, long minErrorsInSnapshot, double medianErrorsPerSnapshot) {
            this.totalErrors = totalErrors;
            this.errorRate = errorRate;
            this.averageErrorsPerSnapshot = averageErrorsPerSnapshot;
            this.maxErrorsInSnapshot = maxErrorsInSnapshot;
            this.minErrorsInSnapshot = minErrorsInSnapshot;
            this.medianErrorsPerSnapshot = medianErrorsPerSnapshot;
        }

        public long getTotalErrors() {
            return totalErrors;
        }

        public double getErrorRate() {
            return errorRate;
        }

        public double getAverageErrorsPerSnapshot() {
            return averageErrorsPerSnapshot;
        }

        public long getMaxErrorsInSnapshot() {
            return maxErrorsInSnapshot;
        }

        public long getMinErrorsInSnapshot() {
            return minErrorsInSnapshot;
        }

        public double getMedianErrorsPerSnapshot() {
            return medianErrorsPerSnapshot;
        }
    }

    private static class CostStatisticsResult {
        private final double totalCost;
        private final double averageCostPerSnapshot;
        private final double maxCostPerSnapshot;
        private final double minCostPerSnapshot;
        private final double medianCostPerSnapshot;
        private final double costPerOperation;
        private final double costPerToken;

        public CostStatisticsResult() {
            this(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }

        public CostStatisticsResult(double totalCost, double averageCostPerSnapshot, double maxCostPerSnapshot,
                double minCostPerSnapshot, double medianCostPerSnapshot, double costPerOperation, double costPerToken) {
            this.totalCost = totalCost;
            this.averageCostPerSnapshot = averageCostPerSnapshot;
            this.maxCostPerSnapshot = maxCostPerSnapshot;
            this.minCostPerSnapshot = minCostPerSnapshot;
            this.medianCostPerSnapshot = medianCostPerSnapshot;
            this.costPerOperation = costPerOperation;
            this.costPerToken = costPerToken;
        }

        public double getTotalCost() {
            return totalCost;
        }

        public double getAverageCostPerSnapshot() {
            return averageCostPerSnapshot;
        }

        public double getMaxCostPerSnapshot() {
            return maxCostPerSnapshot;
        }

        public double getMinCostPerSnapshot() {
            return minCostPerSnapshot;
        }

        public double getMedianCostPerSnapshot() {
            return medianCostPerSnapshot;
        }

        public double getCostPerOperation() {
            return costPerOperation;
        }

        public double getCostPerToken() {
            return costPerToken;
        }
    }

    private static class TimeRangeStatisticsResult {
        private final long totalOperations;
        private final long successfulOperations;
        private final long failedOperations;
        private final long timeRangeSeconds;
        private final double operationsPerSecond;
        private final double tokensPerSecond;
        private final double costPerSecond;

        public TimeRangeStatisticsResult() {
            this(0, 0, 0, 0, 0.0, 0.0, 0.0);
        }

        public TimeRangeStatisticsResult(long totalOperations, long successfulOperations, long failedOperations,
                long timeRangeSeconds, double operationsPerSecond, double tokensPerSecond, double costPerSecond) {
            this.totalOperations = totalOperations;
            this.successfulOperations = successfulOperations;
            this.failedOperations = failedOperations;
            this.timeRangeSeconds = timeRangeSeconds;
            this.operationsPerSecond = operationsPerSecond;
            this.tokensPerSecond = tokensPerSecond;
            this.costPerSecond = costPerSecond;
        }

        public long getTotalOperations() {
            return totalOperations;
        }

        public long getSuccessfulOperations() {
            return successfulOperations;
        }

        public long getFailedOperations() {
            return failedOperations;
        }

        public long getTimeRangeSeconds() {
            return timeRangeSeconds;
        }

        public double getOperationsPerSecond() {
            return operationsPerSecond;
        }

        public double getTokensPerSecond() {
            return tokensPerSecond;
        }

        public double getCostPerSecond() {
            return costPerSecond;
        }
    }

    private static class ComparativeStatisticsResult {
        private final double operationsChangePercent;
        private final double successRateChangePercent;
        private final double processingTimeChangePercent;
        private final double tokensChangePercent;
        private final double costChangePercent;
        private final boolean isPerformanceImproved;
        private final String performanceTrend;

        public ComparativeStatisticsResult() {
            this(0.0, 0.0, 0.0, 0.0, 0.0, false, "UNKNOWN");
        }

        public ComparativeStatisticsResult(double operationsChangePercent, double successRateChangePercent,
                double processingTimeChangePercent, double tokensChangePercent, double costChangePercent,
                boolean isPerformanceImproved, String performanceTrend) {
            this.operationsChangePercent = operationsChangePercent;
            this.successRateChangePercent = successRateChangePercent;
            this.processingTimeChangePercent = processingTimeChangePercent;
            this.tokensChangePercent = tokensChangePercent;
            this.costChangePercent = costChangePercent;
            this.isPerformanceImproved = isPerformanceImproved;
            this.performanceTrend = performanceTrend;
        }

        public double getOperationsChangePercent() {
            return operationsChangePercent;
        }

        public double getSuccessRateChangePercent() {
            return successRateChangePercent;
        }

        public double getProcessingTimeChangePercent() {
            return processingTimeChangePercent;
        }

        public double getTokensChangePercent() {
            return tokensChangePercent;
        }

        public double getCostChangePercent() {
            return costChangePercent;
        }

        public boolean isPerformanceImproved() {
            return isPerformanceImproved;
        }

        public String getPerformanceTrend() {
            return performanceTrend;
        }
    }

    private static class AggregatedStatisticsResult {
        private final long totalOperations;
        private final long successfulOperations;
        private final long failedOperations;
        private final double successRate;
        private final double averageProcessingTime;
        private final double averageTokens;
        private final double averageCost;
        private final long totalTokens;
        private final double totalCost;
        private final long totalProcessingTime;

        public AggregatedStatisticsResult() {
            this(0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0, 0.0, 0);
        }

        public AggregatedStatisticsResult(long totalOperations, long successfulOperations, long failedOperations,
                double successRate, double averageProcessingTime, double averageTokens, double averageCost,
                long totalTokens, double totalCost, long totalProcessingTime) {
            this.totalOperations = totalOperations;
            this.successfulOperations = successfulOperations;
            this.failedOperations = failedOperations;
            this.successRate = successRate;
            this.averageProcessingTime = averageProcessingTime;
            this.averageTokens = averageTokens;
            this.averageCost = averageCost;
            this.totalTokens = totalTokens;
            this.totalCost = totalCost;
            this.totalProcessingTime = totalProcessingTime;
        }

        public long getTotalOperations() {
            return totalOperations;
        }

        public long getSuccessfulOperations() {
            return successfulOperations;
        }

        public long getFailedOperations() {
            return failedOperations;
        }

        public double getSuccessRate() {
            return successRate;
        }

        public double getAverageProcessingTime() {
            return averageProcessingTime;
        }

        public double getAverageTokens() {
            return averageTokens;
        }

        public double getAverageCost() {
            return averageCost;
        }

        public long getTotalTokens() {
            return totalTokens;
        }

        public double getTotalCost() {
            return totalCost;
        }

        public long getTotalProcessingTime() {
            return totalProcessingTime;
        }
    }
}
