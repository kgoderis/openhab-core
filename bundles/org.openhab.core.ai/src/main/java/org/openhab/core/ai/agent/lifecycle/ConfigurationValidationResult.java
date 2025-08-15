package org.openhab.core.ai.agent.lifecycle;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration validation result.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ConfigurationValidationResult {
    private final Map<String, String> errors = new HashMap<>();
    private boolean valid = true;

    public void addError(String key, String message) {
        errors.put(key, message);
        valid = false;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public Map<String, String> getErrors() {
        return new HashMap<>(errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public String getErrorMessage() {
        if (errors.isEmpty()) {
            return "Configuration is valid";
        }

        StringBuilder message = new StringBuilder("Configuration validation errors:\n");
        for (Map.Entry<String, String> error : errors.entrySet()) {
            message.append("- ").append(error.getKey()).append(": ").append(error.getValue()).append("\n");
        }
        return message.toString();
    }
}
