package org.openhab.core.ai.agent.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Agent lifecycle status.
 *
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public enum AgentStatus {
    IDLE,
    BUSY,
    OFFLINE,
    ERROR,
    STARTING,
    STOPPING
}
