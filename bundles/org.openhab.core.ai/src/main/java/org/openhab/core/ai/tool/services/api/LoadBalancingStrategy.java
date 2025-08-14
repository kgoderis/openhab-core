package org.openhab.core.ai.tool.services.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public enum LoadBalancingStrategy {
    ROUND_ROBIN,
    LEAST_CONNECTIONS,
    WEIGHTED_RESPONSE_TIME,
    HEALTH_BASED
}
