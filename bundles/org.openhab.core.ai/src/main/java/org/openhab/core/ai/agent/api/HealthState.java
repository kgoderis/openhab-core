package org.openhab.core.ai.agent.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Extracted from ModelHealthStatus.
 */
@NonNullByDefault
public enum HealthState {
    HEALTHY,
    DEGRADED,
    UNHEALTHY,
    UNKNOWN
}
