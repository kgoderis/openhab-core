package org.openhab.core.ai.reasoning.policies;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class UserPreferenceConfig {
    private final String userId;
    private final Map<String, Object> preferences;
    private final boolean learningEnabled;
    private final double learningRate;

    public UserPreferenceConfig(String userId, Map<String, Object> preferences, boolean learningEnabled,
            double learningRate) {
        this.userId = userId;
        this.preferences = preferences;
        this.learningEnabled = learningEnabled;
        this.learningRate = learningRate;
    }

    public String getUserId() {
        return userId;
    }

    public Map<String, Object> getPreferences() {
        return preferences;
    }

    public boolean isLearningEnabled() {
        return learningEnabled;
    }

    public double getLearningRate() {
        return learningRate;
    }
}
