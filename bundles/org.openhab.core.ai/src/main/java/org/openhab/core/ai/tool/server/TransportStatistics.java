package org.openhab.core.ai.tool.server;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Transport statistics for a tool server transport.
 *
 * <p>
 * Aggregates current and configured transport types, timing and health metrics,
 * and the underlying transport class name. Extracted from
 * {@link DefaultToolServer} inner class.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class TransportStatistics {

    private final @Nullable TransportType currentType;
    private final long startTime;
    private final long uptime;
    private final boolean healthy;
    private final @Nullable String lastError;
    private final String transportClass;
    private final TransportType configuredType;

    public TransportStatistics(@Nullable TransportType currentType, long startTime, long uptime, boolean healthy,
            @Nullable String lastError, String transportClass, TransportType configuredType) {
        this.currentType = currentType;
        this.startTime = startTime;
        this.uptime = uptime;
        this.healthy = healthy;
        this.lastError = lastError;
        this.transportClass = transportClass;
        this.configuredType = configuredType;
    }

    public @Nullable TransportType getCurrentType() {
        return currentType;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getUptime() {
        return uptime;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public @Nullable String getLastError() {
        return lastError;
    }

    public String getTransportClass() {
        return transportClass;
    }

    public TransportType getConfiguredType() {
        return configuredType;
    }

    public boolean isUsingFallback() {
        return currentType != null && currentType != configuredType;
    }

    @Override
    public String toString() {
        return String.format(
                "TransportStatistics{currentType=%s, startTime=%d, uptime=%d, healthy=%s, lastError='%s', transportClass='%s', configuredType=%s}",
                currentType, startTime, uptime, healthy, lastError, transportClass, configuredType);
    }
}
