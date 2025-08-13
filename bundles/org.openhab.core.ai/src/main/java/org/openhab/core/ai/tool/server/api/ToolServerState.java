package org.openhab.core.ai.tool.server.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enumeration of server states.
 * 
 * <p>
 * This enum defines the various states that an MCP server can be in during its lifecycle.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum ToolServerState {
    /**
     * Server is stopped and not running
     */
    STOPPED,

    /**
     * Server is in the process of starting up
     */
    STARTING,

    /**
     * Server is running and accepting requests
     */
    RUNNING,

    /**
     * Server is in the process of shutting down
     */
    STOPPING,

    /**
     * Server is in an error state
     */
    ERROR
}
