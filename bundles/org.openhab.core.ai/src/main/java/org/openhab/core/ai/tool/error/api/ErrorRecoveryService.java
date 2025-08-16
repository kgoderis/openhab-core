package org.openhab.core.ai.tool.error.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.error.ErrorInfo;
import org.openhab.core.ai.tool.error.ErrorRecoveryStatistics;
import org.openhab.core.ai.tool.error.RecoveryAction;

/**
 * Error Recovery Service Interface
 * 
 * <p>
 * This interface defines the contract for error recovery service implementations that provide:
 * - Error handling and recovery mechanisms
 * - Graceful degradation and fallback strategies
 * - Circuit breaker pattern implementation
 * - Service health monitoring and tracking
 * - Error statistics and reporting
 * - Recovery action determination
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ErrorRecoveryService {

    /**
     * Check if error recovery is enabled
     * 
     * @return true if error recovery is enabled
     */
    boolean isErrorRecoveryEnabled();

    /**
     * Check if the error recovery service is healthy
     * 
     * @return true if the service is healthy
     */
    boolean isHealthy();

    /**
     * Handle an error and determine recovery action
     * 
     * @param errorType Type of error
     * @param errorMessage Error message
     * @param clientId Client identifier (if applicable)
     * @return Recovery action to take
     */
    RecoveryAction handleError(String errorType, String errorMessage, String clientId);

    /**
     * Record a successful recovery
     * 
     * @param errorType Type of error that was recovered
     */
    void recordRecovery(String errorType);

    /**
     * Record a fallback action
     * 
     * @param errorType Type of error
     * @param fallbackAction Fallback action taken
     */
    void recordFallback(String errorType, String fallbackAction);

    /**
     * Check if a service is healthy
     * 
     * @param serviceName Service name
     * @return true if the service is healthy
     */
    boolean isServiceHealthy(String serviceName);

    /**
     * Update service health status
     * 
     * @param serviceName Service name
     * @param healthy true if the service is healthy
     */
    void updateServiceHealth(String serviceName, boolean healthy);

    /**
     * Get error details
     * 
     * @return Map of error details
     */
    Map<String, ErrorInfo> getErrorDetails();

    /**
     * Get error recovery statistics
     * 
     * @return Error recovery statistics
     */
    ErrorRecoveryStatistics getErrorRecoveryStatistics();

    /**
     * Reset error counters for a specific error type
     * 
     * @param errorType Error type to reset
     */
    void resetErrorCounters(String errorType);

    /**
     * Reset all error counters
     */
    void resetAllErrorCounters();

    /**
     * Force circuit breaker to open for a service
     * 
     * @param serviceName Service name
     */
    void forceCircuitBreakerOpen(String serviceName);

    /**
     * Force circuit breaker to close for a service
     * 
     * @param serviceName Service name
     */
    void forceCircuitBreakerClose(String serviceName);

    /**
     * Get total error count
     * 
     * @return Total error count
     */
    long getTotalErrors();

    /**
     * Get total recovery count
     * 
     * @return Total recovery count
     */
    long getTotalRecoveries();

    /**
     * Get total fallback count
     * 
     * @return Total fallback count
     */
    long getTotalFallbacks();

    /**
     * Get error count for a specific error type
     * 
     * @param errorType Error type
     * @return Error count
     */
    int getErrorCount(String errorType);

    /**
     * Get last error time for a specific error type
     * 
     * @param errorType Error type
     * @return Last error time in milliseconds, or null if no errors
     */
    Long getLastErrorTime(String errorType);

    /**
     * Get last error message for a specific error type
     * 
     * @param errorType Error type
     * @return Last error message, or null if no errors
     */
    String getLastErrorMessage(String errorType);

    /**
     * Get service health status
     * 
     * @return Map of service health status
     */
    Map<String, Boolean> getServiceHealthStatus();

    /**
     * Get service last check times
     * 
     * @return Map of service last check times
     */
    Map<String, Long> getServiceLastCheckTimes();
}
