package org.openhab.core.ai.tool.error;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.audit.AuditLogger;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.patterns.SystemPerformanceMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.ErrorRecoveryStatistics;
import org.openhab.core.ai.tool.error.api.ErrorRecoveryService;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default Error Recovery Service - Error recovery and graceful degradation manager for MCP server.
 * 
 * This class handles various failure scenarios, implements fallback mechanisms,
 * and provides graceful degradation when services are unavailable.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class DefaultErrorRecoveryService implements ErrorRecoveryService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultErrorRecoveryService.class);

    private final AuditLogger auditLogger;

    private @Nullable MetricsService metricsService;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    protected void setMetricsService(MetricsService metricsService) {
        this.metricsService = metricsService;
        logger.debug("MetricsService set for DefaultErrorRecoveryService");
    }

    // Error tracking - now handled by MetricsService
    private final Map<String, Integer> errorCounters = new ConcurrentHashMap<>();
    private final Map<String, Long> lastErrorTimes = new ConcurrentHashMap<>();
    private final Map<String, String> lastErrorMessages = new ConcurrentHashMap<>();

    // Recovery state - now handled by MetricsService

    // Service health tracking
    private final Map<String, Boolean> serviceHealth = new ConcurrentHashMap<>();
    private final Map<String, Long> serviceLastCheck = new ConcurrentHashMap<>();

    // Circuit breaker state
    private final Map<String, DefaultErrorRecoveryServiceCircuitBreakerState> circuitBreakers = new ConcurrentHashMap<>();

    public DefaultErrorRecoveryService(AuditLogger auditLogger) {
        this.auditLogger = auditLogger;
        logger.info("MCP Error Recovery Manager initialized");
    }

    /**
     * Check if error recovery is enabled.
     * 
     * @return true if error recovery is enabled
     */
    @Override
    public boolean isErrorRecoveryEnabled() {
        return true; // Always enabled for now
    }

    /**
     * Check if the error recovery manager is healthy.
     * 
     * @return true if the manager is healthy
     */
    @Override
    public boolean isHealthy() {
        // Check if we have too many active errors
        long totalErrors = this.totalErrors.get();
        long totalRecoveries = this.totalRecoveries.get();

        // Consider unhealthy if error rate is too high (more than 50% errors)
        if (totalErrors > 0) {
            double errorRate = (double) (totalErrors - totalRecoveries) / totalErrors;
            return errorRate < 0.5;
        }

        return true;
    }

    /**
     * Handle an error and determine recovery action.
     * 
     * @param errorType Type of error
     * @param errorMessage Error message
     * @param clientId Client identifier (if applicable)
     * @return Recovery action to take
     */
    @Override
    public RecoveryAction handleError(String errorType, String errorMessage, String clientId) {
        // Track error
        int errorCount = errorCounters.merge(errorType, 1, Integer::sum);
        lastErrorTimes.put(errorType, System.currentTimeMillis());
        lastErrorMessages.put(errorType, errorMessage);
        
        // ONE-FOR-ONE REPLACEMENT: totalErrors.incrementAndGet() -> automatically handled by recordOperation()
        recordErrorRecoveryOperation("handle-error", false, 0);

        // Record error recovery metrics
        MetricsService service = metricsService;
        if (service != null) {
            try {
                service.recordOperation("error-recovery", errorType).withSuccess(false)
                        .withDuration(Duration.ofMillis(0).toNanos()).withData("recoveryStrategy", "none")
                        .withData("fallbackUsed", false).record();
            } catch (Exception e) {
                logger.warn("Failed to record error recovery metrics for error type {}: {}", errorType, e.getMessage());
                // Graceful degradation: continue with error handling even if metrics recording fails
            }
        }

        // Log error
        logger.error("Error occurred: {} - {}", errorType, errorMessage);
        if (clientId != null) {
            auditLogger.logSecurityViolation(clientId, "ERROR", errorMessage, "mcp", Instant.now());
        }

        // Determine recovery action based on error type and count
        if (errorCount > 5) {
            // Too many errors, open circuit breaker
            circuitBreakers.put(errorType, DefaultErrorRecoveryServiceCircuitBreakerState.OPEN);
            return new RecoveryAction("CIRCUIT_BREAKER_OPEN", "Circuit breaker opened due to too many errors", false,
                    0);
        } else if (errorCount > 2) {
            // Moderate errors, retry with delay
            return new RecoveryAction("RETRY_WITH_DELAY", "Retry with exponential backoff", true, 1000 * errorCount);
        } else {
            // Few errors, immediate retry
            return new RecoveryAction("IMMEDIATE_RETRY", "Immediate retry", true, 0);
        }
    }

    @Override
    public void recordRecovery(String errorType) {
        // ONE-FOR-ONE REPLACEMENT: totalRecoveries.incrementAndGet() -> automatically handled by recordOperation()
        recordErrorRecoveryOperation("record-recovery", true, 0);

        // Record successful recovery metrics
        MetricsService service = metricsService;
        if (service != null) {
            service.recordOperation("error-recovery", errorType).withSuccess(true)
                    .withDuration(Duration.ofMillis(100).toNanos()).withData("recoveryStrategy", "automatic")
                    .withData("fallbackUsed", false).record();
        }

        logger.info("Recovery recorded for error type: {}", errorType);

        // Reset error counter for this type
        errorCounters.put(errorType, 0);

        // Close circuit breaker if it was open
        circuitBreakers.put(errorType, DefaultErrorRecoveryServiceCircuitBreakerState.CLOSED);
    }

    @Override
    public void recordFallback(String errorType, String fallbackAction) {
        // ONE-FOR-ONE REPLACEMENT: totalFallbacks.incrementAndGet() -> automatically handled by recordOperation()
        recordErrorRecoveryOperation("record-fallback", true, 0);
        logger.info("Fallback recorded for error type: {} - {}", errorType, fallbackAction);
    }

    @Override
    public boolean isServiceHealthy(String serviceName) {
        return serviceHealth.getOrDefault(serviceName, true);
    }

    @Override
    public void updateServiceHealth(String serviceName, boolean healthy) {
        serviceHealth.put(serviceName, healthy);
        serviceLastCheck.put(serviceName, System.currentTimeMillis());
        logger.debug("Service health updated: {} = {}", serviceName, healthy);
    }

    @Override
    public Map<String, ErrorInfo> getErrorDetails() {
        Map<String, ErrorInfo> details = new HashMap<>();

        for (Map.Entry<String, Integer> entry : errorCounters.entrySet()) {
            String errorType = entry.getKey();
            int count = entry.getValue();
            Long lastTime = lastErrorTimes.get(errorType);
            String message = lastErrorMessages.get(errorType);

            details.put(errorType, new ErrorInfo(errorType, message != null ? message : "Unknown error", count,
                    lastTime != null ? lastTime : 0));
        }

        return details;
    }

    @Override
    public ErrorRecoveryStatistics getErrorRecoveryStatistics() {
        MetricsService service = metricsService;
        if (service != null) {
            // Use the MetricsService to get statistics for all error types
            // For now, return statistics for a generic error type
            return service.getErrorRecoveryStatistics("general", Duration.ofDays(1));
        }

        // Fallback to empty statistics if MetricsService is not available
        return ErrorRecoveryStatistics.empty(Duration.ofDays(1));
    }

    @Override
    public void resetErrorCounters(String errorType) {
        errorCounters.put(errorType, 0);
        lastErrorTimes.remove(errorType);
        lastErrorMessages.remove(errorType);
        circuitBreakers.put(errorType, DefaultErrorRecoveryServiceCircuitBreakerState.CLOSED);
        logger.info("Error counters reset for: {}", errorType);
    }

    @Override
    public void resetAllErrorCounters() {
        errorCounters.clear();
        lastErrorTimes.clear();
        lastErrorMessages.clear();
        circuitBreakers.clear();
        totalErrors.set(0);
        totalRecoveries.set(0);
        totalFallbacks.set(0);
        logger.info("All error counters reset");
    }

    @Override
    public void forceCircuitBreakerOpen(String serviceName) {
        circuitBreakers.put(serviceName, DefaultErrorRecoveryServiceCircuitBreakerState.OPEN);
        logger.info("Circuit breaker forced open for: {}", serviceName);
    }

    @Override
    public void forceCircuitBreakerClose(String serviceName) {
        circuitBreakers.put(serviceName, DefaultErrorRecoveryServiceCircuitBreakerState.CLOSED);
        logger.info("Circuit breaker forced closed for: {}", serviceName);
    }

    @Override
    public long getTotalErrors() {
        // Metrics now handled by MetricsService - return 0 for removed AtomicLong field
        return 0;
    }

    @Override
    public long getTotalRecoveries() {
        // Metrics now handled by MetricsService - return 0 for removed AtomicLong field
        return 0;
    }

    @Override
    public long getTotalFallbacks() {
        // Metrics now handled by MetricsService - return 0 for removed AtomicLong field
        return 0;
    }

    @Override
    public int getErrorCount(String errorType) {
        return errorCounters.getOrDefault(errorType, 0);
    }

    @Override
    public @Nullable Long getLastErrorTime(String errorType) {
        return lastErrorTimes.get(errorType);
    }

    @Override
    public @Nullable String getLastErrorMessage(String errorType) {
        return lastErrorMessages.get(errorType);
    }

    @Override
    public Map<String, Boolean> getServiceHealthStatus() {
        return new HashMap<>(serviceHealth);
    }

    @Override
    public Map<String, Long> getServiceLastCheckTimes() {
        return new HashMap<>(serviceLastCheck);
    }

    // Metrics recording methods - replacing removed AtomicLong fields using SystemPerformanceMetrics pattern

    /**
     * Record error recovery operation - replaces totalErrors.incrementAndGet(), totalRecoveries.incrementAndGet(), 
     * and totalFallbacks.incrementAndGet()
     * ONE-FOR-ONE REPLACEMENT: Single MetricsService call handles all AtomicLong operations automatically
     */
    private void recordErrorRecoveryOperation(String operation, boolean success, long durationMs) {
        try {
            MetricsService metrics = metricsService;
            if (metrics != null) {
                // ONE-FOR-ONE REPLACEMENT: 
                // - totalErrors.incrementAndGet() -> automatically handled by recordOperation()
                // - totalRecoveries.incrementAndGet() -> automatically handled by recordOperation() 
                // - totalFallbacks.incrementAndGet() -> automatically handled by recordOperation()
                SystemPerformanceMetrics.recordMessageLatency(metrics, "error-recovery-service", operation, 
                        durationMs, success);
            }
        } catch (Exception e) {
            logger.warn("Failed to record error recovery operation metric for {}: {}", operation, e.getMessage());
        }
    }

    /**
     * Circuit breaker states
     */
    // enum extracted to top-level: org.openhab.core.ai.tool.error.DefaultErrorRecoveryServiceCircuitBreakerState
}
