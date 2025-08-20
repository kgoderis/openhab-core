package org.openhab.core.ai.common.services;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Unified load balancing strategy enumeration for openHAB AI components.
 * 
 * Defines the different load balancing strategies that can be used across
 * agent delegation, tool services, and other AI components.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum LoadBalancingStrategy {
    /**
     * Round-robin load balancing.
     * Distributes requests in a circular manner.
     */
    ROUND_ROBIN("round_robin", "Round Robin"),

    /**
     * Least loaded load balancing.
     * Selects the agent/service with the lowest current load.
     */
    LEAST_LOADED("least_loaded", "Least Loaded"),

    /**
     * Least connections load balancing.
     * Selects the service with the fewest active connections.
     */
    LEAST_CONNECTIONS("least_connections", "Least Connections"),

    /**
     * Capability-based load balancing.
     * Selects based on agent capabilities and requirements.
     */
    CAPABILITY_BASED("capability_based", "Capability Based"),

    /**
     * Weighted response time load balancing.
     * Selects based on weighted response times.
     */
    WEIGHTED_RESPONSE_TIME("weighted_response_time", "Weighted Response Time"),

    /**
     * Health-based load balancing.
     * Selects based on health status and availability.
     */
    HEALTH_BASED("health_based", "Health Based"),

    /**
     * Random load balancing.
     * Randomly selects from available options.
     */
    RANDOM("random", "Random");

    private final String code;
    private final String description;

    /**
     * Create a new load balancing strategy.
     *
     * @param code the strategy code
     * @param description the strategy description
     */
    LoadBalancingStrategy(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Get the strategy code.
     *
     * @return the strategy code
     */
    public String getCode() {
        return code;
    }

    /**
     * Get the strategy description.
     *
     * @return the strategy description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get load balancing strategy by code.
     *
     * @param code the strategy code
     * @return the load balancing strategy, or null if not found
     */
    public static LoadBalancingStrategy fromCode(String code) {
        if (code == null) {
            return null;
        }

        for (LoadBalancingStrategy strategy : values()) {
            if (strategy.code.equalsIgnoreCase(code)) {
                return strategy;
            }
        }
        return null;
    }

    /**
     * Check if this strategy is suitable for agent delegation.
     *
     * @return true if suitable for agent delegation
     */
    public boolean isAgentDelegationStrategy() {
        return this == ROUND_ROBIN || this == LEAST_LOADED || this == CAPABILITY_BASED || this == RANDOM;
    }

    /**
     * Check if this strategy is suitable for service load balancing.
     *
     * @return true if suitable for service load balancing
     */
    public boolean isServiceLoadBalancingStrategy() {
        return this == ROUND_ROBIN || this == LEAST_CONNECTIONS || this == WEIGHTED_RESPONSE_TIME
                || this == HEALTH_BASED;
    }

    @Override
    public String toString() {
        return code + " (" + description + ")";
    }
}
