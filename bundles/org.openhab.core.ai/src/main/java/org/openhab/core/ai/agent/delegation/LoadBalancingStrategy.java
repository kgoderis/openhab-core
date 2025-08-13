package org.openhab.core.ai.agent.delegation;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Load balancing strategies for selecting agents during delegation.
 *
 * <p>Defines the strategy used by the delegation service when multiple agents
 * are capable of handling a request.</p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum LoadBalancingStrategy {
    ROUND_ROBIN,
    LEAST_LOADED,
    CAPABILITY_BASED,
    RANDOM
}


