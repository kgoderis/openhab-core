package org.openhab.core.ai.agent.collaboration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Access level for a shared collaboration context.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum ContextAccessLevel {
    /**
     * Private access - only specific agents can access
     */
    PRIVATE,

    /**
     * Shared access - agents in the shared list can access
     */
    SHARED,

    /**
     * Public access - any agent can access
     */
    PUBLIC
}
