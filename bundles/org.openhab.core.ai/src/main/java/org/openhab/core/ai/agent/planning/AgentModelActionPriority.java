package org.openhab.core.ai.agent.planning;

/**
 * Priority levels for agent model actions.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
public enum AgentModelActionPriority {
    /**
     * Critical priority - must be executed immediately and cannot be skipped.
     */
    CRITICAL,

    /**
     * High priority - should be executed as soon as possible.
     */
    HIGH,

    /**
     * Medium priority - normal execution priority.
     */
    MEDIUM,

    /**
     * Low priority - can be executed when resources are available.
     */
    LOW
}
