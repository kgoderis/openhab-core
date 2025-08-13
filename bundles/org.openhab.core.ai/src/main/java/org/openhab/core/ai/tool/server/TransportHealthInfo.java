package org.openhab.core.ai.tool.server;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Transport health information for a tool server transport.
 *
 * <p>
 * Captures current transport type, health status, start time, last error and
 * computed uptime. Extracted from {@link DefaultToolServer} inner class as a
 * top-level type to improve modularity and reuse.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class TransportHealthInfo {

    private final @Nullable TransportType transportType;
    private final boolean healthy;
    private final long startTime;
    private final @Nullable String lastError;
    private final long uptime;

    public TransportHealthInfo(@Nullable TransportType transportType, boolean healthy, long startTime,
            @Nullable String lastError, long uptime) {
        this.transportType = transportType;
        this.healthy = healthy;
        this.startTime = startTime;
        this.lastError = lastError;
        this.uptime = uptime;
    }

    public @Nullable TransportType getTransportType() {
        return transportType;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public long getStartTime() {
        return startTime;
    }

    public @Nullable String getLastError() {
        return lastError;
    }

    public long getUptime() {
        return uptime;
    }

    @Override
    public String toString() {
        return String.format("TransportHealthInfo{type=%s, healthy=%s, startTime=%d, lastError='%s', uptime=%d}",
                transportType, healthy, startTime, lastError, uptime);
    }
}
