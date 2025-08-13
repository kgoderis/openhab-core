package org.openhab.core.ai.tool.util;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Tool performance metrics.
 *
 * <p>
 * Captures execution timing and versioning metadata for a tool invocation.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ToolPerformanceMetrics {
    private final String toolId;
    private final long executionTimeMs;
    private final long timestamp;
    private final String version;

    public ToolPerformanceMetrics(String toolId, long executionTimeMs, long timestamp, String version) {
        this.toolId = toolId;
        this.executionTimeMs = executionTimeMs;
        this.timestamp = timestamp;
        this.version = version;
    }

    public String getToolId() {
        return toolId;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getVersion() {
        return version;
    }

    public boolean isSlowExecution(long thresholdMs) {
        return executionTimeMs > thresholdMs;
    }

    public String getPerformanceSummary() {
        return String.format("Tool %s executed in %dms (version: %s)", toolId, executionTimeMs, version);
    }
}
