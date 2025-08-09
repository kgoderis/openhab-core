package org.openhab.core.ai.agent.core;

/**
 * A2A Protocol Message Types.
 * 
 * <p>
 * Defines the different types of messages that can be sent to an agent
 * according to the A2A protocol specification. Not all message types
 * should result in task creation and execution.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
public enum MessageType {

    /**
     * Discovery messages for agent and capability discovery.
     * Examples: "discover_agents", "get_agent_capabilities"
     * Should NOT create execution tasks.
     */
    DISCOVERY,

    /**
     * Query messages for information and status requests.
     * Examples: "get_task_status", "list_available_skills", "ping"
     * Should NOT create execution tasks.
     */
    QUERY,

    /**
     * Execution messages for skill and action execution.
     * Examples: "execute_skill", "perform_action", "run_automation"
     * Should create execution tasks.
     */
    EXECUTION,

    /**
     * Control messages for task and system control.
     * Examples: "cancel_task", "pause_task", "resume_task"
     * Should NOT create execution tasks.
     */
    CONTROL,

    /**
     * Notification messages for event notifications.
     * Examples: "task_completed", "system_alert"
     * Should NOT create execution tasks.
     */
    NOTIFICATION
}
