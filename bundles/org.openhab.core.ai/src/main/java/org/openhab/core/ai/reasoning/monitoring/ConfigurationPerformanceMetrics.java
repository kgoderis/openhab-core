package org.openhab.core.ai.reasoning.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractMetrics;

/**
 * Consolidated configuration performance metrics that extends the unified monitoring framework.
 * 
 * <p>
 * This class provides comprehensive performance metrics for configuration operations including
 * policy updates, preference updates, constraint updates, and configuration management.
 * It implements CountsMetrics and LatencyMetrics capability interfaces.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ConfigurationPerformanceMetrics extends AbstractMetrics implements CountsMetrics, LatencyMetrics {

    private final long totalPolicyUpdates;
    private final long totalPreferenceUpdates;
    private final long totalConstraintUpdates;
    private final int agentConfigurationCount;
    private final int behaviorPolicyCount;
    private final int userPreferenceCount;
    private final int constraintDefinitionCount;
    private final int safetyPolicyCount;

    /**
     * Create a new ConfigurationPerformanceMetrics instance.
     * 
     * @param id unique identifier for this metrics instance
     * @param timestamp timestamp when metrics were collected
     * @param totalOperations total number of configuration operations
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param lastOperationTime timestamp of last operation
     * @param totalPolicyUpdates total number of policy updates
     * @param totalPreferenceUpdates total number of preference updates
     * @param totalConstraintUpdates total number of constraint updates
     * @param agentConfigurationCount number of agent configurations
     * @param behaviorPolicyCount number of behavior policies
     * @param userPreferenceCount number of user preferences
     * @param constraintDefinitionCount number of constraint definitions
     * @param safetyPolicyCount number of safety policies
     * @param data additional monitoring data
     */
    public ConfigurationPerformanceMetrics(String id, Instant timestamp, long totalOperations,
            long successfulOperations, long failedOperations, long totalProcessingTime, double averageResponseTime,
            @Nullable Instant lastOperationTime, long totalPolicyUpdates, long totalPreferenceUpdates,
            long totalConstraintUpdates, int agentConfigurationCount, int behaviorPolicyCount, int userPreferenceCount,
            int constraintDefinitionCount, int safetyPolicyCount, @Nullable Map<String, Object> data) {
        super(id, timestamp, "reasoning", "configuration-performance", "Configuration performance metrics", data,
                totalOperations, successfulOperations, failedOperations, totalProcessingTime, averageResponseTime,
                lastOperationTime);
        this.totalPolicyUpdates = totalPolicyUpdates;
        this.totalPreferenceUpdates = totalPreferenceUpdates;
        this.totalConstraintUpdates = totalConstraintUpdates;
        this.agentConfigurationCount = agentConfigurationCount;
        this.behaviorPolicyCount = behaviorPolicyCount;
        this.userPreferenceCount = userPreferenceCount;
        this.constraintDefinitionCount = constraintDefinitionCount;
        this.safetyPolicyCount = safetyPolicyCount;
    }

    /**
     * Create a new ConfigurationPerformanceMetrics instance with current timestamp.
     * 
     * @param id unique identifier for this metrics instance
     * @param totalOperations total number of configuration operations
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param totalPolicyUpdates total number of policy updates
     * @param totalPreferenceUpdates total number of preference updates
     * @param totalConstraintUpdates total number of constraint updates
     * @param agentConfigurationCount number of agent configurations
     * @param behaviorPolicyCount number of behavior policies
     * @param userPreferenceCount number of user preferences
     * @param constraintDefinitionCount number of constraint definitions
     * @param safetyPolicyCount number of safety policies
     */
    public ConfigurationPerformanceMetrics(String id, long totalOperations, long successfulOperations,
            long failedOperations, long totalProcessingTime, double averageResponseTime, long totalPolicyUpdates,
            long totalPreferenceUpdates, long totalConstraintUpdates, int agentConfigurationCount,
            int behaviorPolicyCount, int userPreferenceCount, int constraintDefinitionCount, int safetyPolicyCount) {
        this(id, Instant.now(), totalOperations, successfulOperations, failedOperations, totalProcessingTime,
                averageResponseTime, null, totalPolicyUpdates, totalPreferenceUpdates, totalConstraintUpdates,
                agentConfigurationCount, behaviorPolicyCount, userPreferenceCount, constraintDefinitionCount,
                safetyPolicyCount, null);
    }

    /**
     * Get the total number of policy updates.
     * 
     * @return total policy updates
     */
    public long getTotalPolicyUpdates() {
        return totalPolicyUpdates;
    }

    /**
     * Get the total number of preference updates.
     * 
     * @return total preference updates
     */
    public long getTotalPreferenceUpdates() {
        return totalPreferenceUpdates;
    }

    /**
     * Get the total number of constraint updates.
     * 
     * @return total constraint updates
     */
    public long getTotalConstraintUpdates() {
        return totalConstraintUpdates;
    }

    /**
     * Get the number of agent configurations.
     * 
     * @return agent configuration count
     */
    public int getAgentConfigurationCount() {
        return agentConfigurationCount;
    }

    /**
     * Get the number of behavior policies.
     * 
     * @return behavior policy count
     */
    public int getBehaviorPolicyCount() {
        return behaviorPolicyCount;
    }

    /**
     * Get the number of user preferences.
     * 
     * @return user preference count
     */
    public int getUserPreferenceCount() {
        return userPreferenceCount;
    }

    /**
     * Get the number of constraint definitions.
     * 
     * @return constraint definition count
     */
    public int getConstraintDefinitionCount() {
        return constraintDefinitionCount;
    }

    /**
     * Get the number of safety policies.
     * 
     * @return safety policy count
     */
    public int getSafetyPolicyCount() {
        return safetyPolicyCount;
    }

    // CountsMetrics interface implementation
    @Override
    public long total() {
        return getTotalOperations();
    }

    @Override
    public long success() {
        return getSuccessfulOperations();
    }

    @Override
    public long failure() {
        return getFailedOperations();
    }

    // LatencyMetrics interface implementation
    @Override
    public long totalDurationNanos() {
        return getTotalProcessingTime();
    }

    /**
     * Get the policy update rate.
     * 
     * @return policy update rate as a percentage
     */
    public double getPolicyUpdateRate() {
        return total() > 0 ? (double) totalPolicyUpdates / total() * 100.0 : 0.0;
    }

    /**
     * Get the preference update rate.
     * 
     * @return preference update rate as a percentage
     */
    public double getPreferenceUpdateRate() {
        return total() > 0 ? (double) totalPreferenceUpdates / total() * 100.0 : 0.0;
    }

    /**
     * Get the constraint update rate.
     * 
     * @return constraint update rate as a percentage
     */
    public double getConstraintUpdateRate() {
        return total() > 0 ? (double) totalConstraintUpdates / total() * 100.0 : 0.0;
    }

    /**
     * Get the configuration efficiency score.
     * 
     * @return configuration efficiency score between 0.0 and 1.0
     */
    public double getConfigurationEfficiency() {
        double successRate = successRate();
        double policyUpdateRate = total() > 0 ? (double) totalPolicyUpdates / total() : 0.0;
        double preferenceUpdateRate = total() > 0 ? (double) totalPreferenceUpdates / total() : 0.0;
        double constraintUpdateRate = total() > 0 ? (double) totalConstraintUpdates / total() : 0.0;
        double latencyScore = getAverageResponseTime() < 500 ? 1.0
                : getAverageResponseTime() < 1000 ? 0.8 : getAverageResponseTime() < 2000 ? 0.6 : 0.4;

        return (successRate * 0.4) + (policyUpdateRate * 0.2) + (preferenceUpdateRate * 0.2)
                + (constraintUpdateRate * 0.1) + (latencyScore * 0.1);
    }

    /**
     * Get the total configuration complexity (sum of all configuration types).
     * 
     * @return total configuration complexity
     */
    public int getTotalConfigurationComplexity() {
        return agentConfigurationCount + behaviorPolicyCount + userPreferenceCount + constraintDefinitionCount
                + safetyPolicyCount;
    }

    /**
     * Check if configuration management is performing well (high success rate, good update distribution).
     * 
     * @return true if configuration management is performing well
     */
    public boolean isPerformingWell() {
        return successRate() > 0.9 && getAverageResponseTime() < 1000;
    }

    /**
     * Check if the configuration is complex (high number of configuration types).
     * 
     * @return true if the configuration is complex
     */
    public boolean isConfigurationComplex() {
        return getTotalConfigurationComplexity() > 20;
    }

    /**
     * Check if there are frequent configuration changes (high update rates).
     * 
     * @return true if there are frequent configuration changes
     */
    public boolean hasFrequentChanges() {
        return getPolicyUpdateRate() > 30.0 || getPreferenceUpdateRate() > 30.0 || getConstraintUpdateRate() > 20.0;
    }
}
