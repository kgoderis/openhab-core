package org.openhab.core.ai.stub;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;

/**
 * Snapshot class for stub framework metrics.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record StubSnapshot(boolean enabled, int registeredServices, long httpRequestCount,
        long webSocketConnectionCount, long mqttMessageCount, long timestampMs) implements MetricsSnapshot {
}
