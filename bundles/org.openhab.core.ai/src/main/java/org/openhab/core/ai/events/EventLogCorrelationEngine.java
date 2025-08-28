package org.openhab.core.ai.events;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.snapshot.UnifiedMetricsSnapshot;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event-Log Correlation Engine for Autonomous Reasoning
 * 
 * <p>
 * This component provides intelligent correlation between events and logs:
 * - Temporal correlation of events and log entries
 * - Pattern-based correlation using content analysis
 * - Causality detection between events and log anomalies
 * - Correlation confidence scoring and validation
 * - Real-time correlation processing and updates
 * - Correlation analytics and performance monitoring
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = EventLogCorrelationEngine.class)
@NonNullByDefault
public class EventLogCorrelationEngine {

    private static final Logger logger = LoggerFactory.getLogger(EventLogCorrelationEngine.class);

    // Configuration
    private static final Duration DEFAULT_CORRELATION_WINDOW = Duration.ofMinutes(5);
    private static final double DEFAULT_CONFIDENCE_THRESHOLD = 0.6;
    private static final int DEFAULT_MAX_CORRELATIONS_PER_EVENT = 10;
    private static final int DEFAULT_MAX_CORRELATIONS_PER_LOG = 5;

    // Correlation patterns
    private static final Pattern ERROR_PATTERN = Pattern.compile("(?i)(error|exception|failed|failure)");
    private static final Pattern WARNING_PATTERN = Pattern.compile("(?i)(warn|warning)");
    private static final Pattern EVENT_PATTERN = Pattern.compile("(?i)(event|triggered|activated|deactivated)");
    private static final Pattern DEVICE_PATTERN = Pattern.compile("(?i)(device|thing|item|channel)");

    // State management
    private final Map<String, EventLogCorrelation> correlations = new ConcurrentHashMap<>();
    private final Map<String, List<String>> eventCorrelations = new ConcurrentHashMap<>();
    private final Map<String, List<String>> logCorrelations = new ConcurrentHashMap<>();
    private final Map<String, CorrelationPattern> correlationPatterns = new ConcurrentHashMap<>();

    // Performance monitoring now handled by centralized MetricsService

    // Threading
    private final ExecutorService correlationExecutor = Executors.newFixedThreadPool(4);
    private volatile boolean isRunning = false;

    // Dependencies
    @Reference
    private MetricsService metricsService;

    @Reference
    private @Nullable EventSystemIntegration eventSystemIntegration;

    @Reference
    private @Nullable LogIngestionPipeline logIngestionPipeline;

    // Configuration
    private Duration correlationWindow = DEFAULT_CORRELATION_WINDOW;
    private double confidenceThreshold = DEFAULT_CONFIDENCE_THRESHOLD;
    private int maxCorrelationsPerEvent = DEFAULT_MAX_CORRELATIONS_PER_EVENT;
    private int maxCorrelationsPerLog = DEFAULT_MAX_CORRELATIONS_PER_LOG;
    private boolean enableTemporalCorrelation = true;
    private boolean enablePatternCorrelation = true;
    private boolean enableCausalityDetection = true;
    private boolean enablePerformanceMonitoring = true;

    /**
     * Record correlation metrics using MetricsService with proper error handling.
     * 
     * @param operationType the type of correlation operation
     * @param success whether the operation was successful
     * @param duration the operation duration in nanoseconds
     * @param dataEntries additional key-value pairs for context
     */
    private void recordCorrelationMetrics(String operationType, boolean success, long duration, String... dataEntries) {
        try {
            var recorder = metricsService.recordOperation("event-log-correlation", operationType).withSuccess(success)
                    .withDuration(duration);

            // Add data entries in pairs
            for (int i = 0; i < dataEntries.length - 1; i += 2) {
                recorder.withData(dataEntries[i], dataEntries[i + 1]);
            }

            recorder.record();
        } catch (Exception e) {
            logger.warn("Failed to record correlation metrics for operation {}: {}", operationType, e.getMessage());
            // Graceful degradation - continue without metrics if recording fails
        }
    }

    @Activate
    public void activate() {
        logger.debug("Event-Log Correlation Engine activated");
        startEngine();
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Event-Log Correlation Engine deactivated");
        stopEngine();
    }

    /**
     * Start the correlation engine
     */
    public void startEngine() {
        if (isRunning) {
            logger.warn("Event-Log Correlation Engine is already running");
            return;
        }

        isRunning = true;
        logger.info("Starting Event-Log Correlation Engine");

        try {
            // Initialize correlation patterns
            initializeCorrelationPatterns();

            // Start correlation processing
            startCorrelationProcessing();

            logger.info("Event-Log Correlation Engine started successfully");
        } catch (Exception e) {
            logger.error("Failed to start Event-Log Correlation Engine", e);
            isRunning = false;
        }
    }

    /**
     * Stop the correlation engine
     */
    public void stopEngine() {
        if (!isRunning) {
            return;
        }

        isRunning = false;
        logger.info("Stopping Event-Log Correlation Engine");

        // Shutdown executor
        correlationExecutor.shutdown();

        // Clear correlations
        correlations.clear();
        eventCorrelations.clear();
        logCorrelations.clear();
        correlationPatterns.clear();

        logger.info("Event-Log Correlation Engine stopped");
    }

    /**
     * Correlate events with log entries
     */
    public CompletableFuture<List<EventLogCorrelation>> correlateEventsWithLogs(List<Object> events,
            List<LogEntry> logEntries) {
        return CompletableFuture.supplyAsync(() -> {
            Instant startTime = Instant.now();
            List<EventLogCorrelation> correlations = new ArrayList<>();

            try {
                for (Object event : events) {
                    for (LogEntry logEntry : logEntries) {
                        EventLogCorrelation correlation = createCorrelation(event, logEntry);
                        if (correlation != null && correlation.getConfidence() >= confidenceThreshold) {
                            correlations.add(correlation);
                        }
                    }
                }

                // Store correlations
                for (EventLogCorrelation correlation : correlations) {
                    storeCorrelation(correlation);
                }

                // Record correlation metrics using MetricsService
                long processingTimeMs = Duration.between(startTime, Instant.now()).toMillis();
                recordCorrelationMetrics("events-with-logs", true, processingTimeMs * 1_000_000, // Convert to
                                                                                                 // nanoseconds
                        "correlationsCreated", String.valueOf(correlations.size()), "eventCount",
                        String.valueOf(events.size()), "logEntryCount", String.valueOf(logEntries.size()));

                logger.debug("Created {} correlations between {} events and {} log entries", correlations.size(),
                        events.size(), logEntries.size());

            } catch (Exception e) {
                logger.error("Error correlating events with logs", e);
                // Record error metrics
                long processingTimeMs = Duration.between(startTime, Instant.now()).toMillis();
                recordCorrelationMetrics("events-with-logs", false, processingTimeMs * 1_000_000, "error",
                        e.getMessage() != null ? e.getMessage() : "Unknown error");
            }

            return correlations;
        }, correlationExecutor);
    }

    /**
     * Correlate a single event with log entries
     */
    public CompletableFuture<List<EventLogCorrelation>> correlateEventWithLogs(Object event,
            List<LogEntry> logEntries) {
        return CompletableFuture.supplyAsync(() -> {
            Instant startTime = Instant.now();
            List<EventLogCorrelation> correlations = new ArrayList<>();

            try {
                for (LogEntry logEntry : logEntries) {
                    EventLogCorrelation correlation = createCorrelation(event, logEntry);
                    if (correlation != null && correlation.getConfidence() >= confidenceThreshold) {
                        correlations.add(correlation);
                    }
                }

                // Store correlations
                for (EventLogCorrelation correlation : correlations) {
                    storeCorrelation(correlation);
                }

                // Record correlation metrics using MetricsService
                long processingTimeMs = Duration.between(startTime, Instant.now()).toMillis();
                recordCorrelationMetrics("event-with-logs", true, processingTimeMs * 1_000_000, // Convert to
                                                                                                // nanoseconds
                        "correlationsCreated", String.valueOf(correlations.size()), "logEntryCount",
                        String.valueOf(logEntries.size()));

                logger.debug("Created {} correlations for event with {} log entries", correlations.size(),
                        logEntries.size());

            } catch (Exception e) {
                logger.error("Error correlating event with logs", e);
                // Record error metrics
                long processingTimeMs = Duration.between(startTime, Instant.now()).toMillis();
                recordCorrelationMetrics("event-with-logs", false, processingTimeMs * 1_000_000, "error",
                        e.getMessage() != null ? e.getMessage() : "Unknown error");
            }

            return correlations;
        }, correlationExecutor);
    }

    /**
     * Correlate log entries with events
     */
    public CompletableFuture<List<EventLogCorrelation>> correlateLogsWithEvents(List<LogEntry> logEntries,
            List<Object> events) {
        return CompletableFuture.supplyAsync(() -> {
            Instant startTime = Instant.now();
            List<EventLogCorrelation> correlations = new ArrayList<>();

            try {
                for (LogEntry logEntry : logEntries) {
                    for (Object event : events) {
                        EventLogCorrelation correlation = createCorrelation(event, logEntry);
                        if (correlation != null && correlation.getConfidence() >= confidenceThreshold) {
                            correlations.add(correlation);
                        }
                    }
                }

                // Store correlations
                for (EventLogCorrelation correlation : correlations) {
                    storeCorrelation(correlation);
                }

                // Record correlation metrics using MetricsService
                long processingTimeMs = Duration.between(startTime, Instant.now()).toMillis();
                recordCorrelationMetrics("logs-with-events", true, processingTimeMs * 1_000_000, // Convert to
                                                                                                 // nanoseconds
                        "correlationsCreated", String.valueOf(correlations.size()), "logEntryCount",
                        String.valueOf(logEntries.size()), "eventCount", String.valueOf(events.size()));

                logger.debug("Created {} correlations for {} log entries with {} events", correlations.size(),
                        logEntries.size(), events.size());

            } catch (Exception e) {
                logger.error("Error correlating logs with events", e);
                // Record error metrics
                long processingTimeMs = Duration.between(startTime, Instant.now()).toMillis();
                recordCorrelationMetrics("logs-with-events", false, processingTimeMs * 1_000_000, "error",
                        e.getMessage() != null ? e.getMessage() : "Unknown error");
            }

            return correlations;
        }, correlationExecutor);
    }

    /**
     * Create correlation between event and log entry
     */
    private @Nullable EventLogCorrelation createCorrelation(Object event, LogEntry logEntry) {
        try {
            // Extract event information
            EventInfo eventInfo = extractEventInfo(event);
            if (eventInfo == null) {
                return null;
            }

            // Calculate temporal correlation
            double temporalConfidence = calculateTemporalCorrelation(eventInfo, logEntry);

            // Calculate pattern correlation
            double patternConfidence = calculatePatternCorrelation(eventInfo, logEntry);

            // Calculate causality correlation
            double causalityConfidence = calculateCausalityCorrelation(eventInfo, logEntry);

            // Combine confidence scores
            double totalConfidence = (temporalConfidence + patternConfidence + causalityConfidence) / 3.0;

            if (totalConfidence >= confidenceThreshold) {
                return new EventLogCorrelation(generateCorrelationId(), eventInfo, logEntry, totalConfidence,
                        temporalConfidence, patternConfidence, causalityConfidence,
                        determineCorrelationType(eventInfo, logEntry), Instant.now());
            }

            return null;

        } catch (Exception e) {
            logger.debug("Error creating correlation between event and log entry", e);
            return null;
        }
    }

    /**
     * Extract event information
     */
    private @Nullable EventInfo extractEventInfo(Object event) {
        try {
            // This is a simplified extraction - in a real implementation,
            // this would use reflection or specific event interfaces
            String eventId = "event_" + System.currentTimeMillis();
            String eventType = event.getClass().getSimpleName();
            String eventSource = "unknown";
            Instant eventTimestamp = Instant.now();
            Map<String, Object> eventData = new HashMap<>();

            // Try to extract common event properties
            if (event instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> eventMap = (Map<String, Object>) event;
                eventData.putAll(eventMap);

                if (eventMap.containsKey("id")) {
                    eventId = String.valueOf(eventMap.get("id"));
                }
                if (eventMap.containsKey("type")) {
                    eventType = String.valueOf(eventMap.get("type"));
                }
                if (eventMap.containsKey("source")) {
                    eventSource = String.valueOf(eventMap.get("source"));
                }
                if (eventMap.containsKey("timestamp")) {
                    Object timestamp = eventMap.get("timestamp");
                    if (timestamp instanceof Instant) {
                        eventTimestamp = (Instant) timestamp;
                    } else if (timestamp instanceof String) {
                        // Try to parse timestamp string
                        try {
                            eventTimestamp = Instant.parse((String) timestamp);
                        } catch (Exception e) {
                            // Use current time if parsing fails
                        }
                    }
                }
            }

            return new EventInfo(eventId, eventType, eventSource, eventTimestamp, eventData);

        } catch (Exception e) {
            logger.debug("Error extracting event information", e);
            return null;
        }
    }

    /**
     * Calculate temporal correlation between event and log entry
     */
    private double calculateTemporalCorrelation(EventInfo eventInfo, LogEntry logEntry) {
        if (!enableTemporalCorrelation) {
            return 0.0;
        }

        try {
            // Convert log timestamp to Instant for comparison
            Instant logTimestamp = logEntry.getTimestamp().atZone(ZoneId.systemDefault()).toInstant();
            Duration timeDifference = Duration.between(eventInfo.getTimestamp(), logTimestamp).abs();

            // Calculate confidence based on time difference
            if (timeDifference.compareTo(correlationWindow) <= 0) {
                // Within correlation window
                double normalizedDifference = (double) timeDifference.toMillis() / correlationWindow.toMillis();
                return Math.max(0.0, 1.0 - normalizedDifference);
            } else {
                // Outside correlation window
                return 0.0;
            }

        } catch (Exception e) {
            logger.debug("Error calculating temporal correlation", e);
            return 0.0;
        }
    }

    /**
     * Calculate pattern correlation between event and log entry
     */
    private double calculatePatternCorrelation(EventInfo eventInfo, LogEntry logEntry) {
        if (!enablePatternCorrelation) {
            return 0.0;
        }

        try {
            double confidence = 0.0;
            String eventType = eventInfo.getType().toLowerCase();
            String logMessage = logEntry.getMessage().toLowerCase();

            // Check for error patterns
            if (ERROR_PATTERN.matcher(logMessage).find() && (eventType.contains("error")
                    || eventType.contains("exception") || eventType.contains("failure"))) {
                confidence += 0.4;
            }

            // Check for warning patterns
            if (WARNING_PATTERN.matcher(logMessage).find()
                    && (eventType.contains("warn") || eventType.contains("warning"))) {
                confidence += 0.3;
            }

            // Check for event patterns
            if (EVENT_PATTERN.matcher(logMessage).find()
                    && (eventType.contains("event") || eventType.contains("trigger"))) {
                confidence += 0.3;
            }

            // Check for device patterns
            if (DEVICE_PATTERN.matcher(logMessage).find()
                    && (eventType.contains("device") || eventType.contains("thing") || eventType.contains("item"))) {
                confidence += 0.2;
            }

            // Check for source correlation
            if (eventInfo.getSource().toLowerCase().contains(logEntry.getLoggerName().toLowerCase())
                    || logEntry.getLoggerName().toLowerCase().contains(eventInfo.getSource().toLowerCase())) {
                confidence += 0.2;
            }

            return Math.min(1.0, confidence);

        } catch (Exception e) {
            logger.debug("Error calculating pattern correlation", e);
            return 0.0;
        }
    }

    /**
     * Calculate causality correlation between event and log entry
     */
    private double calculateCausalityCorrelation(EventInfo eventInfo, LogEntry logEntry) {
        if (!enableCausalityDetection) {
            return 0.0;
        }

        try {
            double confidence = 0.0;
            String eventType = eventInfo.getType().toLowerCase();
            String logMessage = logEntry.getMessage().toLowerCase();

            // Check for causal relationships
            if (logEntry.getLevel() == LogLevel.ERROR) {
                // Error logs often indicate causal relationships
                if (eventType.contains("error") || eventType.contains("exception") || eventType.contains("failure")) {
                    confidence += 0.5;
                }
            }

            if (logEntry.getLevel() == LogLevel.WARN) {
                // Warning logs may indicate causal relationships
                if (eventType.contains("warn") || eventType.contains("warning")) {
                    confidence += 0.3;
                }
            }

            // Check for specific causal patterns
            if (logMessage.contains("caused by") || logMessage.contains("due to")
                    || logMessage.contains("because of")) {
                confidence += 0.2;
            }

            // Check for sequence patterns
            if (logMessage.contains("before") || logMessage.contains("after") || logMessage.contains("then")) {
                confidence += 0.1;
            }

            return Math.min(1.0, confidence);

        } catch (Exception e) {
            logger.debug("Error calculating causality correlation", e);
            return 0.0;
        }
    }

    /**
     * Determine correlation type
     */
    private EventLogCorrelationType determineCorrelationType(EventInfo eventInfo, LogEntry logEntry) {
        // Determine correlation type based on event and log characteristics
        if (logEntry.getLevel() == LogLevel.ERROR) {
            return EventLogCorrelationType.ERROR_CORRELATION;
        } else if (logEntry.getLevel() == LogLevel.WARN) {
            return EventLogCorrelationType.WARNING_CORRELATION;
        } else if (eventInfo.getType().toLowerCase().contains("security")) {
            return EventLogCorrelationType.SECURITY_CORRELATION;
        } else if (eventInfo.getType().toLowerCase().contains("performance")) {
            return EventLogCorrelationType.PERFORMANCE_CORRELATION;
        } else {
            return EventLogCorrelationType.GENERAL_CORRELATION;
        }
    }

    /**
     * Store correlation
     */
    private void storeCorrelation(EventLogCorrelation correlation) {
        correlations.put(correlation.getId(), correlation);

        // Store event correlations
        eventCorrelations.computeIfAbsent(correlation.getEventInfo().getId(), k -> new ArrayList<>())
                .add(correlation.getId());

        // Store log correlations
        logCorrelations.computeIfAbsent(correlation.getLogEntry().getId(), k -> new ArrayList<>())
                .add(correlation.getId());

        // Limit correlations per event/log
        limitCorrelations(eventCorrelations.get(correlation.getEventInfo().getId()), maxCorrelationsPerEvent);
        limitCorrelations(logCorrelations.get(correlation.getLogEntry().getId()), maxCorrelationsPerLog);
    }

    /**
     * Limit correlations to maximum count
     */
    private void limitCorrelations(List<String> correlationIds, int maxCount) {
        if (correlationIds.size() > maxCount) {
            // Remove oldest correlations
            correlationIds.subList(0, correlationIds.size() - maxCount).clear();
        }
    }

    /**
     * Get correlations for an event
     */
    public List<EventLogCorrelation> getCorrelationsForEvent(String eventId) {
        List<String> correlationIds = eventCorrelations.get(eventId);
        if (correlationIds == null) {
            return new ArrayList<>();
        }

        return correlationIds.stream().map(correlations::get).filter(correlation -> correlation != null)
                .collect(Collectors.toList());
    }

    /**
     * Get correlations for a log entry
     */
    public List<EventLogCorrelation> getCorrelationsForLog(String logId) {
        List<String> correlationIds = logCorrelations.get(logId);
        if (correlationIds == null) {
            return new ArrayList<>();
        }

        return correlationIds.stream().map(correlations::get).filter(correlation -> correlation != null)
                .collect(Collectors.toList());
    }

    /**
     * Get all correlations
     */
    public List<EventLogCorrelation> getAllCorrelations() {
        return new ArrayList<>(correlations.values());
    }

    /**
     * Validate correlation
     */
    public CompletableFuture<CorrelationValidationResult> validateCorrelation(String correlationId) {
        return CompletableFuture.supplyAsync(() -> {
            EventLogCorrelation correlation = correlations.get(correlationId);
            if (correlation == null) {
                return CorrelationValidationResult.notFound("Correlation not found: " + correlationId);
            }

            try {
                // Recalculate confidence scores
                double temporalConfidence = calculateTemporalCorrelation(correlation.getEventInfo(),
                        correlation.getLogEntry());
                double patternConfidence = calculatePatternCorrelation(correlation.getEventInfo(),
                        correlation.getLogEntry());
                double causalityConfidence = calculateCausalityCorrelation(correlation.getEventInfo(),
                        correlation.getLogEntry());

                double newConfidence = (temporalConfidence + patternConfidence + causalityConfidence) / 3.0;

                // Update correlation if confidence changed significantly
                if (Math.abs(newConfidence - correlation.getConfidence()) > 0.1) {
                    correlation.updateConfidence(newConfidence, temporalConfidence, patternConfidence,
                            causalityConfidence);
                }

                // Record validation metrics using MetricsService
                recordCorrelationMetrics("correlation-validation", true, 0, "correlationId", correlationId,
                        "oldConfidence", String.valueOf(correlation.getConfidence()), "newConfidence",
                        String.valueOf(newConfidence));

                return CorrelationValidationResult.valid(correlation.getConfidence());

            } catch (Exception e) {
                logger.error("Error validating correlation {}", correlationId, e);
                return CorrelationValidationResult.error("Error validating correlation: " + e.getMessage());
            }
        }, correlationExecutor);
    }

    /**
     * Get correlation count from MetricsService for a specific operation type.
     * 
     * @param operationType the operation type to query
     * @return the correlation count, or 0 if not available
     */
    private long getCorrelationCount(String operationType) {
        try {
            var metricKey = MetricKeys.custom("event-log-correlation", Map.of("operation", operationType),
                    Set.of("counts", "latency"));
            var snapshot = metricsService.getSnapshot(metricKey, UnifiedMetricsSnapshot.class);
            if (snapshot != null) {
                Map<String, Object> rawData = snapshot.getRawData();
                if (rawData != null) {
                    Object count = rawData.get("correlationsCreated");
                    return count instanceof Number ? ((Number) count).longValue() : 0L;
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to retrieve correlation count for operation {}: {}", operationType, e.getMessage());
        }
        return 0L;
    }

    /**
     * Get total processing time from MetricsService.
     * 
     * @return the total processing time in milliseconds, or 0 if not available
     */
    private long getTotalProcessingTime() {
        try {
            // Aggregate processing time from all correlation operations
            long totalTime = 0;
            String[] operations = { "events-with-logs", "event-with-logs", "logs-with-events" };

            for (String operation : operations) {
                var metricKey = MetricKeys.custom("event-log-correlation", Map.of("operation", operation),
                        Set.of("counts", "latency"));
                var snapshot = metricsService.getSnapshot(metricKey, UnifiedMetricsSnapshot.class);
                if (snapshot != null) {
                    Map<String, Object> rawData = snapshot.getRawData();
                    if (rawData != null) {
                        Object duration = rawData.get("duration-total-nanos");
                        if (duration instanceof Number) {
                            totalTime += ((Number) duration).longValue() / 1_000_000; // Convert to milliseconds
                        }
                    }
                }
            }
            return totalTime;
        } catch (Exception e) {
            logger.warn("Failed to retrieve total processing time: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * Get performance metrics from MetricsService
     */
    public CorrelationPerformanceMetrics getPerformanceMetrics() {
        long totalCorrelationsCreated = getCorrelationCount("events-with-logs") + getCorrelationCount("event-with-logs")
                + getCorrelationCount("logs-with-events");
        long totalCorrelationsValidated = getCorrelationCount("correlation-validation");
        long totalProcessingTime = getTotalProcessingTime();

        return new CorrelationPerformanceMetrics(totalCorrelationsCreated, totalCorrelationsValidated,
                totalProcessingTime, correlations.size(), eventCorrelations.size(), logCorrelations.size(),
                correlationPatterns.size());
    }

    /**
     * Initialize correlation patterns
     */
    private void initializeCorrelationPatterns() {
        // Initialize predefined correlation patterns
        correlationPatterns.put("error_pattern", new CorrelationPattern("error_pattern", ERROR_PATTERN, 0.8));
        correlationPatterns.put("warning_pattern", new CorrelationPattern("warning_pattern", WARNING_PATTERN, 0.6));
        correlationPatterns.put("event_pattern", new CorrelationPattern("event_pattern", EVENT_PATTERN, 0.7));
        correlationPatterns.put("device_pattern", new CorrelationPattern("device_pattern", DEVICE_PATTERN, 0.5));

        logger.debug("Initialized {} correlation patterns", correlationPatterns.size());
    }

    /**
     * Start correlation processing
     */
    private void startCorrelationProcessing() {
        // Start background correlation tasks
        correlationExecutor.submit(this::processCorrelations);
    }

    /**
     * Process correlations
     */
    private void processCorrelations() {
        while (isRunning) {
            try {
                // Process any pending correlations
                // This could include periodic validation, cleanup, etc.
                Thread.sleep(1000); // Check every second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Error in correlation processing", e);
            }
        }
    }

    /**
     * Generate unique correlation ID
     */
    private String generateCorrelationId() {
        return "correlation_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    // Configuration methods
    public void setCorrelationWindow(Duration correlationWindow) {
        this.correlationWindow = correlationWindow;
    }

    public void setConfidenceThreshold(double confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }

    public void setMaxCorrelationsPerEvent(int maxCorrelationsPerEvent) {
        this.maxCorrelationsPerEvent = maxCorrelationsPerEvent;
    }

    public void setMaxCorrelationsPerLog(int maxCorrelationsPerLog) {
        this.maxCorrelationsPerLog = maxCorrelationsPerLog;
    }

    public void setEnableTemporalCorrelation(boolean enableTemporalCorrelation) {
        this.enableTemporalCorrelation = enableTemporalCorrelation;
    }

    public void setEnablePatternCorrelation(boolean enablePatternCorrelation) {
        this.enablePatternCorrelation = enablePatternCorrelation;
    }

    public void setEnableCausalityDetection(boolean enableCausalityDetection) {
        this.enableCausalityDetection = enableCausalityDetection;
    }

    public void setEnablePerformanceMonitoring(boolean enablePerformanceMonitoring) {
        this.enablePerformanceMonitoring = enablePerformanceMonitoring;
    }

    // (Inner data classes extracted to top-level in org.openhab.core.ai.events:)
    // EventInfo, EventLogCorrelation, CorrelationPattern,
    // CorrelationValidationResult, CorrelationPerformanceMetrics
}
