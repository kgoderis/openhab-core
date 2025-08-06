package org.openhab.core.ai.agent;

/**
 * State enumeration for autonomous agents
 * 
 * <p>
 * This enum defines the possible states of an autonomous agent:
 * - INITIALIZING: Agent is being initialized
 * - READY: Agent is ready to accept actions
 * - RUNNING: Agent is actively processing
 * - STOPPING: Agent is in the process of stopping
 * - STOPPED: Agent has stopped
 * - ERROR: Agent is in an error state
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
public enum AgentState {

    /**
     * Agent is being initialized
     */
    INITIALIZING,

    /**
     * Agent is ready to accept actions
     */
    READY,

    /**
     * Agent is actively processing
     */
    RUNNING,

    /**
     * Agent is in the process of stopping
     */
    STOPPING,

    /**
     * Agent has stopped
     */
    STOPPED,

    /**
     * Agent is in an error state
     */
    ERROR;

    /**
     * Check if the agent is in an active state (ready or running)
     * 
     * @return true if the agent is active
     */
    public boolean isActive() {
        return this == READY || this == RUNNING;
    }

    /**
     * Check if the agent is in a transitional state
     * 
     * @return true if the agent is transitioning
     */
    public boolean isTransitional() {
        return this == INITIALIZING || this == STOPPING;
    }

    /**
     * Check if the agent is in a final state
     * 
     * @return true if the agent is in a final state
     */
    public boolean isFinal() {
        return this == STOPPED || this == ERROR;
    }

    /**
     * Check if the agent can accept actions
     * 
     * @return true if the agent can accept actions
     */
    public boolean canAcceptActions() {
        return this == READY || this == RUNNING;
    }

    /**
     * Get a human-readable description of the state
     * 
     * @return the state description
     */
    public String getDescription() {
        switch (this) {
            case INITIALIZING:
                return "Initializing";
            case READY:
                return "Ready";
            case RUNNING:
                return "Running";
            case STOPPING:
                return "Stopping";
            case STOPPED:
                return "Stopped";
            case ERROR:
                return "Error";
            default:
                return "Unknown";
        }
    }
}
