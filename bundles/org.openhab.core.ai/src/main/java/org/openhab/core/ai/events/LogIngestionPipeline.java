package org.openhab.core.ai.events;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot;
import org.openhab.core.ai.common.monitoring.service.snapshot.UnifiedMetricsSnapshot;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Log Ingestion Pipeline for Autonomous Reasoning
 * 
 * <p>
 * This component provides comprehensive log ingestion capabilities for autonomous reasoning agents:
 * - Real-time log file monitoring and ingestion
 * - Log parsing and structured data extraction
 * - Log event correlation with system events
 * - Log-based anomaly detection
 * - Performance metrics and analysis
 * - Log-to-reasoning context mapping
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = LogIngestionPipeline.class)
@NonNullByDefault
public class LogIngestionPipeline {

    static final Logger logger = LoggerFactory.getLogger(LogIngestionPipeline.class);

    @Reference
    private MetricsService metricsService;

    // Configuration
    private static final String DEFAULT_LOG_DIR = "logs";
    private static final String DEFAULT_LOG_FILE = "openhab.log";
    private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(1);
    private static final int DEFAULT_MAX_LINES_PER_BATCH = 100;
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    // Log parsing patterns
    private static final Pattern LOG_PATTERN = Pattern
            .compile("(d{4}-d{2}-d{2} d{2}:d{2}:d{2}.d{3}) [(.*?)] (w+) - (.*)");
    private static final Pattern ERROR_PATTERN = Pattern.compile("(?i)(error|exception|failed|failure)");
    private static final Pattern WARNING_PATTERN = Pattern.compile("(?i)(warn|warning)");
    private static final Pattern ANOMALY_PATTERN = Pattern
            .compile("(?i)(unexpected|unusual|abnormal|suspicious|anomaly|outlier)");

    // State management
    private final Map<String, LogFileMonitor> logMonitors = new ConcurrentHashMap<>();
    private final Map<String, LogEntry> recentLogs = new ConcurrentHashMap<>();
    private final Map<String, LogAnomaly> detectedAnomalies = new ConcurrentHashMap<>();
    private final List<LogCorrelation> logCorrelations = new ArrayList<>();

    // Performance monitoring now handled by centralized MetricsService

    // Threading
    private final ExecutorService processingExecutor = Executors.newFixedThreadPool(4);
    private volatile boolean isRunning = false;

    // Configuration
    private String logDirectory = DEFAULT_LOG_DIR;
    private String logFileName = DEFAULT_LOG_FILE;
    private Duration pollInterval = DEFAULT_POLL_INTERVAL;
    private int maxLinesPerBatch = DEFAULT_MAX_LINES_PER_BATCH;
    private boolean enableAnomalyDetection = true;
    private boolean enableCorrelation = true;
    private boolean enablePerformanceMonitoring = true;

    /**
     * Record log processing metrics using MetricsService with proper error handling.
     * 
     * @param operationType the type of log processing operation
     * @param success whether the operation was successful
     * @param duration the operation duration in nanoseconds
     * @param dataEntries additional key-value pairs for context
     */
    private void recordLogMetrics(String operationType, boolean success, long duration, String... dataEntries) {
        try {
            var recorder = metricsService.recordOperation("log-processing", operationType).withSuccess(success)
                    .withDuration(duration);

            // Add data entries in pairs
            for (int i = 0; i < dataEntries.length - 1; i += 2) {
                recorder.withData(dataEntries[i], dataEntries[i + 1]);
            }

            recorder.record();
        } catch (Exception e) {
            logger.warn("Failed to record log processing metrics for operation {}: {}", operationType, e.getMessage());
            // Graceful degradation - continue without metrics if recording fails
        }
    }

    @Activate
    public void activate() {
        logger.debug("Log Ingestion Pipeline activated");
        startPipeline();
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Log Ingestion Pipeline deactivated");
        stopPipeline();
    }

    /**
     * Start the log ingestion pipeline
     */
    public void startPipeline() {
        if (isRunning) {
            logger.warn("Log ingestion pipeline is already running");
            return;
        }

        isRunning = true;
        logger.info("Starting log ingestion pipeline for directory: {}", logDirectory);

        try {
            // Start monitoring the main log file
            startLogFileMonitoring(logFileName);

            // Start monitoring additional log files if configured
            startAdditionalLogMonitoring();

            logger.info("Log ingestion pipeline started successfully");
        } catch (Exception e) {
            logger.error("Failed to start log ingestion pipeline", e);
            isRunning = false;
        }
    }

    /**
     * Stop the log ingestion pipeline
     */
    public void stopPipeline() {
        if (!isRunning) {
            return;
        }

        isRunning = false;
        logger.info("Stopping log ingestion pipeline");

        // Stop all log monitors
        logMonitors.values().forEach(LogFileMonitor::stop);
        logMonitors.clear();

        // Shutdown processing executor
        processingExecutor.shutdown();

        logger.info("Log ingestion pipeline stopped");
    }

    /**
     * Start monitoring a specific log file
     */
    public void startLogFileMonitoring(String fileName) {
        if (logMonitors.containsKey(fileName)) {
            logger.warn("Log file {} is already being monitored", fileName);
            return;
        }

        Path logPath = Paths.get(logDirectory, fileName);
        if (!Files.exists(logPath)) {
            logger.warn("Log file does not exist: {}", logPath);
            return;
        }

        LogFileMonitor monitor = new LogFileMonitor(this, fileName, logPath);
        logMonitors.put(fileName, monitor);
        monitor.start();

        logger.info("Started monitoring log file: {}", fileName);
    }

    /**
     * Stop monitoring a specific log file
     */
    public void stopLogFileMonitoring(String fileName) {
        LogFileMonitor monitor = logMonitors.remove(fileName);
        if (monitor != null) {
            monitor.stop();
            logger.info("Stopped monitoring log file: {}", fileName);
        }
    }

    /**
     * Process a batch of log lines
     */
    public CompletableFuture<List<LogEntry>> processLogLines(List<String> logLines) {
        return CompletableFuture.supplyAsync(() -> {
            Instant startTime = Instant.now();
            List<LogEntry> processedEntries = new ArrayList<>();

            try {
                for (String line : logLines) {
                    LogEntry entry = parseLogLine(line);
                    if (entry != null) {
                        processedEntries.add(entry);
                        recentLogs.put(entry.getId(), entry);

                        // Detect anomalies
                        if (enableAnomalyDetection && isAnomaly(entry)) {
                            LogAnomaly anomaly = createAnomaly(entry);
                            detectedAnomalies.put(anomaly.getId(), anomaly);
                            // Record anomaly detection using MetricsService
                            recordLogMetrics("anomaly-detection", true, 0, "entryId", entry.getId(), "severity",
                                    anomaly.getSeverity().toString());
                        }

                        // Correlate with system events
                        if (enableCorrelation) {
                            correlateWithEvents(entry);
                        }
                    }
                }

                // Record log processing metrics using MetricsService
                long processingTimeMs = Duration.between(startTime, Instant.now()).toMillis();
                recordLogMetrics("log-lines-processing", true, processingTimeMs * 1_000_000, // Convert to nanoseconds
                        "linesProcessed", String.valueOf(logLines.size()), "processingTimeMs",
                        String.valueOf(processingTimeMs));

                logger.debug("Processed {} log lines in {}ms", logLines.size(), processingTimeMs);

            } catch (Exception e) {
                logger.error("Error processing log lines", e);
            }

            return processedEntries;
        }, processingExecutor);
    }

    /** Package-private accessor for poll interval used by LogFileMonitor */
    Duration getPollInterval() {
        return pollInterval;
    }

    /**
     * Parse a single log line into a structured LogEntry
     */
    private @Nullable LogEntry parseLogLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        Matcher matcher = LOG_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return null;
        }

        try {
            String timestampStr = matcher.group(1);
            String loggerName = matcher.group(2);
            String level = matcher.group(3);
            String message = matcher.group(4);

            LocalDateTime timestamp = LocalDateTime.parse(timestampStr,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));

            LogEntry entry = new LogEntry(generateLogEntryId(), timestamp, loggerName,
                    LogLevel.valueOf(level.toUpperCase()), message, extractLogMetadata(line), Instant.now());

            return entry;
        } catch (Exception e) {
            logger.debug("Failed to parse log line: {}", line, e);
            return null;
        }
    }

    /**
     * Extract metadata from log line
     */
    private Map<String, Object> extractLogMetadata(String line) {
        Map<String, Object> metadata = new HashMap<>();

        // Extract error information
        if (ERROR_PATTERN.matcher(line).find()) {
            metadata.put("hasError", true);
            metadata.put("errorType", "general");
        }

        // Extract warning information
        if (WARNING_PATTERN.matcher(line).find()) {
            metadata.put("hasWarning", true);
        }

        // Extract anomaly indicators
        if (ANOMALY_PATTERN.matcher(line).find()) {
            metadata.put("hasAnomaly", true);
        }

        // Extract thread information if present
        Pattern threadPattern = Pattern.compile("[([^]]+)]");
        Matcher threadMatcher = threadPattern.matcher(line);
        if (threadMatcher.find()) {
            metadata.put("thread", threadMatcher.group(1));
        }

        return metadata;
    }

    /**
     * Check if a log entry represents an anomaly
     */
    private boolean isAnomaly(LogEntry entry) {
        // Check for error patterns
        if (entry.getLevel() == LogLevel.ERROR) {
            return true;
        }

        // Check for anomaly indicators in message
        if (entry.getMetadata().containsKey("hasAnomaly")) {
            return true;
        }

        // Check for unusual patterns (customizable)
        String message = entry.getMessage().toLowerCase();
        return message.contains("unexpected") || message.contains("unusual") || message.contains("abnormal");
    }

    /**
     * Create an anomaly record from a log entry
     */
    private LogAnomaly createAnomaly(LogEntry entry) {
        return new LogAnomaly(generateAnomalyId(), entry, "log_anomaly", calculateAnomalySeverity(entry),
                entry.getTimestamp(), Instant.now());
    }

    /**
     * Calculate anomaly severity
     */
    private LogAnomalySeverity calculateAnomalySeverity(LogEntry entry) {
        if (entry.getLevel() == LogLevel.ERROR) {
            return LogAnomalySeverity.HIGH;
        } else if (entry.getLevel() == LogLevel.WARN) {
            return LogAnomalySeverity.MEDIUM;
        } else {
            return LogAnomalySeverity.LOW;
        }
    }

    /**
     * Correlate log entry with system events
     */
    private void correlateWithEvents(LogEntry entry) {
        // This would integrate with the EventSystemIntegration
        // For now, we'll create basic correlations
        LogCorrelation correlation = new LogCorrelation(generateCorrelationId(), entry, null, // Would be linked to
                                                                                              // system event
                "log_system_correlation", calculateCorrelationConfidence(entry), Instant.now());

        logCorrelations.add(correlation);
        // Record correlation tracking using MetricsService
        recordLogMetrics("log-correlation", true, 0, "entryId", entry.getId(), "confidence",
                String.valueOf(correlation.getConfidence()));
    }

    /**
     * Calculate correlation confidence
     */
    private double calculateCorrelationConfidence(LogEntry entry) {
        // Simple confidence calculation based on log level and content
        double baseConfidence = 0.5;

        if (entry.getLevel() == LogLevel.ERROR) {
            baseConfidence += 0.3;
        } else if (entry.getLevel() == LogLevel.WARN) {
            baseConfidence += 0.2;
        }

        if (entry.getMetadata().containsKey("hasAnomaly")) {
            baseConfidence += 0.2;
        }

        return Math.min(baseConfidence, 1.0);
    }

    /**
     * Get recent log entries
     */
    public List<LogEntry> getRecentLogs(int limit) {
        return recentLogs.values().stream().sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp())).limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get detected anomalies
     */
    public List<LogAnomaly> getDetectedAnomalies(int limit) {
        return detectedAnomalies.values().stream().sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit).collect(Collectors.toList());
    }

    /**
     * Get log correlations
     */
    public List<LogCorrelation> getLogCorrelations(int limit) {
        return logCorrelations.stream().sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp())).limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get performance metrics from MetricsService
     */
    public GenericMetricsSnapshot getPerformanceMetrics() {
        long totalLogLinesProcessed = getLogProcessingCount("log-lines-processing");
        long totalAnomaliesDetected = getLogProcessingCount("anomaly-detection");
        long totalCorrelationsFound = getLogProcessingCount("log-correlation");
        long totalProcessingTime = getLogProcessingDuration("log-lines-processing");

        return GenericMetricsSnapshot.builder("log-ingestion", "performance")
                .withCounts(totalLogLinesProcessed, totalLogLinesProcessed - totalAnomaliesDetected)
                .withLatency(totalProcessingTime * 1_000_000L) // Convert to nanoseconds
                .withMetric("totalAnomaliesDetected", totalAnomaliesDetected)
                .withMetric("totalCorrelationsFound", totalCorrelationsFound)
                .withMetric("recentLogsCount", recentLogs.size()).withMetric("anomaliesCount", detectedAnomalies.size())
                .withMetric("correlationsCount", logCorrelations.size())
                .withMetric("activeMonitorsCount", logMonitors.size()).build();
    }

    /**
     * Get count from MetricsService for specific log processing operation.
     * 
     * @param operationType the operation type to get count for
     * @return the count or 0 if unavailable
     */
    private long getLogProcessingCount(String operationType) {
        try {
            var metricKey = MetricKeys.custom("log-processing", Map.of("operation", operationType),
                    java.util.Set.of("counts"));
            var snapshot = metricsService.getSnapshot(metricKey, UnifiedMetricsSnapshot.class);
            if (snapshot != null) {
                return snapshot.total();
            }
        } catch (Exception e) {
            logger.warn("Failed to get log processing count for operation {}: {}", operationType, e.getMessage());
        }
        return 0L;
    }

    /**
     * Get total duration from MetricsService for specific log processing operation.
     * 
     * @param operationType the operation type to get duration for
     * @return the total duration in milliseconds or 0 if unavailable
     */
    private long getLogProcessingDuration(String operationType) {
        try {
            var metricKey = MetricKeys.custom("log-processing", Map.of("operation", operationType),
                    java.util.Set.of("latency"));
            var snapshot = metricsService.getSnapshot(metricKey, UnifiedMetricsSnapshot.class);
            if (snapshot != null) {
                return snapshot.totalDurationNanos() / 1_000_000; // Convert to milliseconds
            }
        } catch (Exception e) {
            logger.warn("Failed to get log processing duration for operation {}: {}", operationType, e.getMessage());
        }
        return 0L;
    }

    /**
     * Start monitoring additional log files
     */
    private void startAdditionalLogMonitoring() {
        // This could be configured via configuration files
        // For now, we'll monitor common openHAB log files
        String[] additionalLogs = { "events.log", "automation.log", "persistence.log" };

        for (String logFile : additionalLogs) {
            Path logPath = Paths.get(logDirectory, logFile);
            if (Files.exists(logPath)) {
                startLogFileMonitoring(logFile);
            }
        }
    }

    /**
     * Generate unique log entry ID
     */
    private String generateLogEntryId() {
        return "log_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    /**
     * Generate unique anomaly ID
     */
    private String generateAnomalyId() {
        return "anomaly_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    /**
     * Generate unique correlation ID
     */
    private String generateCorrelationId() {
        return "correlation_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    // Configuration methods
    public void setLogDirectory(String logDirectory) {
        this.logDirectory = logDirectory;
    }

    public void setLogFileName(String logFileName) {
        this.logFileName = logFileName;
    }

    public void setPollInterval(Duration pollInterval) {
        this.pollInterval = pollInterval;
    }

    public void setMaxLinesPerBatch(int maxLinesPerBatch) {
        this.maxLinesPerBatch = maxLinesPerBatch;
    }

    public void setEnableAnomalyDetection(boolean enableAnomalyDetection) {
        this.enableAnomalyDetection = enableAnomalyDetection;
    }

    public void setEnableCorrelation(boolean enableCorrelation) {
        this.enableCorrelation = enableCorrelation;
    }

    public void setEnablePerformanceMonitoring(boolean enablePerformanceMonitoring) {
        this.enablePerformanceMonitoring = enablePerformanceMonitoring;
    }

    // Inner classes extracted to top-level: LogEntry, LogAnomaly, LogCorrelation, LogPerformanceMetrics

    // LogLevel extracted to top-level: org.openhab.core.ai.events.LogLevel

    // Inner class extracted to top-level: org.openhab.core.ai.events.LogFileMonitor
}
