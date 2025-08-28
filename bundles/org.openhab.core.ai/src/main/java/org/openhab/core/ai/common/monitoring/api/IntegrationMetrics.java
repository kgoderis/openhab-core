package org.openhab.core.ai.common.monitoring.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for integration monitoring metrics.
 * 
 * <p>
 * This interface provides functionality for monitoring external integrations,
 * APIs, and service connections.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface IntegrationMetrics {

    /**
     * Get integration success rate.
     * 
     * @return integration success rate (0.0-1.0)
     */
    double integrationSuccessRate();

    /**
     * Get the number of active integrations.
     * 
     * @return number of active integrations
     */
    int activeIntegrations();

    /**
     * Get the total number of integrations.
     * 
     * @return total number of integrations
     */
    int totalIntegrations();

    /**
     * Get integration response times by service.
     * 
     * @return map of service name to average response time in milliseconds
     */
    Map<String, Double> integrationResponseTimes();

    /**
     * Get integration error rates by service.
     * 
     * @return map of service name to error rate (0.0-1.0)
     */
    Map<String, Double> integrationErrorRates();

    /**
     * Get the last integration check time.
     * 
     * @return last integration check time in milliseconds since epoch
     */
    long lastIntegrationCheckTime();

    /**
     * Get the number of failed integrations.
     * 
     * @return number of failed integrations
     */
    int failedIntegrations();

    /**
     * Get integration availability percentage.
     * 
     * @return integration availability percentage (0.0-100.0)
     */
    default double integrationAvailabilityPercentage() {
        return totalIntegrations() > 0 ? (activeIntegrations() * 100.0) / totalIntegrations() : 0.0;
    }

    /**
     * Get integration failure rate.
     * 
     * @return integration failure rate (0.0-1.0)
     */
    default double integrationFailureRate() {
        return totalIntegrations() > 0 ? (double) failedIntegrations() / totalIntegrations() : 0.0;
    }

    /**
     * Check if integrations are healthy.
     * 
     * @return true if integrations are healthy, false otherwise
     */
    default boolean areIntegrationsHealthy() {
        return integrationSuccessRate() >= 0.95 && integrationAvailabilityPercentage() >= 90.0;
    }

    /**
     * Check if integrations need attention.
     * 
     * @return true if attention is needed, false otherwise
     */
    default boolean doIntegrationsNeedAttention() {
        return integrationSuccessRate() < 0.9 || integrationAvailabilityPercentage() < 80.0;
    }

    /**
     * Get the average integration response time.
     * 
     * @return average response time in milliseconds, or 0.0 if no data
     */
    default double averageIntegrationResponseTime() {
        return integrationResponseTimes().values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    /**
     * Get the slowest integration service.
     * 
     * @return slowest service name, or null if no data
     */
    default String getSlowestIntegrationService() {
        return integrationResponseTimes().entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Get the fastest integration service.
     * 
     * @return fastest service name, or null if no data
     */
    default String getFastestIntegrationService() {
        return integrationResponseTimes().entrySet().stream().min(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Get the most problematic integration service.
     * 
     * @return service with highest error rate, or null if no data
     */
    default String getMostProblematicIntegrationService() {
        return integrationErrorRates().entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Check if a specific integration service is healthy.
     * 
     * @param serviceName the service name
     * @return true if healthy, false otherwise
     */
    default boolean isIntegrationServiceHealthy(String serviceName) {
        Double errorRate = integrationErrorRates().get(serviceName);
        return errorRate != null && errorRate < 0.1;
    }

    /**
     * Get integration health score (0.0 to 1.0).
     * 
     * @return health score between 0.0 and 1.0
     */
    default double getIntegrationHealthScore() {
        double successScore = integrationSuccessRate();
        double availabilityScore = integrationAvailabilityPercentage() / 100.0;
        return (successScore + availabilityScore) / 2.0;
    }
}
