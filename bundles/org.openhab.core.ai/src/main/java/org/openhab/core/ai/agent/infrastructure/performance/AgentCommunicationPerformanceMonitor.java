package org.openhab.core.ai.agent.infrastructure.performance;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.collector.ExecutionMetricsCollector;
import org.openhab.core.ai.common.monitoring.registry.MonitoringRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitors and analyzes agent communication performance metrics
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = AgentCommunicationPerformanceMonitor.class)
@NonNullByDefault
public class AgentCommunicationPerformanceMonitor {

    private final Logger logger = LoggerFactory.getLogger(AgentCommunicationPerformanceMonitor.class);

    // Performance metrics storage
    private final Map<String, MessageLatencyMetrics> latencyMetrics = new ConcurrentHashMap<>();
    private final Map<String, ThroughputMetrics> throughputMetrics = new ConcurrentHashMap<>();
    private final Map<String, BandwidthMetrics> bandwidthMetrics = new ConcurrentHashMap<>();
    private final Map<String, PerformanceHistory> performanceHistory = new ConcurrentHashMap<>();

    // SLA thresholds
    private final AtomicReference<Duration> maxLatencyThreshold = new AtomicReference<>(Duration.ofMillis(100));
    private final AtomicReference<Long> minThroughputThreshold = new AtomicReference<>(1000L); // messages/sec
    private final AtomicReference<Long> maxBandwidthThreshold = new AtomicReference<>(1024L * 1024L); // 1MB/sec

    // Background processors
    private final ScheduledExecutorService metricsProcessor = Executors.newScheduledThreadPool(2);
    private final ScheduledExecutorService alertingProcessor = Executors.newScheduledThreadPool(1);
    private final ScheduledExecutorService optimizationProcessor = Executors.newScheduledThreadPool(1);

    // NEW: Monitoring registry for centralized metrics collection
    @Reference
    private @Nullable MonitoringRegistry monitoringRegistry;

    @Activate
    public AgentCommunicationPerformanceMonitor() {
        startBackgroundProcessors();
    }

    @Deactivate
    public void deactivate() {
        metricsProcessor.shutdown();
        alertingProcessor.shutdown();
        optimizationProcessor.shutdown();
    }

    /**
     * Record message latency using the new monitoring framework
     */
    public void recordMessageLatency(String agentId, long latencyMs) {
        try {
            // Use centralized monitoring registry
            if (monitoringRegistry != null) {
                ExecutionMetricsCollector collector = monitoringRegistry.executionCollector(MetricKeys.agent(agentId));
                // Record as successful execution with latency
                collector.recordExecution(true, latencyMs * 1_000_000L); // Convert to nanoseconds
            }

            // Legacy local metrics for backward compatibility
            MessageLatencyMetrics metrics = latencyMetrics.computeIfAbsent(agentId,
                    id -> new MessageLatencyMetrics(id));
            metrics.recordLatency("message", Duration.ofMillis(latencyMs));

            // Check SLA violation
            if (latencyMs > maxLatencyThreshold.get().toMillis()) {
                logger.warn("Latency SLA violation for agent {}: {}ms (threshold: {}ms)", agentId, latencyMs,
                        maxLatencyThreshold.get().toMillis());
            }

        } catch (Exception e) {
            logger.error("Error recording latency for agent: {}", agentId, e);
        }
    }

    /**
     * Record throughput using the new monitoring framework
     */
    public void recordThroughput(String agentId, long messagesPerSecond) {
        try {
            // Use centralized monitoring registry
            if (monitoringRegistry != null) {
                ExecutionMetricsCollector collector = monitoringRegistry.executionCollector(MetricKeys.agent(agentId));
                // Record throughput as successful execution
                collector.recordExecution(true, 0L);
            }

            // Legacy local metrics for backward compatibility
            ThroughputMetrics metrics = throughputMetrics.computeIfAbsent(agentId, id -> new ThroughputMetrics(id));
            metrics.recordThroughput(messagesPerSecond);

            // Check SLA violation
            if (messagesPerSecond < minThroughputThreshold.get()) {
                logger.warn("Throughput SLA violation for agent {}: {} msg/sec (threshold: {} msg/sec)", agentId,
                        messagesPerSecond, minThroughputThreshold.get());
            }

        } catch (Exception e) {
            logger.error("Error recording throughput for agent: {}", agentId, e);
        }
    }

    /**
     * Record bandwidth usage using the new monitoring framework
     */
    public void recordBandwidthUsage(String agentId, long bytesPerSecond) {
        try {
            // Use centralized monitoring registry
            if (monitoringRegistry != null) {
                ExecutionMetricsCollector collector = monitoringRegistry.executionCollector(MetricKeys.agent(agentId));
                // Record bandwidth as successful execution
                collector.recordExecution(true, 0L);
            }

            // Legacy local metrics for backward compatibility
            BandwidthMetrics metrics = bandwidthMetrics.computeIfAbsent(agentId, id -> new BandwidthMetrics(id));
            metrics.recordBandwidth(bytesPerSecond);

            // Check SLA violation
            if (bytesPerSecond > maxBandwidthThreshold.get()) {
                logger.warn("Bandwidth SLA violation for agent {}: {} bytes/sec (threshold: {} bytes/sec)", agentId,
                        bytesPerSecond, maxBandwidthThreshold.get());
            }

        } catch (Exception e) {
            logger.error("Error recording bandwidth for agent: {}", agentId, e);
        }
    }

    /**
     * Get performance statistics using the new monitoring framework
     */
    public PerformanceStatistics getStatistics() {
        // Get statistics from monitoring registry
        long totalMessagesProcessed = 0;
        long totalLatencyViolations = 0;
        long totalThroughputViolations = 0;
        long totalBandwidthViolations = 0;

        if (monitoringRegistry != null) {
            // Aggregate statistics from all agent collectors
            for (String agentId : latencyMetrics.keySet()) {
                ExecutionMetricsCollector collector = monitoringRegistry.executionCollector(MetricKeys.agent(agentId));
                var snapshot = collector.snapshot();
                totalMessagesProcessed += snapshot.total();
                // Violations would need separate tracking in a real implementation
            }
        }

        return new PerformanceStatistics(totalMessagesProcessed, totalLatencyViolations, totalThroughputViolations,
                totalBandwidthViolations, latencyMetrics.size(), throughputMetrics.size(), bandwidthMetrics.size());
    }

    /**
     * Get latency metrics for an agent
     */
    public @Nullable MessageLatencyMetrics getLatencyMetrics(String agentId) {
        return latencyMetrics.get(agentId);
    }

    /**
     * Get throughput metrics for an agent
     */
    public @Nullable ThroughputMetrics getThroughputMetrics(String agentId) {
        return throughputMetrics.get(agentId);
    }

    /**
     * Get bandwidth metrics for an agent
     */
    public @Nullable BandwidthMetrics getBandwidthMetrics(String agentId) {
        return bandwidthMetrics.get(agentId);
    }

    /**
     * Get performance history for an agent
     */
    public @Nullable PerformanceHistory getPerformanceHistory(String agentId) {
        return performanceHistory.get(agentId);
    }

    /**
     * Set latency threshold for SLA monitoring
     */
    public void setLatencyThreshold(Duration threshold) {
        maxLatencyThreshold.set(threshold);
        logger.debug("Updated latency threshold: {} ms", threshold.toMillis());
    }

    /**
     * Set throughput threshold for SLA monitoring
     */
    public void setThroughputThreshold(long messagesPerSecond) {
        minThroughputThreshold.set(messagesPerSecond);
        logger.debug("Updated throughput threshold: {} msg/sec", messagesPerSecond);
    }

    /**
     * Set bandwidth threshold for SLA monitoring
     */
    public void setBandwidthThreshold(long bytesPerSecond) {
        maxBandwidthThreshold.set(bytesPerSecond);
        logger.debug("Updated bandwidth threshold: {} bytes/sec", bytesPerSecond);
    }

    /**
     * Get performance optimization suggestions
     */
    public CompletableFuture<List<PerformanceOptimizationSuggestion>> getOptimizationSuggestions(String agentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<PerformanceOptimizationSuggestion> suggestions = new ArrayList<>();

                // Analyze latency metrics
                MessageLatencyMetrics latencyMetrics = this.latencyMetrics.get(agentId);
                if (latencyMetrics != null) {
                    Duration avgLatency = latencyMetrics.getAverageLatency();
                    if (avgLatency != null && avgLatency.compareTo(Duration.ofMillis(50)) > 0) {
                        suggestions.add(new PerformanceOptimizationSuggestion("HIGH_LATENCY",
                                "Consider optimizing message processing or reducing message size",
                                PerformanceImpact.HIGH));
                    }
                }

                // Analyze throughput metrics
                ThroughputMetrics throughputMetrics = this.throughputMetrics.get(agentId);
                if (throughputMetrics != null) {
                    long avgThroughput = throughputMetrics.getAverageThroughput();
                    if (avgThroughput < 500) {
                        suggestions.add(new PerformanceOptimizationSuggestion("LOW_THROUGHPUT",
                                "Consider implementing message batching or parallel processing",
                                PerformanceImpact.MEDIUM));
                    }
                }

                // Analyze bandwidth metrics
                BandwidthMetrics bandwidthMetrics = this.bandwidthMetrics.get(agentId);
                if (bandwidthMetrics != null) {
                    long avgBandwidth = bandwidthMetrics.getAverageBandwidth();
                    if (avgBandwidth > 512 * 1024) { // 512KB/sec
                        suggestions.add(new PerformanceOptimizationSuggestion("HIGH_BANDWIDTH",
                                "Consider compressing messages or reducing message frequency",
                                PerformanceImpact.MEDIUM));
                    }
                }

                return suggestions;

            } catch (Exception e) {
                logger.error("Error generating optimization suggestions for agent: {}", agentId, e);
                return List.of();
            }
        });
    }

    /**
     * Generate performance report
     */
    public CompletableFuture<PerformanceReport> generatePerformanceReport(String agentId, Duration timeRange) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Instant cutoffTime = Instant.now().minus(timeRange);

                // Collect metrics within time range
                MessageLatencyMetrics latencyMetrics = this.latencyMetrics.get(agentId);
                ThroughputMetrics throughputMetrics = this.throughputMetrics.get(agentId);
                BandwidthMetrics bandwidthMetrics = this.bandwidthMetrics.get(agentId);

                PerformanceReport report = new PerformanceReport(agentId, timeRange, cutoffTime, Instant.now(),
                        latencyMetrics != null ? latencyMetrics.getAverageLatency() : null,
                        throughputMetrics != null ? throughputMetrics.getAverageThroughput() : 0L,
                        bandwidthMetrics != null ? bandwidthMetrics.getAverageBandwidth() : 0L, 0L, 0L, 0L); // Violations
                                                                                                             // would
                                                                                                             // need
                                                                                                             // separate
                                                                                                             // tracking
                                                                                                             // in a
                                                                                                             // real
                                                                                                             // implementation

                return report;

            } catch (Exception e) {
                logger.error("Error generating performance report for agent: {}", agentId, e);
                return new PerformanceReport(agentId, timeRange, Instant.now(), Instant.now(), null, 0L, 0L, 0L, 0L,
                        0L);
            }
        });
    }

    /**
     * Run performance benchmark
     */
    public CompletableFuture<PerformanceBenchmark> runBenchmark(String agentId, Duration duration) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Instant startTime = Instant.now();
                Instant endTime = startTime.plus(duration);

                // Collect baseline metrics
                MessageLatencyMetrics baselineLatency = latencyMetrics.get(agentId);
                ThroughputMetrics baselineThroughput = throughputMetrics.get(agentId);
                BandwidthMetrics baselineBandwidth = bandwidthMetrics.get(agentId);

                // Wait for benchmark duration
                Thread.sleep(duration.toMillis());

                // Collect final metrics
                MessageLatencyMetrics finalLatency = latencyMetrics.get(agentId);
                ThroughputMetrics finalThroughput = throughputMetrics.get(agentId);
                BandwidthMetrics finalBandwidth = bandwidthMetrics.get(agentId);

                PerformanceBenchmark benchmark = new PerformanceBenchmark(agentId, startTime, endTime, duration,
                        baselineLatency, finalLatency, baselineThroughput, finalThroughput, baselineBandwidth,
                        finalBandwidth);

                return benchmark;

            } catch (Exception e) {
                logger.error("Error running performance benchmark for agent: {}", agentId, e);
                return new PerformanceBenchmark(agentId, Instant.now(), Instant.now(), Duration.ZERO, null, null, null,
                        null, null, null);
            }
        });
    }

    // Background processing methods
    private void processMetrics() {
        // Update performance history
        Instant now = Instant.now();
        latencyMetrics.forEach((agentId, metrics) -> {
            PerformanceHistory history = performanceHistory.computeIfAbsent(agentId, id -> new PerformanceHistory(id));
            history.addLatencySnapshot(now, metrics.getAverageLatency());
        });

        throughputMetrics.forEach((agentId, metrics) -> {
            PerformanceHistory history = performanceHistory.computeIfAbsent(agentId, id -> new PerformanceHistory(id));
            history.addThroughputSnapshot(now, metrics.getAverageThroughput());
        });

        bandwidthMetrics.forEach((agentId, metrics) -> {
            PerformanceHistory history = performanceHistory.computeIfAbsent(agentId, id -> new PerformanceHistory(id));
            history.addBandwidthSnapshot(now, metrics.getAverageBandwidth());
        });
    }

    private void checkAlerts() {
        // Check for SLA violations and generate alerts
        latencyMetrics.forEach((agentId, metrics) -> {
            Duration avgLatency = metrics.getAverageLatency();
            if (avgLatency != null && avgLatency.compareTo(maxLatencyThreshold.get()) > 0) {
                logger.warn("Performance alert: Agent {} has high latency - {} ms", agentId, avgLatency.toMillis());
            }
        });

        throughputMetrics.forEach((agentId, metrics) -> {
            long avgThroughput = metrics.getAverageThroughput();
            if (avgThroughput < minThroughputThreshold.get()) {
                logger.warn("Performance alert: Agent {} has low throughput - {} msg/sec", agentId, avgThroughput);
            }
        });

        bandwidthMetrics.forEach((agentId, metrics) -> {
            long avgBandwidth = metrics.getAverageBandwidth();
            if (avgBandwidth > maxBandwidthThreshold.get()) {
                logger.warn("Performance alert: Agent {} has high bandwidth usage - {} bytes/sec", agentId,
                        avgBandwidth);
            }
        });
    }

    private void generateOptimizationSuggestions() {
        // Generate periodic optimization suggestions
        latencyMetrics.keySet().forEach(agentId -> {
            getOptimizationSuggestions(agentId).thenAccept(suggestions -> {
                if (!suggestions.isEmpty()) {
                    logger.info("Performance optimization suggestions for agent {}: {}", agentId,
                            suggestions.stream().map(PerformanceOptimizationSuggestion::suggestion).toList());
                }
            });
        });
    }

    private void startBackgroundProcessors() {
        metricsProcessor.scheduleAtFixedRate(this::processMetrics, 0, 60000, TimeUnit.MILLISECONDS); // 1 minute
        alertingProcessor.scheduleAtFixedRate(this::checkAlerts, 0, 30000, TimeUnit.MILLISECONDS); // 30 seconds
        optimizationProcessor.scheduleAtFixedRate(this::generateOptimizationSuggestions, 0, 300000,
                TimeUnit.MILLISECONDS); // 5 minutes
    }

    // Data classes
    // records extracted to top-level: PerformanceStatistics, PerformanceOptimizationSuggestion, PerformanceReport,
    // PerformanceBenchmark

    // enum extracted to top-level: org.openhab.core.ai.agent.infrastructure.performance.PerformanceImpact
}
