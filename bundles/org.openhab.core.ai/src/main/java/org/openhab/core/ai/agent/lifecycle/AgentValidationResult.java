package org.openhab.core.ai.agent.lifecycle;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Agent validation result
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentValidationResult {
    private final boolean valid;
    private final List<String> errors;

    public AgentValidationResult(boolean valid, List<String> errors) {
        this.valid = valid;
        this.errors = List.copyOf(errors);
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getErrors() {
        return errors;
    }
}
