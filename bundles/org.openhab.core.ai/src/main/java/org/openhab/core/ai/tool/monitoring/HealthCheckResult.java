package org.openhab.core.ai.tool.monitoring;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.model.api.ModelProviderType;

/**
 * Health check result for a monitored target (provider or service).
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class HealthCheckResult {
    private final String target;
    private final boolean healthy;
    private final long responseTime;
    private final @Nullable Exception error;

    public HealthCheckResult(String target, boolean healthy, long responseTime, @Nullable Exception error) {
        this.target = target;
        this.healthy = healthy;
        this.responseTime = responseTime;
        this.error = error;
    }

    public HealthCheckResult(ModelProviderType provider, boolean healthy, long responseTime,
            @Nullable Exception error) {
        this.target = provider.name();
        this.healthy = healthy;
        this.responseTime = responseTime;
        this.error = error;
    }

    public String getTarget() {
        return target;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public @Nullable Exception getError() {
        return error;
    }
}
