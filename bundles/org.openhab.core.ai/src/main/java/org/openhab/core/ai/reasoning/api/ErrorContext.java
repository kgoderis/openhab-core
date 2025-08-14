package org.openhab.core.ai.reasoning.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Context information for a reasoning error.
 *
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ErrorContext {
    private final String componentId;
    private final String operation;
    private final String agentId;
    private final long timestamp;
    private final String[] additionalInfo;

    public ErrorContext(String componentId, String operation, String agentId, long timestamp, String[] additionalInfo) {
        this.componentId = componentId;
        this.operation = operation;
        this.agentId = agentId;
        this.timestamp = timestamp;
        this.additionalInfo = additionalInfo;
    }

    public String getComponentId() { return componentId; }
    public String getOperation() { return operation; }
    public String getAgentId() { return agentId; }
    public long getTimestamp() { return timestamp; }
    public String[] getAdditionalInfo() { return additionalInfo; }
}


