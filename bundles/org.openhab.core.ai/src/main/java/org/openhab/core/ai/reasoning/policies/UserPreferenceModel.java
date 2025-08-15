package org.openhab.core.ai.reasoning.policies;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * User preference model learned from interactions and feedback.
 *
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class UserPreferenceModel {
    private final String userId;
    private final Map<String, Double> preferences = new ConcurrentHashMap<>();
    private final Map<String, Integer> interactionCounts = new ConcurrentHashMap<>();
    private double confidence = 0.0;

    public UserPreferenceModel(String userId) {
        this.userId = userId;
    }

    public void learnFromInteraction(String interactionType, Map<String, Object> interactionData, double feedbackScore,
            double learningRate) {
        double currentPreference = preferences.getOrDefault(interactionType, 0.5);
        double newPreference = currentPreference + learningRate * (feedbackScore - currentPreference);
        preferences.put(interactionType, Math.max(0.0, Math.min(1.0, newPreference)));

        interactionCounts.put(interactionType, interactionCounts.getOrDefault(interactionType, 0) + 1);
        updateConfidence();
    }

    public void updateFromFeedback(String feedbackType, double feedbackScore, double learningRate) {
        double currentPreference = preferences.getOrDefault(feedbackType, 0.5);
        double newPreference = currentPreference + learningRate * (feedbackScore - currentPreference);
        preferences.put(feedbackType, Math.max(0.0, Math.min(1.0, newPreference)));
        updateConfidence();
    }

    private void updateConfidence() {
        int totalInteractions = interactionCounts.values().stream().mapToInt(Integer::intValue).sum();
        confidence = Math.min(1.0, totalInteractions / 100.0);
    }

    public String getUserId() {
        return userId;
    }

    public Map<String, Double> getPreferences() {
        return Collections.unmodifiableMap(preferences);
    }

    public double getConfidence() {
        return confidence;
    }
}
