package org.openhab.core.ai.tool.monitoring;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.model.api.ModelProviderType;

@NonNullByDefault
public class SystemHealthStatus {
    private final Map<ModelProviderType, ProviderHealthMetrics> providerMetrics;
    private final Map<String, ServiceHealthMetrics> serviceMetrics;

    public SystemHealthStatus(Map<ModelProviderType, ProviderHealthMetrics> providerMetrics,
            Map<String, ServiceHealthMetrics> serviceMetrics) {
        this.providerMetrics = providerMetrics;
        this.serviceMetrics = serviceMetrics;
    }

    public Map<ModelProviderType, ProviderHealthMetrics> getProviderMetrics() { return providerMetrics; }
    public Map<String, ServiceHealthMetrics> getServiceMetrics() { return serviceMetrics; }

    public boolean isSystemHealthy() {
        return providerMetrics.values().stream().allMatch(ProviderHealthMetrics::isHealthy)
                && serviceMetrics.values().stream().allMatch(ServiceHealthMetrics::isHealthy);
    }
}
