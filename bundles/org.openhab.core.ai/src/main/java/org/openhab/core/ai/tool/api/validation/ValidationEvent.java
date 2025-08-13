package org.openhab.core.ai.tool.api.validation;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Validation event emitted by validation services.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ValidationEvent {
    private final String toolId;
    private final ValidationResult result;
    private final long timestamp;

    public ValidationEvent(String toolId, ValidationResult result) {
        this.toolId = toolId;
        this.result = result;
        this.timestamp = System.currentTimeMillis();
    }

    public String getToolId() {
        return toolId;
    }

    public ValidationResult getResult() {
        return result;
    }

    public long getTimestamp() {
        return timestamp;
    }
}


