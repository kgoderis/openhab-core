package org.openhab.core.ai.reasoning;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class PreferenceResult {
    private final boolean success;
    private final String message;
    private final AutonomousBehaviorConfig.UserPreferenceConfig preferences;

    private PreferenceResult(boolean success, String message, AutonomousBehaviorConfig.UserPreferenceConfig preferences) {
        this.success = success;
        this.message = message;
        this.preferences = preferences;
    }

    public static PreferenceResult success(AutonomousBehaviorConfig.UserPreferenceConfig preferences) {
        return new PreferenceResult(true, "Preference configuration successful", preferences);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public AutonomousBehaviorConfig.UserPreferenceConfig getPreferences() { return preferences; }
}
