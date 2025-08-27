package org.openhab.core.ai.tool.monitoring;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.HealthMetrics;
import org.openhab.core.ai.common.monitoring.api.HealthStatus;
import org.openhab.core.ai.model.api.ModelProviderType;

@NonNullByDefault
public class SystemHealthStatus {
    private final Map<ModelProviderType, HealthMetrics> providerMetrics;
    private final Map<String, HealthMetrics> serviceMetrics;

    public SystemHealthStatus(Map<ModelProviderType, HealthMetrics> providerMetrics,
            Map<String, HealthMetrics> serviceMetrics) {
        this.providerMetrics = providerMetrics;
        this.serviceMetrics = serviceMetrics;
    }

    public Map<ModelProviderType, HealthMetrics> getProviderMetrics() {
        return providerMetrics;
    }

    public Map<String, HealthMetrics> getServiceMetrics() {
        return serviceMetrics;
    }

    public boolean isSystemHealthy() {
        return providerMetrics.values().stream().allMatch(metrics -> metrics.healthStatus() == HealthStatus.HEALTHY)
                && serviceMetrics.values().stream().allMatch(HealthMetrics::isHealthy);
    }
}
