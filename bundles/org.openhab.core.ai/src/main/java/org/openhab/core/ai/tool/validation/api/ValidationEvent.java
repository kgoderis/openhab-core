package org.openhab.core.ai.tool.validation.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.validation.ToolValidationResult;

/**
 * Validation event emitted by validation services.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ValidationEvent {
    private final String toolId;
    private final ToolValidationResult result;
    private final long timestamp;

    public ValidationEvent(String toolId, ToolValidationResult result) {
        this.toolId = toolId;
        this.result = result;
        this.timestamp = System.currentTimeMillis();
    }

    public String getToolId() {
        return toolId;
    }

    public ToolValidationResult getResult() {
        return result;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
