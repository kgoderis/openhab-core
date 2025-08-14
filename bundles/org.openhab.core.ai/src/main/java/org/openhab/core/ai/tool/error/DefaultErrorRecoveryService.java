package org.openhab.core.ai.tool.error;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.auth.AuditLogger;
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
public class DefaultErrorRecoveryService implements org.openhab.core.ai.tool.error.api.ErrorRecoveryService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultErrorRecoveryService.class);

    private final AuditLogger auditLogger;

    // Error tracking
    private final Map<String, AtomicInteger> errorCounters = new ConcurrentHashMap<>();
    private final Map<String, Long> lastErrorTimes = new ConcurrentHashMap<>();
    private final Map<String, String> lastErrorMessages = new ConcurrentHashMap<>();

    // Recovery state
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final AtomicLong totalRecoveries = new AtomicLong(0);
    private final AtomicLong totalFallbacks = new AtomicLong(0);

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
        totalErrors.incrementAndGet();

        // Track error
        AtomicInteger counter = errorCounters.computeIfAbsent(errorType, k -> new AtomicInteger(0));
        int errorCount = counter.incrementAndGet();
        lastErrorTimes.put(errorType, System.currentTimeMillis());
        lastErrorMessages.put(errorType, errorMessage);

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
        totalRecoveries.incrementAndGet();
        logger.info("Recovery recorded for error type: {}", errorType);

        // Reset error counter for this type
        AtomicInteger counter = errorCounters.get(errorType);
        if (counter != null) {
            counter.set(0);
        }

        // Close circuit breaker if it was open
        circuitBreakers.put(errorType, DefaultErrorRecoveryServiceCircuitBreakerState.CLOSED);
    }

    @Override
    public void recordFallback(String errorType, String fallbackAction) {
        totalFallbacks.incrementAndGet();
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

        for (Map.Entry<String, AtomicInteger> entry : errorCounters.entrySet()) {
            String errorType = entry.getKey();
            int count = entry.getValue().get();
            Long lastTime = lastErrorTimes.get(errorType);
            String message = lastErrorMessages.get(errorType);

            details.put(errorType, new ErrorInfo(errorType, message != null ? message : "Unknown error", count,
                    lastTime != null ? lastTime : 0));
        }

        return details;
    }

    @Override
    public ErrorRecoveryStatistics getErrorRecoveryStatistics() {
        Map<String, Long> errorCountsByType = new HashMap<>();
        for (Map.Entry<String, AtomicInteger> entry : errorCounters.entrySet()) {
            errorCountsByType.put(entry.getKey(), (long) entry.getValue().get());
        }

        Map<String, Long> recoveryCountsByStrategy = new HashMap<>();
        // TODO: Implement strategy tracking

        return new ErrorRecoveryStatistics(totalErrors.get(), totalRecoveries.get(), totalFallbacks.get(), 0, // totalFailures
                                                                                                              // - TODO:
                                                                                                              // implement
                errorCountsByType, recoveryCountsByStrategy);
    }

    @Override
    public void resetErrorCounters(String errorType) {
        AtomicInteger counter = errorCounters.get(errorType);
        if (counter != null) {
            counter.set(0);
        }
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
        return totalErrors.get();
    }

    @Override
    public long getTotalRecoveries() {
        return totalRecoveries.get();
    }

    @Override
    public long getTotalFallbacks() {
        return totalFallbacks.get();
    }

    @Override
    public int getErrorCount(String errorType) {
        AtomicInteger counter = errorCounters.get(errorType);
        return counter != null ? counter.get() : 0;
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

    /**
     * Circuit breaker states
     */
    // enum extracted to top-level: org.openhab.core.ai.tool.error.DefaultErrorRecoveryServiceCircuitBreakerState
}
