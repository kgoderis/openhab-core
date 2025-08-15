package org.openhab.core.ai.reasoning.results;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class PreferenceResult {
    private final boolean success;
    private final String message;
    private final org.openhab.core.ai.reasoning.policies.UserPreferenceConfig preferences;

    private PreferenceResult(boolean success, String message,
            org.openhab.core.ai.reasoning.policies.UserPreferenceConfig preferences) {
        this.success = success;
        this.message = message;
        this.preferences = preferences;
    }

    public static PreferenceResult success(org.openhab.core.ai.reasoning.policies.UserPreferenceConfig preferences) {
        return new PreferenceResult(true, "Preference configuration successful", preferences);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public org.openhab.core.ai.reasoning.policies.UserPreferenceConfig getPreferences() {
        return preferences;
    }
}
