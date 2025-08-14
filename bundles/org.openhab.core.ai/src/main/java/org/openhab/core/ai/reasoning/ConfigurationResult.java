package org.openhab.core.ai.reasoning;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class ConfigurationResult {
    private final boolean success;
    private final String message;
    private final AutonomousBehaviorConfig.AgentConfiguration configuration;

    private ConfigurationResult(boolean success, String message, AutonomousBehaviorConfig.AgentConfiguration configuration) {
        this.success = success;
        this.message = message;
        this.configuration = configuration;
    }

    public static ConfigurationResult success(AutonomousBehaviorConfig.AgentConfiguration configuration) {
        return new ConfigurationResult(true, "Configuration successful", configuration);
    }

    public static ConfigurationResult invalid(String reason) {
        return new ConfigurationResult(false, "Configuration invalid: " + reason, null);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public AutonomousBehaviorConfig.AgentConfiguration getConfiguration() { return configuration; }
}


