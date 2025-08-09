package org.openhab.core.ai.tool.error;

import java.time.Instant;
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
 * Error recovery and graceful degradation manager for MCP server.
 * 
 * This class handles various failure scenarios, implements fallback mechanisms,
 * and provides graceful degradation when services are unavailable.
 * 
 * 
 */
@NonNullByDefault
public class ToolErrorRecoveryManager {

    private static final Logger logger = LoggerFactory.getLogger(ToolErrorRecoveryManager.class);

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
    private final Map<String, CircuitBreakerState> circuitBreakers = new ConcurrentHashMap<>();

    public ToolErrorRecoveryManager(AuditLogger auditLogger) {
        this.auditLogger = auditLogger;
        logger.info("MCP Error Recovery Manager initialized");
    }

    /**
     * Check if error recovery is enabled.
     * 
     * @return true if error recovery is enabled
     */
    public boolean isErrorRecoveryEnabled() {
        return true; // Always enabled for now
    }

    /**
     * Check if the error recovery manager is healthy.
     * 
     * @return true if the manager is healthy
     */
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

        // Check circuit breaker
        CircuitBreakerState circuitBreaker = getCircuitBreaker(errorType);
        if (circuitBreaker != null && circuitBreaker.isOpen()) {
            logger.warn("Circuit breaker is open for error type: {}", errorType);
            return RecoveryAction.FALLBACK;
        }

        // Determine recovery action based on error type and count
        RecoveryAction action = determineRecoveryAction(errorType, errorCount);

        // Update circuit breaker
        updateCircuitBreaker(errorType, action == RecoveryAction.FALLBACK);

        return action;
    }

    /**
     * Record a successful recovery.
     * 
     * @param errorType Type of error that was recovered
     */
    public void recordRecovery(String errorType) {
        totalRecoveries.incrementAndGet();

        // Reset error counter for this type
        errorCounters.remove(errorType);
        lastErrorTimes.remove(errorType);
        lastErrorMessages.remove(errorType);

        // Update circuit breaker
        CircuitBreakerState circuitBreaker = circuitBreakers.get(errorType);
        if (circuitBreaker != null) {
            circuitBreaker.recordSuccess();
        }

        logger.info("Recovery successful for error type: {}", errorType);
    }

    /**
     * Record a fallback action.
     * 
     * @param errorType Type of error that triggered fallback
     * @param fallbackAction Description of fallback action
     */
    public void recordFallback(String errorType, String fallbackAction) {
        totalFallbacks.incrementAndGet();

        logger.info("Fallback executed for error type: {} - {}", errorType, fallbackAction);
        auditLogger.logSecurityViolation(null, "FALLBACK", fallbackAction, "mcp", Instant.now());
    }

    /**
     * Check if a service is healthy.
     * 
     * @param serviceName Service name
     * @return true if service is healthy
     */
    public boolean isServiceHealthy(String serviceName) {
        Boolean healthy = serviceHealth.get(serviceName);
        if (healthy == null) {
            // Assume healthy if not tracked
            return true;
        }
        return healthy;
    }

    /**
     * Update service health status.
     * 
     * @param serviceName Service name
     * @param healthy Health status
     */
    public void updateServiceHealth(String serviceName, boolean healthy) {
        serviceHealth.put(serviceName, healthy);
        serviceLastCheck.put(serviceName, System.currentTimeMillis());

        if (!healthy) {
            logger.warn("Service marked as unhealthy: {}", serviceName);
        } else {
            logger.info("Service marked as healthy: {}", serviceName);
        }
    }

    /**
     * Get error recovery statistics.
     * 
     * @return Error recovery statistics
     */
    public ErrorRecoveryStatistics getErrorRecoveryStatistics() {
        return new ErrorRecoveryStatistics(totalErrors.get(), totalRecoveries.get(), totalFallbacks.get(),
                errorCounters.size(), circuitBreakers.size(), serviceHealth.size());
    }

    /**
     * Get detailed error information.
     * 
     * @return Map of error types to error information
     */
    public Map<String, ErrorInfo> getErrorDetails() {
        Map<String, ErrorInfo> details = new ConcurrentHashMap<>();

        for (String errorType : errorCounters.keySet()) {
            AtomicInteger counter = errorCounters.get(errorType);
            Long lastErrorTime = lastErrorTimes.get(errorType);
            String lastErrorMessage = lastErrorMessages.get(errorType);
            CircuitBreakerState circuitBreaker = circuitBreakers.get(errorType);

            if (counter != null) {
                details.put(errorType, new ErrorInfo(counter.get(), lastErrorTime, lastErrorMessage,
                        circuitBreaker != null ? circuitBreaker.getState() : CircuitBreakerState.State.CLOSED));
            }
        }

        return details;
    }

    /**
     * Determine recovery action based on error type and count.
     * 
     * @param errorType Error type
     * @param errorCount Number of errors
     * @return Recovery action
     */
    private RecoveryAction determineRecoveryAction(String errorType, int errorCount) {
        // Network-related errors
        if (errorType.contains("NETWORK") || errorType.contains("CONNECTION")) {
            if (errorCount <= 3) {
                return RecoveryAction.RETRY;
            } else if (errorCount <= 10) {
                return RecoveryAction.FALLBACK;
            } else {
                return RecoveryAction.DEGRADE;
            }
        }

        // Authentication errors
        if (errorType.contains("AUTH") || errorType.contains("PERMISSION")) {
            if (errorCount <= 5) {
                return RecoveryAction.RETRY;
            } else {
                return RecoveryAction.FALLBACK;
            }
        }

        // Resource errors
        if (errorType.contains("RESOURCE") || errorType.contains("MEMORY")) {
            if (errorCount <= 2) {
                return RecoveryAction.RETRY;
            } else {
                return RecoveryAction.DEGRADE;
            }
        }

        // Tool execution errors
        if (errorType.contains("TOOL") || errorType.contains("EXECUTION")) {
            if (errorCount <= 3) {
                return RecoveryAction.RETRY;
            } else {
                return RecoveryAction.FALLBACK;
            }
        }

        // Default behavior
        if (errorCount <= 5) {
            return RecoveryAction.RETRY;
        } else if (errorCount <= 15) {
            return RecoveryAction.FALLBACK;
        } else {
            return RecoveryAction.DEGRADE;
        }
    }

    /**
     * Get or create circuit breaker for error type.
     * 
     * @param errorType Error type
     * @return Circuit breaker state
     */
    private @Nullable CircuitBreakerState getCircuitBreaker(String errorType) {
        return circuitBreakers.computeIfAbsent(errorType, k -> new CircuitBreakerState());
    }

    /**
     * Update circuit breaker based on recovery action.
     * 
     * @param errorType Error type
     * @param shouldOpen Whether circuit breaker should open
     */
    private void updateCircuitBreaker(String errorType, boolean shouldOpen) {
        CircuitBreakerState circuitBreaker = getCircuitBreaker(errorType);
        if (shouldOpen && circuitBreaker != null) {
            circuitBreaker.recordFailure();
        }
    }

    /**
     * Recovery action enumeration.
     */
    public enum RecoveryAction {
        RETRY, // Retry the operation
        FALLBACK, // Use fallback mechanism
        DEGRADE // Degrade functionality
    }

    /**
     * Circuit breaker state management.
     */
    private static class CircuitBreakerState {
        private static final int FAILURE_THRESHOLD = 5;
        private static final long TIMEOUT_MS = 60000; // 1 minute

        private State state = State.CLOSED;
        private int failureCount = 0;
        private long lastFailureTime = 0;

        public enum State {
            CLOSED, // Normal operation
            OPEN, // Circuit is open (failing)
            HALF_OPEN // Testing if service is recovered
        }

        public boolean isOpen() {
            if (state == State.OPEN) {
                // Check if timeout has passed
                if (System.currentTimeMillis() - lastFailureTime > TIMEOUT_MS) {
                    state = State.HALF_OPEN;
                    logger.info("Circuit breaker transitioning to HALF_OPEN");
                }
            }
            return state == State.OPEN;
        }

        public void recordFailure() {
            failureCount++;
            lastFailureTime = System.currentTimeMillis();

            if (failureCount >= FAILURE_THRESHOLD && state == State.CLOSED) {
                state = State.OPEN;
                logger.warn("Circuit breaker opened after {} failures", failureCount);
            }
        }

        public void recordSuccess() {
            failureCount = 0;
            if (state == State.HALF_OPEN) {
                state = State.CLOSED;
                logger.info("Circuit breaker closed after successful recovery");
            }
        }

        public State getState() {
            return state;
        }
    }

    /**
     * Error information.
     */
    public static class ErrorInfo {
        private final int count;
        private final Long lastErrorTime;
        private final String lastErrorMessage;
        private final CircuitBreakerState.State circuitBreakerState;

        public ErrorInfo(int count, Long lastErrorTime, String lastErrorMessage,
                CircuitBreakerState.State circuitBreakerState) {
            this.count = count;
            this.lastErrorTime = lastErrorTime;
            this.lastErrorMessage = lastErrorMessage;
            this.circuitBreakerState = circuitBreakerState;
        }

        public int getCount() {
            return count;
        }

        public Long getLastErrorTime() {
            return lastErrorTime;
        }

        public String getLastErrorMessage() {
            return lastErrorMessage;
        }

        public CircuitBreakerState.State getCircuitBreakerState() {
            return circuitBreakerState;
        }
    }

    /**
     * Error recovery statistics.
     */
    public static class ErrorRecoveryStatistics {
        private final long totalErrors;
        private final long totalRecoveries;
        private final long totalFallbacks;
        private final int activeErrorTypes;
        private final int activeCircuitBreakers;
        private final int trackedServices;

        public ErrorRecoveryStatistics(long totalErrors, long totalRecoveries, long totalFallbacks,
                int activeErrorTypes, int activeCircuitBreakers, int trackedServices) {
            this.totalErrors = totalErrors;
            this.totalRecoveries = totalRecoveries;
            this.totalFallbacks = totalFallbacks;
            this.activeErrorTypes = activeErrorTypes;
            this.activeCircuitBreakers = activeCircuitBreakers;
            this.trackedServices = trackedServices;
        }

        public long getTotalErrors() {
            return totalErrors;
        }

        public long getTotalRecoveries() {
            return totalRecoveries;
        }

        public long getTotalFallbacks() {
            return totalFallbacks;
        }

        public int getActiveErrorTypes() {
            return activeErrorTypes;
        }

        public int getActiveCircuitBreakers() {
            return activeCircuitBreakers;
        }

        public int getTrackedServices() {
            return trackedServices;
        }

        public double getRecoveryRate() {
            return totalErrors > 0 ? (double) totalRecoveries / totalErrors : 0.0;
        }

        @Override
        public String toString() {
            return String.format(
                    "ErrorRecoveryStatistics{totalErrors=%d, totalRecoveries=%d, totalFallbacks=%d, "
                            + "recoveryRate=%.2f, activeErrorTypes=%d, activeCircuitBreakers=%d, trackedServices=%d}",
                    totalErrors, totalRecoveries, totalFallbacks, getRecoveryRate(), activeErrorTypes,
                    activeCircuitBreakers, trackedServices);
        }
    }
}
