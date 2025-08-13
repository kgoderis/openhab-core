package org.openhab.core.ai.agent.transport;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Status information for agent transport
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class StatusInfo {
    private final String status;

    public StatusInfo(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
