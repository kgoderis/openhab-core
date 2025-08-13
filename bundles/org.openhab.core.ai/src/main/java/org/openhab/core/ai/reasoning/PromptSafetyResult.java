package org.openhab.core.ai.reasoning;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Safety validation result for a model prompt.
 *
 * <p>Contains safety issues, warnings, and recommendations.</p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class PromptSafetyResult {
    private final List<String> safetyIssues = new ArrayList<>();
    private final List<String> safetyWarnings = new ArrayList<>();
    private final List<String> recommendations = new ArrayList<>();

    public void addSafetyIssue(String issue) {
        safetyIssues.add(issue);
    }

    public void addSafetyWarning(String warning) {
        safetyWarnings.add(warning);
    }

    public void addRecommendation(String recommendation) {
        recommendations.add(recommendation);
    }

    public List<String> getSafetyIssues() {
        return new ArrayList<>(safetyIssues);
    }

    public List<String> getSafetyWarnings() {
        return new ArrayList<>(safetyWarnings);
    }

    public List<String> getRecommendations() {
        return new ArrayList<>(recommendations);
    }

    public boolean isSafe() {
        return safetyIssues.isEmpty();
    }

    public boolean hasWarnings() {
        return !safetyWarnings.isEmpty();
    }

    public boolean hasRecommendations() {
        return !recommendations.isEmpty();
    }

    public int getIssueCount() {
        return safetyIssues.size();
    }

    public int getWarningCount() {
        return safetyWarnings.size();
    }

    public int getRecommendationCount() {
        return recommendations.size();
    }
}


