package org.openhab.core.ai.reasoning.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class ConfigurationResult {
    private final boolean success;
    private final String message;
    private final AgentConfiguration configuration;

    private ConfigurationResult(boolean success, String message, AgentConfiguration configuration) {
        this.success = success;
        this.message = message;
        this.configuration = configuration;
    }

    public static ConfigurationResult success(AgentConfiguration configuration) {
        return new ConfigurationResult(true, "Configuration successful", configuration);
    }

    public static ConfigurationResult invalid(String reason) {
        return new ConfigurationResult(false, "Configuration invalid: " + reason, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public AgentConfiguration getConfiguration() {
        return configuration;
    }
}
