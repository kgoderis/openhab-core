package org.openhab.core.ai.tool.error.recovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.error.ErrorRecoveryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of ErrorRecoveryStrategy.
 * 
 * This class provides comprehensive error recovery capabilities including:
 * - Error recovery logic with multiple recovery strategies
 * - Error recovery chaining for complex error scenarios
 * - Performance monitoring and metrics collection
 * - Versioning support for recovery strategies
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class DefaultErrorRecoveryStrategy implements ErrorRecoveryStrategy {

    private static final Logger logger = LoggerFactory.getLogger(DefaultErrorRecoveryStrategy.class);

    private final String strategyId;
    private final String strategyName;
    private final String strategyDescription;
    private final String[] supportedErrorTypes;
    private final int priority;
    private final AtomicReference<Boolean> enabled = new AtomicReference<>(true);
    private final Map<String, Object> configuration = new ConcurrentHashMap<>();

    // Performance monitoring
    private final AtomicLong totalRecoveryAttempts = new AtomicLong(0);
    private final AtomicLong successfulRecoveries = new AtomicLong(0);
    private final AtomicLong failedRecoveries = new AtomicLong(0);
    private final AtomicLong totalRecoveryTime = new AtomicLong(0);
    private final Map<String, AtomicInteger> errorTypeRecoveryCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> errorTypeRecoveryTimes = new ConcurrentHashMap<>();

    // Recovery chaining
    private final List<ErrorRecoveryStrategy> chainedStrategies = new ArrayList<>();
    private final AtomicInteger chainDepth = new AtomicInteger(0);
    private final AtomicLong totalChainRecoveries = new AtomicLong(0);

    // Versioning
    private final String version;
    private final AtomicReference<String> currentVersion = new AtomicReference<>();
    private final Map<String, String> versionHistory = new ConcurrentHashMap<>();

    /**
     * Create a new default error recovery strategy.
     * 
     * @param strategyId the strategy ID
     * @param strategyName the strategy name
     * @param strategyDescription the strategy description
     * @param supportedErrorTypes the supported error types
     * @param priority the strategy priority
     * @param version the strategy version
     */
    public DefaultErrorRecoveryStrategy(String strategyId, String strategyName, String strategyDescription,
            String[] supportedErrorTypes, int priority, String version) {
        this.strategyId = strategyId;
        this.strategyName = strategyName;
        this.strategyDescription = strategyDescription;
        this.supportedErrorTypes = supportedErrorTypes;
        this.priority = priority;
        this.version = version;
        this.currentVersion.set(version);

        // Initialize default configuration
        configuration.put("maxRetryAttempts", 3);
        configuration.put("retryDelayMs", 1000);
        configuration.put("enableChaining", true);
        configuration.put("maxChainDepth", 5);
        configuration.put("enablePerformanceMonitoring", true);
        configuration.put("enableVersioning", true);
    }

    @Override
    public String getStrategyId() {
        return strategyId;
    }

    @Override
    public String getStrategyName() {
        return strategyName;
    }

    @Override
    public String getStrategyDescription() {
        return strategyDescription;
    }

    @Override
    public String[] getSupportedErrorTypes() {
        return supportedErrorTypes;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean isEnabled() {
        return enabled.get();
    }

    @Override
    public boolean canHandle(Throwable error) {
        if (!isEnabled()) {
            return false;
        }

        String errorType = getErrorType(error);
        for (String supportedType : supportedErrorTypes) {
            if (supportedType.equals(errorType) || errorType.contains(supportedType)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public ErrorRecoveryResult recover(Throwable error) {
        // Implement error recovery logic
        long startTime = System.currentTimeMillis();
        totalRecoveryAttempts.incrementAndGet();

        try {
            logger.debug("Starting error recovery for strategy: {}", strategyName);

            // Check if we should use chaining
            boolean enableChaining = (Boolean) configuration.getOrDefault("enableChaining", true);
            int maxChainDepth = (Integer) configuration.getOrDefault("maxChainDepth", 5);

            if (enableChaining && chainDepth.get() < maxChainDepth && !chainedStrategies.isEmpty()) {
                return executeChainedRecovery(error);
            }

            // Execute primary recovery logic
            ErrorRecoveryResult result = executePrimaryRecovery(error);

            // Record performance metrics
            long recoveryTime = System.currentTimeMillis() - startTime;
            recordRecoveryMetrics(error, result, recoveryTime);

            return result;

        } catch (Exception e) {
            logger.error("Error during recovery execution: {}", e.getMessage(), e);
            failedRecoveries.incrementAndGet();

            return new ErrorRecoveryResult(false, "FAILED", "EXECUTION_FAILED",
                    "Recovery execution failed: " + e.getMessage(),
                    Map.of("error", e.getMessage(), "strategy", strategyName), System.currentTimeMillis(), 0);
        }
    }

    @Override
    public Map<String, Object> getConfiguration() {
        return new HashMap<>(configuration);
    }

    @Override
    public void updateConfiguration(Map<String, Object> configuration) {
        this.configuration.putAll(configuration);
        logger.debug("Configuration updated for strategy: {}", strategyName);
    }

    /**
     * Add support for error recovery chaining
     * 
     * @param strategy the strategy to chain
     */
    public void addChainedStrategy(ErrorRecoveryStrategy strategy) {
        chainedStrategies.add(strategy);
        logger.debug("Added chained strategy: {} to {}", strategy.getStrategyName(), strategyName);
    }

    /**
     * Remove a chained strategy
     * 
     * @param strategy the strategy to remove
     */
    public void removeChainedStrategy(ErrorRecoveryStrategy strategy) {
        chainedStrategies.remove(strategy);
        logger.debug("Removed chained strategy: {} from {}", strategy.getStrategyName(), strategyName);
    }

    /**
     * Implement error recovery performance monitoring
     * 
     * @return performance metrics
     */
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        long totalAttempts = totalRecoveryAttempts.get();
        long successful = successfulRecoveries.get();
        long failed = failedRecoveries.get();
        long totalTime = totalRecoveryTime.get();

        metrics.put("totalRecoveryAttempts", totalAttempts);
        metrics.put("successfulRecoveries", successful);
        metrics.put("failedRecoveries", failed);
        metrics.put("successRate", totalAttempts > 0 ? (double) successful / totalAttempts : 0.0);
        metrics.put("averageRecoveryTimeMs", totalAttempts > 0 ? (double) totalTime / totalAttempts : 0.0);
        metrics.put("totalChainRecoveries", totalChainRecoveries.get());
        metrics.put("currentChainDepth", chainDepth.get());
        metrics.put("chainedStrategiesCount", chainedStrategies.size());

        // Add error type specific metrics
        Map<String, Object> errorTypeMetrics = new HashMap<>();
        for (Map.Entry<String, AtomicInteger> entry : errorTypeRecoveryCounts.entrySet()) {
            String errorType = entry.getKey();
            int count = entry.getValue().get();
            Long recoveryTime = errorTypeRecoveryTimes.get(errorType).get();

            Map<String, Object> typeMetrics = new HashMap<>();
            typeMetrics.put("count", count);
            typeMetrics.put("averageRecoveryTimeMs", count > 0 ? (double) recoveryTime / count : 0.0);
            errorTypeMetrics.put(errorType, typeMetrics);
        }
        metrics.put("errorTypeMetrics", errorTypeMetrics);

        return metrics;
    }

    /**
     * Add support for error recovery versioning
     * 
     * @param newVersion the new version
     * @param description the version description
     */
    public void updateVersion(String newVersion, String description) {
        String oldVersion = currentVersion.get();
        versionHistory.put(oldVersion, description);
        currentVersion.set(newVersion);
        logger.info("Updated strategy version from {} to {}: {}", oldVersion, newVersion, description);
    }

    /**
     * Get version information
     * 
     * @return version information
     */
    public Map<String, Object> getVersionInfo() {
        Map<String, Object> versionInfo = new HashMap<>();
        versionInfo.put("currentVersion", currentVersion.get());
        versionInfo.put("originalVersion", version);
        versionInfo.put("versionHistory", new HashMap<>(versionHistory));
        return versionInfo;
    }

    /**
     * Execute primary recovery logic
     * 
     * @param error the error to recover from
     * @return recovery result
     */
    private ErrorRecoveryResult executePrimaryRecovery(Throwable error) {
        String errorType = getErrorType(error);
        int maxRetryAttempts = (Integer) configuration.getOrDefault("maxRetryAttempts", 3);
        long retryDelayMs = (Long) configuration.getOrDefault("retryDelayMs", 1000L);

        logger.debug("Executing primary recovery for error type: {}", errorType);

        // Simple retry logic
        for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
            try {
                // Simulate recovery attempt
                Thread.sleep(retryDelayMs);

                // Check if error is recoverable
                if (isRecoverableError(error)) {
                    successfulRecoveries.incrementAndGet();
                    return new ErrorRecoveryResult(true, "RECOVERED", "RETRY_SUCCESS",
                            "Recovery successful on attempt " + attempt,
                            Map.of("attempt", attempt, "errorType", errorType, "strategy", strategyName),
                            System.currentTimeMillis(), 0);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new ErrorRecoveryResult(false, "FAILED", "INTERRUPTED", "Recovery interrupted",
                        Map.of("error", e.getMessage(), "strategy", strategyName), System.currentTimeMillis(), 0);
            } catch (Exception e) {
                logger.warn("Recovery attempt {} failed: {}", attempt, e.getMessage());
            }
        }

        failedRecoveries.incrementAndGet();
        return new ErrorRecoveryResult(false, "FAILED", "MAX_ATTEMPTS_EXCEEDED",
                "Recovery failed after " + maxRetryAttempts + " attempts",
                Map.of("attempts", maxRetryAttempts, "strategy", strategyName), System.currentTimeMillis(), 0);
    }

    /**
     * Execute chained recovery
     * 
     * @param error the error to recover from
     * @return recovery result
     */
    private ErrorRecoveryResult executeChainedRecovery(Throwable error) {
        chainDepth.incrementAndGet();
        totalChainRecoveries.incrementAndGet();

        logger.debug("Executing chained recovery, depth: {}", chainDepth.get());

        try {
            // Try each chained strategy in order
            for (ErrorRecoveryStrategy strategy : chainedStrategies) {
                if (strategy.canHandle(error)) {
                    ErrorRecoveryResult result = strategy.recover(error);
                    if (result.isRecovered()) {
                        successfulRecoveries.incrementAndGet();
                        return result;
                    }
                }
            }

            // If no chained strategy succeeded, fall back to primary recovery
            return executePrimaryRecovery(error);

        } finally {
            chainDepth.decrementAndGet();
        }
    }

    /**
     * Record recovery metrics
     * 
     * @param error the error that was recovered
     * @param result the recovery result
     * @param recoveryTime the recovery time in milliseconds
     */
    private void recordRecoveryMetrics(Throwable error, ErrorRecoveryResult result, long recoveryTime) {
        totalRecoveryTime.addAndGet(recoveryTime);

        String errorType = getErrorType(error);
        errorTypeRecoveryCounts.computeIfAbsent(errorType, k -> new AtomicInteger(0)).incrementAndGet();
        errorTypeRecoveryTimes.computeIfAbsent(errorType, k -> new AtomicLong(0)).addAndGet(recoveryTime);

        logger.debug("Recorded recovery metrics - Type: {}, Success: {}, Time: {}ms", errorType, result.isRecovered(),
                recoveryTime);
    }

    /**
     * Get error type from throwable
     * 
     * @param error the error
     * @return the error type
     */
    private String getErrorType(Throwable error) {
        return error.getClass().getSimpleName();
    }

    /**
     * Check if error is recoverable
     * 
     * @param error the error to check
     * @return true if recoverable
     */
    private boolean isRecoverableError(Throwable error) {
        // Simple heuristic - consider transient errors as recoverable
        String errorType = getErrorType(error);
        String message = error.getMessage();

        if (message != null) {
            String lowerMessage = message.toLowerCase();
            return lowerMessage.contains("timeout") || lowerMessage.contains("connection")
                    || lowerMessage.contains("temporary") || lowerMessage.contains("retry");
        }

        return false;
    }
}
