package org.openhab.core.ai.tool.monitoring;

/**
 * Circuit breaker state.
 */
public enum CircuitBreakerState {
    CLOSED, // Normal operation
    OPEN, // Circuit is open, requests are failing
    HALF_OPEN // Testing if service has recovered
}
