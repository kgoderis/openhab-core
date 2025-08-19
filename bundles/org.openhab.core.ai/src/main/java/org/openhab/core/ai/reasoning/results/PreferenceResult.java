package org.openhab.core.ai.reasoning.results;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.reasoning.policies.UserPreferenceConfig;

@NonNullByDefault
public class PreferenceResult {
    private final boolean success;
    private final String message;
    private final UserPreferenceConfig preferences;

    private PreferenceResult(boolean success, String message, UserPreferenceConfig preferences) {
        this.success = success;
        this.message = message;
        this.preferences = preferences;
    }

    public static PreferenceResult success(UserPreferenceConfig preferences) {
        return new PreferenceResult(true, "Preference configuration successful", preferences);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public UserPreferenceConfig getPreferences() {
        return preferences;
    }
}
